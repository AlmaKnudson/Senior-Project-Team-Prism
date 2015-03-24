//
//  HomeController.swift
//  Prism Lights
//
//  Created by Cody Foltz on 11/19/14.
//  Copyright (c) 2014 Prism. All rights reserved.
//

import UIKit
let MAX_HUE:UInt32 = 65535



protocol BulbSettingsProtocol{
    /*mutating*/ func ApplySettings()
}



class HomeController: UIViewController, UICollectionViewDataSource, UICollectionViewDelegate, UIGestureRecognizerDelegate, BulbSettingsProtocol, ESTBeaconManagerDelegate {
    
    let lastBridgeMessage = "Looking for Bridge..."
    let SCANNING_MESSAGE = "Scanning for New Bridge..."
    let GROUP_SECTION = 0
    let BULB_SECTION = 1
    
    var retryConnection = true
    var beenConnected = false
    var skipNextHeartbeat = false
    var bulbToBeacon:[String:String] = [String:String]()
    
    //Create beacon manager instance
    let beaconManager : ESTBeaconManager = ESTBeaconManager()
    
    var bridgeSendAPI = PHBridgeSendAPI();
    
    
    var lightCount :Int = 0;
    var groupCount :Int = 1;
    
    @IBOutlet weak var scanningIndicator: UIActivityIndicatorView!
    @IBOutlet weak var loadingMessageLabel: UILabel!
    @IBOutlet weak var rescanButton: UIButton!
    @IBOutlet weak var loadingView: HomeLoadingView!
    @IBOutlet weak var bulbCollectionView: UICollectionView!
    
    @IBAction func StartRescan(sender: UIButton) {
        
        (UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!.enableLocalConnection()
        self.loadingMessageLabel.text = lastBridgeMessage
        //        SearchForBridge(true, portalSearch: false, ipAddressSearch: false)
        sender.hidden = true
        scanningIndicator.startAnimating()
    }
    
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
        
        //Get the light count
        var cache:PHBridgeResourcesCache? = PHBridgeResourcesReader.readBridgeResourcesCache()
        if(cache != nil){
            var lights = cache?.lights;
            if(lights == nil){
                lightCount = 0;
            } else{
                lightCount = (cache?.lights.count)!
            }
        }
        
        //Button look and feel
        rescanButton.backgroundColor = UIColor.whiteColor()
        rescanButton.layer.cornerRadius = 10
        rescanButton.layer.borderWidth = 2
        rescanButton.layer.borderColor = UIColor.darkGrayColor().CGColor
        
        
        
        
    }
    
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override init() {
        super.init()
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
        
        
        //Check for previous bridge connection
        var cache:PHBridgeResourcesCache! = PHBridgeResourcesReader.readBridgeResourcesCache()
        if(cache != nil){
            
            if(cache.bridgeConfiguration == nil || cache.bridgeConfiguration.ipaddress == nil ){
                //TODO: First Time App user - App has never connected to bridge
                
                //This line is here so that there is always an IP set if they haven't connected to a bridge in the past.
                (UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!.setBridgeToUseWithIpAddress("1.1.1.1", macAddress: "ab:ab:ab:ab:ab:ab")
            }
            
        }
        
        //Check that we are connected to bridge.
        if !((UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!.localConnected()){
            //Connect to bridge
            (UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!.enableLocalConnection()
            self.loadingMessageLabel.text = lastBridgeMessage
        } else{
            bulbCollectionView.reloadData()
        }
        
        
        if(BRIDGELESS){
            HideConnectingView()
        }
        
    }
    
    override func viewDidAppear(animated: Bool) {
        bulbCollectionView.reloadData()
    }
    
    override func viewWillDisappear(animated: Bool) {
        
        if(DEBUG){
            println("Home Controller will disappeared")
        }
    }
    override func viewDidDisappear(animated: Bool) {
        PHNotificationManager.defaultManager().deregisterObjectForAllNotifications(self)
        if(DEBUG){
            println("Home disappeared")
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
            var dest = segue.destinationViewController as UINavigationController
            var bulbSettingsController = dest.viewControllers[0] as BulbSettingsController
            bulbSettingsController.homeDelegate = self
            bulbSettingsController.bulbId = "\((sender as NSIndexPath).row+1)"
            bulbSettingsController.isGroup = false
        } else if segue.identifier == "pushAuth" {
            var dest = segue.destinationViewController as PushAuthController
            dest.delegate = self
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
        
        
        if(section == BULB_SECTION){
            
            if(cache.lights != nil){
                lightCount = cache.lights.count
            } else{
                lightCount = 0
            }
            if(BRIDGELESS){
                lightCount = 3
            }
            
            return lightCount;
        }
        
        if section == GROUP_SECTION {
            if(cache.groups != nil){
                //count doesn't account for group number 0. So +1 to account for it.
                groupCount = cache.groups.count + 1
            } else {
                groupCount = 1
            }
            if(BRIDGELESS){
                groupCount = 1
            }
            return groupCount
        }
        return 0
    }
    
    func numberOfSectionsInCollectionView(collectionView: UICollectionView) -> Int {
        return 2
    }
    
    
    // The cell that is returned must be retrieved from a call to -dequeueReusableCellWithReuseIdentifier:forIndexPath:
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell{
        
        var cell:BulbCollectionCell! = bulbCollectionView.dequeueReusableCellWithReuseIdentifier("bulb", forIndexPath: indexPath) as? BulbCollectionCell
        if( cell == nil){
            cell = BulbCollectionCell()
        }
        
        var cache:PHBridgeResourcesCache! = PHBridgeResourcesReader.readBridgeResourcesCache()
        
        if indexPath.section == BULB_SECTION {
            var lightId = indexPath.row+1
            var light:PHLight! = cache.lights?["\(lightId)"] as? PHLight
            if(light != nil){
                
                cell.SetBulbLabel(light.name)
                if(light.lightState.on == 0){
                    cell.SetBulbImage(false, animate:false)
                } else{
                    var point = CGPoint(x: Double(light.lightState.x), y: Double(light.lightState.y))
                    var color = PHUtilities.colorFromXY(point, forModel: light.modelNumber)
                    cell.SetBulbImage(true, animate:false)
                    cell.SetBulbColor(color)
                    if (light.lightState.reachable == 0){
                        cell.SetBulbUnreachable()
                    }
                }
            } else{
                cell.SetBulbImage(false, animate:false)
            }
        } else if indexPath.section == GROUP_SECTION {
            var groupId = indexPath.row
            var lightState = PHLightState()
            lightState.on = false
            var group:PHGroup! = cache?.groups?["\(groupId)"] as? PHGroup
            
            if groupId == 0 {
                cell.SetBulbLabel("All Bulbs")
            } else {
                cell.SetBulbLabel(group.name)
            }
            
            var theLight:PHLight! = nil
            for (lightId, light) in (cache.lights as [String:PHLight]){
                //Ignore lights not connected to bridge
                if(light.lightState.reachable == 0){
                    continue
                }
                
                if light.lightState.on == 1{
                    theLight = light
                    lightState.on = true
                    lightState.x = light.lightState.x
                    lightState.y = light.lightState.y
                    break
                }
            }
            
            if(lightState.on == 1){
                var point = CGPoint(x: Double(lightState.x), y: Double(lightState.y))
                var color = PHUtilities.colorFromXY(point, forModel: theLight.modelNumber)
                cell.SetGroupImage(true)
                cell.SetGroupColor(color)
            } else{
                cell.SetGroupImage(false)
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
            var identifier = "\(indexPath.row+1)"
            ToggleLightState(identifier)
            
        }
        
        if( indexPath.section == GROUP_SECTION){
            var identifier = "\(indexPath.row)"
            ToggleGroupState(identifier)
            
        }
        collectionView.reloadData()
        
        
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
                self.performSegueWithIdentifier("BulbSettingsNav", sender: indexPath)
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
        if(beenConnected){
            if(loadingView.hidden != true){
                HideConnectingView()
            }
        }
        
        self.bulbCollectionView.reloadData()
    }
    
    /**
    
    Handles the not authorized with the bridge PHNotification
    
    :param: void
    
    :returns: void
    
    */
    func NotAuthorized(){
        var hueSDK = (UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!
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
        
        var hueSDK = (UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!
        
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
            if !BRIDGELESS {
                if loadingView.hidden == true {
                    ShowConnectingView()
                }
                loadingMessageLabel.text = SCANNING_MESSAGE
                SearchForBridge(true, portalSearch: false, ipAddressSearch: false)
            }
        }
        
    }
    
    //MARK: Helper Methods
    
    func HideConnectingView(){
        loadingView.hidden = true
        scanningIndicator.stopAnimating()
    }
    
    func ShowConnectingView(){
        loadingView.hidden = false
    }
    
    
    func ToggleLightState(identifier:String) -> Bool {
        var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
        var lightState = PHLightState()
        var bridgeSendAPI = PHBridgeSendAPI()
        
        var light = cache!.lights[identifier] as PHLight
        if(light.lightState.reachable == 0){
            return false
        }
        if light.lightState.on == 1{
            light.lightState.on = false
            lightState.on = false
        } else{
            lightState.on = true
            light.lightState.on = true
        }
        
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
        
        return lightState.on == 1
    }
    
    func ToggleGroupState(identifier:String) -> Bool {
        var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
        var lightState = PHLightState()
        var bridgeSendAPI = PHBridgeSendAPI()
        lightState.on = true
        
        //Group 0 is all lights
        //Group 0 is assumed and isn't in the cache
        if identifier == "0" {
            for (lightId, light) in (cache.lights as [String:PHLight]){
                //Ignore lights not connected to bridge
                if(light.lightState.reachable == 0){
                    continue
                }
                
                if light.lightState.on == 1{
                    lightState.on = false
                    break
                }
            }
        } else {
            
            var group:PHGroup = cache!.groups[identifier] as PHGroup
            
            
            for lightId:String in (group.lightIdentifiers as [String]) {
                var light = cache.lights[lightId] as PHLight
                
                
                
            }
        }
        //Send Light change to Bridge
        bridgeSendAPI.setLightStateForGroupWithId(identifier, lightState: lightState){
            error -> Void in
            if error != nil {
                if(DEBUG){
                    println("Error updating light state.")
                }
                return
            }
            self.skipNextHeartbeat = true
        }
        
        return lightState.on == 1
        
        
    }
    
    
    
    func SearchForBridge(upnpSearch:Bool, portalSearch:Bool, ipAddressSearch:Bool){
        
        var hueSDK = (UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!
        let bridgeSearch = PHBridgeSearching(upnpSearch: upnpSearch, andPortalSearch: portalSearch, andIpAdressSearch: ipAddressSearch)
        
        bridgeSearch.startSearchWithCompletionHandler { (dict:[NSObject : AnyObject]!) -> Void in
            self.scanningIndicator.stopAnimating()
            var addresses = dict as [String:String]
            var macAddresses = [String](addresses.keys)
            if(addresses.count == 1){
                self.loadingMessageLabel.text = "Connected!"
                var mac = macAddresses[0]
                var ipaddress = addresses[mac]
                hueSDK.setBridgeToUseWithIpAddress(ipaddress, macAddress: mac)
                hueSDK.enableLocalConnection()
            } else if(addresses.count > 1){
                //TODO: Present choices
                var alert = UIAlertController(title: "1+ Bridges Found", message: "More than 1 bridge has been dectected.", preferredStyle: UIAlertControllerStyle.Alert)
                let cancelButton = UIAlertAction(title: "Okay", style: UIAlertActionStyle.Cancel) { (cancelButton) -> Void in     }
                alert.addAction(cancelButton)
                //Show the alert to the user
                self.presentViewController(alert, animated: true) { () -> Void in}
                
            } else{
                
                self.rescanButton.hidden = false
                self.loadingMessageLabel.text = "No Bridges Found."
                var alert = UIAlertController(title: "No Bridge Found", message: "Please ensure you are connected to the wireless.", preferredStyle: UIAlertControllerStyle.Alert)
                let cancelButton = UIAlertAction(title: "Okay", style: UIAlertActionStyle.Cancel) { (cancelButton) -> Void in     }
                alert.addAction(cancelButton)
                //Show the alert to the user
                self.presentViewController(alert, animated: true) { () -> Void in}
                
            }
        }
    }
    
    func ApplySettings(){
        self.dismissViewControllerAnimated(true, completion: nil)
        HideConnectingView()
        self.bulbCollectionView.reloadData()
        //TODO: Need protocol for pushAuth
    }
    
    func beaconManager(manager: ESTBeaconRegion, didRangeBeacons: [ESTBeacon], inRegion: ESTBeaconRegion){
        //        println("I've found \(didRangeBeacons.count) beacons in range.")
        
        if(didRangeBeacons.count > 0) {
            //Keeping track of all beacon macAddresses in beaconMA set
            var closestBeacon:ESTBeacon = didRangeBeacons.first!
            var bId:NSString = "\(closestBeacon.major)_\(closestBeacon.minor)"
            
            var defaults = NSUserDefaults.standardUserDefaults()
            //            defaults.setObject(bbtString, forKey: currentBeaconIdentifier)
            var bulbIdPlusRange: AnyObject? = defaults.objectForKey(bId)
            //            if bulbIdPlusRange != nil {
            //                NSLog("Found a bulb/beacon association::\(bulbIdPlusRange)::::\(bId)")
            //            }
            
            var cache:PHBridgeResourcesCache? = PHBridgeResourcesReader.readBridgeResourcesCache()
            
            
            for b in didRangeBeacons{
                bId = "\(b.major)_\(b.minor)"
                bulbIdPlusRange = defaults.objectForKey(bId)
                if bulbIdPlusRange != nil {
                    NSLog("Found a bulb/beacon association::\(bulbIdPlusRange)::::\(bId)")
                    
                    
                    var bulb = bulbIdPlusRange as String
                    
                    
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
    
    
}

