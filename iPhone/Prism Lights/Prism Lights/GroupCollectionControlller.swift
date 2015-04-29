//
//  GroupCollectionControlller.swift
//  Prism Lights
//
//  Created by Cody Foltz on 4/14/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

class GroupCollectionController: UIViewController, UICollectionViewDataSource, UICollectionViewDelegate, UIGestureRecognizerDelegate, BulbSettingsProtocol, DismissPresentedController, ESTBeaconManagerDelegate {
    
    let GROUP_SECTION = 0
    
    var retryConnection = true
    var beenConnected = false
    var skipNextHeartbeat = false
    var bulbToBeacon:[String:String] = [String:String]()
    
    //Create beacon manager instance
    let beaconManager : ESTBeaconManager = ESTBeaconManager()
    
    var bridgeSendAPI = PHBridgeSendAPI();
    
    
    @IBOutlet weak var bulbCollectionView: UICollectionView!
    
    
    //MARK: - UIViewController Methods
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //set beacon manager delegate
        beaconManager.delegate = self
        
        var beaconRegion : ESTBeaconRegion = ESTBeaconRegion(proximityUUID: NSUUID(UUIDString:"B9407F30-F5F8-466E-AFF9-25556B57FE6D"), identifier: "regionName")
        
        beaconManager.requestWhenInUseAuthorization()
        
        beaconManager.startRangingBeaconsInRegion(beaconRegion)
        
        //Add long press Gesture
        var gesture = UILongPressGestureRecognizer(target: self, action: "ShowBulbSettings:")
        gesture.minimumPressDuration = 0.25
        gesture.delegate = self
        self.bulbCollectionView.addGestureRecognizer(gesture)
        
    }
    
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: NSBundle?) {
        super.init(nibName: nil, bundle: nil)
    }
    
    /**
    View Will Appear
    Registers the controller with the PHManager for bridge connections
    Then tries to connect to the bridge
    */
    override func viewWillAppear(animated: Bool) {
        var manager = PHNotificationManager.defaultManager()
        manager!.registerObject(self, withSelector: "HeartBeatReceived", forNotification: "LOCAL_CONNECTION_NOTIFICATION")
        manager!.registerObject(self, withSelector: "NetworkConnectionLost", forNotification: "NO_LOCAL_CONNECTION_NOTIFICATION")
        manager!.registerObject(self, withSelector: "NotAuthorized", forNotification: "NO_LOCAL_AUTHENTICATION_NOTIFICATION")
        
        
        //Check that we are connected to bridge.
        if !((UIApplication.sharedApplication().delegate as! AppDelegate).hueSDK!.localConnected()){
            //Connect to bridge
            (UIApplication.sharedApplication().delegate as! AppDelegate).hueSDK!.enableLocalConnection()
        }
        
    }
    
    override func viewDidAppear(animated: Bool) {
        bulbCollectionView.reloadData()
    }
    
    override func viewWillDisappear(animated: Bool) {
        PHNotificationManager.defaultManager().deregisterObjectForAllNotifications(self)
        
        if(DEBUG){
            println("Group Controller will disappeared")
        }
    }
    override func viewDidDisappear(animated: Bool) {
        if(DEBUG){
            println("Group disappeared")
        }
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if(DEBUG){
            println("In Prepare For Segue")
        }
        if segue.identifier == "BulbSettingsNav" {
            var dest = segue.destinationViewController as! UINavigationController
            var bulbSettingsController = dest.viewControllers[0] as! BulbSettingsController
            bulbSettingsController.homeDelegate = self
            
            let groupId = Groups[(sender as! NSIndexPath).row]
            var lightState = GetGroupState(groupId).lightState
            bulbSettingsController.Setup(lightState.brightness.integerValue, id: groupId, isGroup: true, name: GetGroupName(groupId)!)
        } else if segue.identifier == "pushAuth" {
            var dest = segue.destinationViewController as! PushAuthController
            dest.delegate = self
        } else if segue.identifier == "editCollection" {
            var dest = segue.destinationViewController as! EditBulbsCollection
            dest.editType = "group"
            dest.dismissDeleget = self
        }
    }
    
    //MARK: - UICollectionView Methods
    
    /**
    Number of sections
    
    :param: collectionView the bulb collection view
    :param: section        The sections, 0 is group, 1 is individual bulbs
    
    :returns: number of bulbs in group
    */
    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int{
        var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
        
        if section == GROUP_SECTION {
            return Groups.count()
        }
        return 0
    }
    
    func numberOfSectionsInCollectionView(collectionView: UICollectionView) -> Int {
        return 1
    }
    
    
    // The cell that is returned must be retrieved from a call to -dequeueReusableCellWithReuseIdentifier:forIndexPath:
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell{
        
        var cell:GroupBulbCell! = bulbCollectionView.dequeueReusableCellWithReuseIdentifier("groupCell", forIndexPath: indexPath) as! GroupBulbCell
        if( cell == nil){
            cell = GroupBulbCell()
        }
        
        var cache:PHBridgeResourcesCache! = PHBridgeResourcesReader.readBridgeResourcesCache()
        var groupId = Groups[indexPath.row]
        if indexPath.section == GROUP_SECTION {
            if groupId == "0" {
                
                //Name the group cell to all
                cell.initGroupCell("All Lights")
                
            } else {
                //Set the name of the group cell from group name
                var group:PHGroup! = cache?.groups?[groupId] as? PHGroup
                if group != nil {
                    cell.initGroupCell(group.name)
                } else{
                    cell.initGroupCell("Unknown")
                    cell.SetUnreachable()
                    return cell
                }
            }
            
            var tuple  = GetGroupState(groupId)
            let modelNumber = tuple.modelNumber
            let lightState = tuple.lightState
            
            //Check that the group is reachable
            if !lightState.reachable.boolValue {
                cell.SetUnreachable()
                return cell
            }
            
            //Set the bulb image
            if lightState.on.boolValue{
                var point = CGPoint(x: Double(lightState.x), y: Double(lightState.y))
                var color = PHUtilities.colorFromXY(point, forModel: modelNumber)
                cell.turnOn(false)
                cell.SetGroupColor(color)
            } else{
                cell.turnOff(false)
            }
        }
        
        return cell!
    }
    
    
    func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
        if(DEBUG){
            println("Bulb tapped")
        }
        
        self.skipNextHeartbeat = true
        if( indexPath.section == GROUP_SECTION){
            var identifier = Groups[indexPath.row]
            ToggleGroupState(identifier)
            
        }
        
        //Reload that cell
        var index:[AnyObject] = [AnyObject]()
        index.append(indexPath)
        collectionView.reloadItemsAtIndexPaths(index)
        
    }
    
    
    func collectionView(collectionView: UICollectionView, viewForSupplementaryElementOfKind kind: String, atIndexPath indexPath: NSIndexPath) -> UICollectionReusableView{
        //1
        switch kind {
            //2
        case UICollectionElementKindSectionHeader:
            //3
            let headerView =
            collectionView.dequeueReusableSupplementaryViewOfKind(kind,
                withReuseIdentifier: "SectionHeader",
                forIndexPath: indexPath)
                as! SectionHeader
            headerView.headerLabel.text = "Groups"
            headerView.headerType = "group"
            return headerView
        default:
            //4
            assert(false, "Unexpected element kind. Should only have headers.")
        }
    }

    
    
    
    /**
    Sets the phone status bar to be light colored for dark background
    
    :returns: UIStatusBarStyle
    */
    override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return UIStatusBarStyle.LightContent
    }
    
    
    func ShowBulbSettings( gestureRecognizer: UILongPressGestureRecognizer){
        
        if(gestureRecognizer.state == UIGestureRecognizerState.Began){
            var point = gestureRecognizer.locationInView(self.bulbCollectionView)
            var indexPath = self.bulbCollectionView.indexPathForItemAtPoint(point)
            if indexPath == nil{
                if(DEBUG){
                    println("Unable to find index")
                }
            } else{
                if(DEBUG){
                    println("indexPath of cell: \(indexPath)")
                }
                
                var groupId = Groups[indexPath!.row]
                var groupState = GetGroupState(groupId)
                if(groupState.lightState.reachable.boolValue){
                    self.performSegueWithIdentifier("BulbSettingsNav", sender: indexPath)
                }
            }
        }
        
        if gestureRecognizer.state != UIGestureRecognizerState.Ended{
            return
        }
        
        
    }
    
    
    //MARK: Notification Methods
    
    /**
    Handles the heartbeat event from the PHNotification
    Updates the Bulbs UI
    
    :param:
    
    :returns:
    */
    func HeartBeatReceived(){
        
        //skip one (this) heartbeat
        if skipNextHeartbeat{
            skipNextHeartbeat = false;
            return
        }
        
        //Successful connect to bridge has been made
        retryConnection = true
        beenConnected = true
        
        self.bulbCollectionView.reloadData()
    }
    
    /**
    
    Handles the not authorized with the bridge PHNotification
    
    :param: void
    
    :returns: void
    
    */
    func NotAuthorized(){
        var hueSDK = (UIApplication.sharedApplication().delegate as! AppDelegate).hueSDK!
        self.performSegueWithIdentifier("pushAuth", sender: self)
    }
    /**
    Handles the unable to connect bridge PHNotification
    
    :param: void
    
    :returns: void
    */
    func NetworkConnectionLost(){
        
        var hueSDK = (UIApplication.sharedApplication().delegate as! AppDelegate).hueSDK!
        
        //Has been connected to the bridge at least once during this session
        if(beenConnected){
            //returns for one more heartbeat timer
            if(retryConnection){
                retryConnection = false
                return
            }
            hueSDK.disableLocalConnection()
            beenConnected = false
            //Create the alert
            var alert = UIAlertController(title: "No Connection", message: "Connection to bridge lost. Insure the bridge is available and your network is working", preferredStyle: UIAlertControllerStyle.Alert)
            let reScan = UIAlertAction(title: "Re-Connect", style: UIAlertActionStyle.Default) {
                (scan) -> Void in
                
                
            }
            let cancelButton = UIAlertAction(title: "Okay", style: UIAlertActionStyle.Cancel) { (cancelButton) -> Void in     }
            alert.addAction(cancelButton)
            //Show the alert to the user
            self.presentViewController(alert, animated: true) { () -> Void in}
        } else{
            hueSDK.disableLocalConnection()
            
        }
        
    }
    
    //MARK: Helper Methods
    
    func ToggleGroupState(identifier:String) -> Bool {
        
        let groupState = GetGroupState(identifier)
        let groupLightState = groupState.lightState
        let modelNumber = groupState.modelNumber
        
        if !groupLightState.reachable.boolValue {
            return false
        }
     
        //Flip the on state of the group
        if groupLightState.on.boolValue {
            groupLightState.on = false
        } else{
            groupLightState.on = true
        }
        
        SetGroupLightState(identifier, groupLightState)
        return true
    }
    
    
    func ApplySettings(){
        var manager = PHNotificationManager.defaultManager()
        manager!.registerObject(self, withSelector: "HeartBeatReceived", forNotification: "LOCAL_CONNECTION_NOTIFICATION")
        manager!.registerObject(self, withSelector: "NetworkConnectionLost", forNotification: "NO_LOCAL_CONNECTION_NOTIFICATION")
        manager!.registerObject(self, withSelector: "NotAuthorized", forNotification: "NO_LOCAL_AUTHENTICATION_NOTIFICATION")
        self.dismissViewControllerAnimated(true, completion: nil)
        self.bulbCollectionView.reloadData()
    }
    
    func DismissMe() {
        var manager = PHNotificationManager.defaultManager()
        manager!.registerObject(self, withSelector: "HeartBeatReceived", forNotification: "LOCAL_CONNECTION_NOTIFICATION")
        manager!.registerObject(self, withSelector: "NetworkConnectionLost", forNotification: "NO_LOCAL_CONNECTION_NOTIFICATION")
        manager!.registerObject(self, withSelector: "NotAuthorized", forNotification: "NO_LOCAL_AUTHENTICATION_NOTIFICATION")
        self.dismissViewControllerAnimated(true, completion: nil)
    }
    
    

    
    //- (void)beaconManager:(ESTBeaconManager *)manager
    //    didStartMonitoringForRegion:(ESTBeaconRegion *)region;
    func beaconManager(manager: ESTBeaconManager!, didRangeBeacons beacons: [AnyObject]!, inRegion region: ESTBeaconRegion!) {
        if(beacons.count > 0) {
            //Keeping track of all beacon macAddresses in beaconMA set
            var closestBeacon:ESTBeacon = beacons.first as! ESTBeacon
            var bId:NSString = "\(closestBeacon.major)_\(closestBeacon.minor)"
            
            var defaults = NSUserDefaults.standardUserDefaults()
            //            defaults.setObject(bbtString, forKey: currentBeaconIdentifier)
            var bulbIdPlusRange: AnyObject? = defaults.objectForKey(bId as String)
            //            if bulbIdPlusRange != nil {
            //                NSLog("Found a bulb/beacon association::\(bulbIdPlusRange)::::\(bId)")
            //            }
            
            var cache:PHBridgeResourcesCache? = PHBridgeResourcesReader.readBridgeResourcesCache()
            
            var beacons2 = beacons as! [ESTBeacon]
            for b in beacons2 {
                bId = "\(b.major)_\(b.minor)"
                bulbIdPlusRange = defaults.objectForKey(bId as String)
                if bulbIdPlusRange != nil {
                    NSLog("Found a bulb/beacon association::\(bulbIdPlusRange)::::\(bId)")
                    
                    
                    var bulb = bulbIdPlusRange as! String
                    
                    
                    let resultArr = bulb.componentsSeparatedByString("_")
                    
                    var bulbId: String = resultArr[0]
                    var range: String? = resultArr[1]
                    //Found this beacon assocaitionsdafjlk with a bulb
                    if(cache != nil){
                        
                        for light in cache!.lights.values{
                            if light.identifier == bulbId {
                                //Found the bulb.
                                
                                var numberFormatter = NSNumberFormatter()
                                var number:NSNumber? = numberFormatter.numberFromString(range!)
                                var rangeInt = Float(number!)
                                //                                                            NSLog("\(b.distance.floatValue) < \(rangeInt)")
                                
                                
                                if (b.distance.floatValue/3.0) <= rangeInt {
                                    
                                    
                                    //This is to print out the distance to see if I get a different reading each time. Otherwise, I will have to poll for an average in a different/slower way.
                                    //                                    for var i = 0; i < 10; ++i  {
                                    NSLog("\(b.distance.floatValue)")
                                    //                                    }
                                    var lightState:PHLightState = PHLightState()
                                    //                        lightState.on = false
                                    lightState.on = true
                                    //                    lightState.hue = hueVal.toInt();
                                    //                        lightState.on = true;
                                    //                        lightState.saturation = 254;
                                    //                        lightState.brightness = currentBrightness;
                                    lightState.brightness = 254;
                                    
                                    
                                    bridgeSendAPI.updateLightStateForId(bulbId, withLightState: lightState, completionHandler: nil);
                                } else {
                                    var lightState:PHLightState = PHLightState()
                                    //                        lightState.on = false
                                    lightState.on = false
                                    //                    lightState.hue = hueVal.toInt();
                                    //                        lightState.on = true;
                                    //                        lightState.saturation = 254;
                                    //                        lightState.brightness = currentBrightness;
                                    lightState.brightness = 254;
                                    
                                    
                                    bridgeSendAPI.updateLightStateForId(bulbId, withLightState: lightState, completionHandler: nil);
                                }
                                
                            }
                            
                            
                            //                            var resultArr = split(bulb) {$0 == "_"}
                            //                            var bulbId: String = resultArr[0]
                            //                            var range: String? = resultArr.count > 1 ? resultArr[1] : nil
                        }
                        
                    }
                }
            }
            //
            //            //Closest beacon is at index 0
            //            var closestBeacon:ESTBeacon = didRangeBeacons.first!
            //            loadingCircle.stopAnimating()
            //            if (notTracking) {
            //                currentBeaconIdentifier  = "\(closestBeacon.major)_\(closestBeacon.minor)"
            //                searchingLabel.text = currentBeaconIdentifier
            //            } else {
            //                for b in didRangeBeacons {
            //                    if ( ("\(b.major)_\(b.minor)") == currentBeaconIdentifier ) {
            //                        closestBeacon = b
            //                    }
            //                }
            //            }
            //
            //            if ( (closestBeacon.distance).integerValue < range.integerValue/3){
            //                //update bulbs
            //                NSLog("Update Light Bulb__\(closestBeacon.distance)___\(range.integerValue)");
            //            }
        }
    }
    
    
    
    
    //    func beaconManager(manager: ESTBeaconRegion, didRangeBeacons: [ESTBeacon], inRegion: ESTBeaconRegion){
    //        //        println("I've found \(didRangeBeacons.count) beacons in range.")
    //
    //        if(didRangeBeacons.count > 0) {
    //            //Keeping track of all beacon macAddresses in beaconMA set
    //            var closestBeacon:ESTBeacon = didRangeBeacons.first!
    //            var bId:NSString = "\(closestBeacon.major)_\(closestBeacon.minor)"
    //
    //            var defaults = NSUserDefaults.standardUserDefaults()
    //            //            defaults.setObject(bbtString, forKey: currentBeaconIdentifier)
    //            var bulbIdPlusRange: AnyObject? = defaults.objectForKey(bId as String)
    //            //            if bulbIdPlusRange != nil {
    //            //                NSLog("Found a bulb/beacon association::\(bulbIdPlusRange)::::\(bId)")
    //            //            }
    //
    //            var cache:PHBridgeResourcesCache? = PHBridgeResourcesReader.readBridgeResourcesCache()
    //
    //
    //            for b in didRangeBeacons{
    //                bId = "\(b.major)_\(b.minor)"
    //                bulbIdPlusRange = defaults.objectForKey(bId as String)
    //                if bulbIdPlusRange != nil {
    //                    NSLog("Found a bulb/beacon association::\(bulbIdPlusRange)::::\(bId)")
    //
    //
    //                    var bulb = bulbIdPlusRange as! String
    //
    //
    //                    let resultArr = bulb.componentsSeparatedByString("_")
    //
    //                    var bulbId: String = resultArr[0]
    //                    var range: String? = resultArr[1]
    //                    //Found this beacon assocaitionsdafjlk with a bulb
    //                    if(cache != nil){
    //
    //                        for light in cache!.lights.values{
    //                            if light.identifier == bulbId {
    //                                //Found the bulb.
    //
    //                                var numberFormatter = NSNumberFormatter()
    //                                var number:NSNumber? = numberFormatter.numberFromString(range!)
    //                                var rangeInt = (Float(number!)/10)
    //                                //                                                            NSLog("\(b.distance.floatValue) < \(rangeInt)")
    //
    //
    //                                if b.distance.floatValue <= rangeInt {
    //
    //
    //                                    //This is to print out the distance to see if I get a different reading each time. Otherwise, I will have to poll for an average in a different/slower way.
    //                                    //                                    for var i = 0; i < 10; ++i  {
    //                                    NSLog("\(b.distance.floatValue)")
    //                                    //                                    }
    //                                    var lightState:PHLightState = PHLightState()
    //                                    //                        lightState.on = false
    //                                    lightState.on = true
    //                                    //                    lightState.hue = hueVal.toInt();
    //                                    //                        lightState.on = true;
    //                                    //                        lightState.saturation = 254;
    //                                    //                        lightState.brightness = currentBrightness;
    //                                    lightState.brightness = 254;
    //
    //
    //                                    bridgeSendAPI.updateLightStateForId(bulbId, withLightState: lightState, completionHandler: nil);
    //                                } else {
    //                                    var lightState:PHLightState = PHLightState()
    //                                    //                        lightState.on = false
    //                                    lightState.on = false
    //                                    //                    lightState.hue = hueVal.toInt();
    //                                    //                        lightState.on = true;
    //                                    //                        lightState.saturation = 254;
    //                                    //                        lightState.brightness = currentBrightness;
    //                                    lightState.brightness = 254;
    //
    //
    //                                    bridgeSendAPI.updateLightStateForId(bulbId, withLightState: lightState, completionHandler: nil);
    //                                }
    //
    //                            }
    //
    //
    //                            //                            var resultArr = split(bulb) {$0 == "_"}
    //                            //                            var bulbId: String = resultArr[0]
    //                            //                            var range: String? = resultArr.count > 1 ? resultArr[1] : nil
    //                        }
    //
    //                    }
    //                }
    //            }
    //            //
    //            //            //Closest beacon is at index 0
    //            //            var closestBeacon:ESTBeacon = didRangeBeacons.first!
    //            //            loadingCircle.stopAnimating()
    //            //            if (notTracking) {
    //            //                currentBeaconIdentifier  = "\(closestBeacon.major)_\(closestBeacon.minor)"
    //            //                searchingLabel.text = currentBeaconIdentifier
    //            //            } else {
    //            //                for b in didRangeBeacons {
    //            //                    if ( ("\(b.major)_\(b.minor)") == currentBeaconIdentifier ) {
    //            //                        closestBeacon = b
    //            //                    }
    //            //                }
    //            //            }
    //            //
    //            //            if ( (closestBeacon.distance).integerValue < range.integerValue/3){
    //            //                //update bulbs
    //            //                NSLog("Update Light Bulb__\(closestBeacon.distance)___\(range.integerValue)");
    //            //            }
    //        }
    //    }
    
    
}
