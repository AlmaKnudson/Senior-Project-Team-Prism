//
//  BulbsCollectionController.swift
//  Prism Lights
//
//  Created by Cody Foltz on 11/19/14.
//  Copyright (c) 2014 Prism. All rights reserved.
//

import UIKit


class BulbsCollectionController: UIViewController, UICollectionViewDataSource, UICollectionViewDelegate, UIGestureRecognizerDelegate, BulbSettingsProtocol, DismissPresentedController, ESTBeaconManagerDelegate {
    
    
    let BULB_SECTION = 0
    let GROUP_SECTION = 1
    let NUM_OF_SECTIONS = 1
    
    
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
        gesture.minimumPressDuration = 0.50
        gesture.delegate = self
        self.bulbCollectionView.addGestureRecognizer(gesture)
        
        //Make sure the bulbs are updated
        BulbsModel.CompareToCache()
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
        
        if(DEBUG){
            println("Bulb View Will Appear")
        }
        
        //Check that we are connected to bridge.
        if !((UIApplication.sharedApplication().delegate as! AppDelegate).hueSDK!.localConnected()){
            //Connect to bridge
            (UIApplication.sharedApplication().delegate as! AppDelegate).hueSDK!.enableLocalConnection()
        }

    }
    
    override func viewDidAppear(animated: Bool) {
        var manager = PHNotificationManager.defaultManager()
        manager!.registerObject(self, withSelector: "HeartBeatReceived", forNotification: "LOCAL_CONNECTION_NOTIFICATION")
        manager!.registerObject(self, withSelector: "NetworkConnectionLost", forNotification: "NO_LOCAL_CONNECTION_NOTIFICATION")
        manager!.registerObject(self, withSelector: "NotAuthorized", forNotification: "NO_LOCAL_AUTHENTICATION_NOTIFICATION")
        
        
    }
    
    override func viewWillDisappear(animated: Bool) {
        PHNotificationManager.defaultManager().deregisterObjectForAllNotifications(self)
    }
    override func viewDidDisappear(animated: Bool) {
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
            var bulbId = BulbsModel[(sender as! NSIndexPath).row]
            if let light = GetBulbLightState(bulbId) {
                bulbSettingsController.Setup(light.brightness.integerValue, id: bulbId, isGroup: false, name: GetBulbName(bulbId)!)
            }
        } else if segue.identifier == "pushAuth" {
            var dest = segue.destinationViewController as! PushAuthController
            dest.delegate = self
        } else if segue.identifier == "editCollection" {
            var dest = segue.destinationViewController as! EditBulbsCollection
            dest.editType = "single"
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
        if(section == BULB_SECTION){
            return BulbsModel.count();
        }
        return 0
    }
    
    func numberOfSectionsInCollectionView(collectionView: UICollectionView) -> Int {
        return NUM_OF_SECTIONS
    }
    
    
    // The cell that is returned must be retrieved from a call to -dequeueReusableCellWithReuseIdentifier:forIndexPath:
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell{
        
        var cell:BulbCollectionCell! = bulbCollectionView.dequeueReusableCellWithReuseIdentifier("bulbCell", forIndexPath: indexPath) as? BulbCollectionCell
        if( cell == nil){
            cell = BulbCollectionCell()
        }
        
        var cache:PHBridgeResourcesCache! = PHBridgeResourcesReader.readBridgeResourcesCache()
        
        if indexPath.section == BULB_SECTION {
            var lightId = BulbsModel[indexPath.row]
            var light:PHLight! = cache.lights?["\(lightId)"] as? PHLight
            if(light != nil){
                cell.initBulbCell(light.name)
                
                if !light.lightState.reachable.boolValue {
                    cell.SetUnreachable()
                } else if(!light.lightState.on.boolValue){
                    cell.turnOff(false)
                } else{
                    var point = CGPoint(x: Double(light.lightState.x), y: Double(light.lightState.y))
                    var color = PHUtilities.colorFromXY(point, forModel: light.modelNumber)
                    cell.SetBulbColor(color)
                    cell.turnOn(false)
                    
                }
            } else{
                cell.initBulbCell("")
            }
        }
        
        return cell!
    }
    
    
    func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
        if(DEBUG){
            println("Bulb tapped")
        }
        self.skipNextHeartbeat = true
        if( indexPath.section == BULB_SECTION){
            var identifier = BulbsModel[indexPath.row]
            ToggleLightState(identifier)
            
        }
        
//        if( indexPath.section == GROUP_SECTION){
//            var identifier = "\(indexPath.row)"
//            ToggleGroupState(identifier)
//            
//        }
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
            headerView.headerLabel.text = "Individual Bulbs"
            headerView.headerType = "single"
            return headerView
        default:
            //4
            assert(false, "Unexpected element kind")
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
                
                var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
                var index = indexPath!.row
                var lightId = BulbsModel[index]
                var light = cache.lights["\(lightId)"] as! PHLight
                if(light.lightState.reachable.boolValue){
                    self.performSegueWithIdentifier("BulbSettingsNav", sender: indexPath)
                }
            }
        }
        
        if gestureRecognizer.state != UIGestureRecognizerState.Ended{
            return
        }
        
    }
    
    
    //MARK: - Notification Methods
    
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
        //        hueSDK.disableLocalConnection()
        //        var alert = UIAlertController(title: "Authenticate", message: "Press button on Bridge to Authenticate.", preferredStyle: UIAlertControllerStyle.Alert)
        //        let cancelButton = UIAlertAction(title: "Okay", style: UIAlertActionStyle.Cancel) { (cancelButton) -> Void in     }
        //        alert.addAction(cancelButton)
        //        //Show the alert to the user
        //        self.presentViewController(alert, animated: true) { () -> Void in}
        self.performSegueWithIdentifier("pushAuth", sender: self)
    }
    /**
    Handles the unable to connect bridge PHNotification
    
    :param: void
    
    :returns: void
    */
    func NetworkConnectionLost(){
        
        var hueSDK = (UIApplication.sharedApplication().delegate as! AppDelegate).hueSDK!
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
        
        
    }
    
    //MARK: - Helper Methods
    
    
    func ToggleLightState(identifier:String) -> Bool {
        var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
        var lightOn = true
        var bridgeSendAPI = PHBridgeSendAPI()
        
        var light = cache!.lights[identifier] as! PHLight
        if(light.lightState.reachable == 0){
            return false
        }
        if light.lightState.on == 1{
            light.lightState.on = false
        } else{
            light.lightState.on = true
        }
        
        var lightState = PHLightState()
        lightState.x = light.lightState.x
        lightState.y = light.lightState.y
        lightState.on = light.lightState.on
        lightState.brightness = light.lightState.brightness
        
        //Send Light change to Bridge
        bridgeSendAPI.updateLightStateForId(identifier, withLightState: lightState){
            error -> Void in
            if error != nil {
                if(DEBUG){
                    println("Error updating light state.")
                }
                return
            }
            
        }
        
        return light.lightState.on.boolValue
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
                                var rangeInt = (Float(number!)/10)
                                //                                                            NSLog("\(b.distance.floatValue) < \(rangeInt)")
                                
                                
                                if b.distance.floatValue <= rangeInt {
                                    
                                    
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

