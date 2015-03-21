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
        gesture.minimumPressDuration = 0.5
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
            
            if(cache.bridgeConfiguration == nil){
                //TODO: First Time App user - App has never connected to bridge
                
            } else{
                if cache.bridgeConfiguration.ipaddress == nil {
                    //TODO: No previous bridge saved
                }
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
        var dest = segue.destinationViewController as UINavigationController
        var bulbSettingsController = dest.viewControllers[0] as BulbSettingsController
        bulbSettingsController.homeDelegate = self
        bulbSettingsController.bulbId = "\((sender as NSIndexPath).row+1)"
        bulbSettingsController.isGroup = false
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
        
        
        if(section == 1){
            
            if(cache.lights != nil){
                lightCount = cache.lights.count
            }
            if(BRIDGELESS){
                lightCount = 3
            }
            
            return lightCount;
        }
        
        if section == 0 {
            if(cache.lights != nil){
                groupCount = cache.groups.count
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
        
        var cell = bulbCollectionView.dequeueReusableCellWithReuseIdentifier("bulb", forIndexPath: indexPath) as? BulbCollectionCell
        if( cell == nil){
            cell = BulbCollectionCell()
        }
        
        var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
        
        var light = cache?.lights?["\(indexPath.row+1)"] as? PHLight
        //        var sdk = ((UIApplication.sharedApplication().delegate) as AppDelegate).hueSDK!
        if(light != nil){
            
            
            cell!.SetBulbLabel(light!.name)
            if(light!.lightState.on == 0){
                cell!.SetBulbImage(false)
            } else{
                //cell!.SetBulbImage(true)
                var point = CGPoint(x: Double(light!.lightState.x), y: Double(light!.lightState.y))
                var color = PHUtilities.colorFromXY(point, forModel: light!.modelNumber)
                //var color = UIColor(hue: CGFloat(light!.lightState.hue), saturation: CGFloat(light!.lightState.saturation), brightness: CGFloat(light!.lightState.brightness), alpha: 1)
                cell!.SetBulbImage(true)
                cell!.SetBulbColor(color)
                if (light?.lightState.reachable == 0){
                    cell!.SetBulbUnreachable()
                }
            }
        } else{
            cell!.SetBulbImage(false)
            //            cell!.SetBulbUnreachable()
        }
        
        return cell!
    }
    
    
    func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
        if(DEBUG){
            println("Bulb tapped")
        }
        var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
        var light = cache!.lights["\(indexPath.row+1)"] as PHLight
        
        if(light.lightState.reachable == 0){
            return
        }
        var lightState = PHLightState()
        var cell = bulbCollectionView.cellForItemAtIndexPath(indexPath) as BulbCollectionCell
        if light.lightState.on == 1{
            light.lightState.on = false
            lightState.on = false
        } else{
            lightState.on = true
            light.lightState.on = true
            //cell.SetBulbImage(true)
            
        }
        var bridgeSendAPI = PHBridgeSendAPI()
        bridgeSendAPI.updateLightStateForId(light.identifier, withLightState: lightState, completionHandler: nil)
        if(light.lightState.on == 1){
            var point = CGPoint(x: Double(light.lightState.x), y: Double(light.lightState.y))
            var color = PHUtilities.colorFromXY(point, forModel: light.modelNumber)
            cell.SetBulbColor(color)
        } else{
            cell.SetBulbImage(false)
        }
        bridgeSendAPI.updateLightStateForId(light.identifier, withLightState: lightState){
            error -> Void in
            if error != nil {
                if(DEBUG){
                    println("Error updating light state.")
                }
                return
            }
            
            if(light.lightState.on == 1){
                var point = CGPoint(x: Double(light.lightState.x), y: Double(light.lightState.y))
                var color = PHUtilities.colorFromXY(point, forModel: light.modelNumber)
                cell.SetBulbColor(color)
            } else{
                cell.SetBulbImage(false)
            }
            self.skipNextHeartbeat = true
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
    
    func HideConnectingView(){
        loadingView.hidden = true
        scanningIndicator.stopAnimating()
    }
    
    func ShowConnectingView(){
        loadingView.hidden = false
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
            //TODO: Add retry connection button.
            //Show the alert to the user
            self.presentViewController(alert, animated: true) { () -> Void in}
        } else{
            hueSDK.disableLocalConnection()
            if loadingView.hidden == true {
                ShowConnectingView()
            }
            loadingMessageLabel.text = SCANNING_MESSAGE
            SearchForBridge(true, portalSearch: false, ipAddressSearch: false)
            
            
        }
        
        
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
                //TODO: Add retry connection button.
                //Show the alert to the user
                self.presentViewController(alert, animated: true) { () -> Void in}
                
            } else{
                
                self.rescanButton.hidden = false
                self.loadingMessageLabel.text = "No Bridges Found."
                var alert = UIAlertController(title: "No Bridge Found", message: "Please ensure you are connected to the wireless.", preferredStyle: UIAlertControllerStyle.Alert)
                let cancelButton = UIAlertAction(title: "Okay", style: UIAlertActionStyle.Cancel) { (cancelButton) -> Void in     }
                alert.addAction(cancelButton)
                //TODO: Add retry connection button.
                //Show the alert to the user
                self.presentViewController(alert, animated: true) { () -> Void in}
                
            }
        }
    }
    
    /**
    
    Handles the not authorized with the bridge PHNotification
    
    :param: void
    
    :returns: void
    
    */
    func NotAuthorized(){
        var hueSDK = (UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!
        hueSDK.disableLocalConnection()
        //TODO: Notify user of lost Authorization
        //TODO: Initiate Push Auth
    }
    
    
    func ApplySettings(){
        self.dismissViewControllerAnimated(true, completion: nil)
    }
    
    
    func beaconManager(manager: ESTBeaconRegion, didRangeBeacons: [ESTBeacon], inRegion: ESTBeaconRegion){
        //        println("I've found \(didRangeBeacons.count) beacons in range.")
        
        if(didRangeBeacons.count > 0) {
            //Keeping track of all beacon macAddresses in beaconMA set
            var closestBeacon:ESTBeacon = didRangeBeacons.first!
            var bId:NSString = "\(closestBeacon.major)_\(closestBeacon.minor)"
            
            var defaults = NSUserDefaults.standardUserDefaults()
            //            defaults.setObject(bbtString, forKey: currentBeaconIdentifier)
            var bulbIdPlusRange = defaults.objectForKey(bId)
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
                                var rangeInt = (Int(number!)/10)
//                                NSLog()
                                
                                
                                if b.distance.integerValue <= rangeInt {
                                   
                                    
                                    //This is to print out the distance to see if I get a different reading each time. Otherwise, I will have to poll for an average in a different/slower way.
                                    for var i = 0; i < 10; ++i  {
                                        NSLog("\(b.distance)")
                                    }
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

