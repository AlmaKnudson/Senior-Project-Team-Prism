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



class HomeController: UIViewController, UICollectionViewDataSource, UICollectionViewDelegate, UIGestureRecognizerDelegate, BulbSettingsProtocol {
    
    @IBOutlet weak var bulbCollectionView: UICollectionView!
    var retryConnection = true
    var beenConnected = false
    var skipNextHeartbeat = false
    @IBOutlet weak var loadingView: UIView!
    
    
    var lightCount :Int = 0;
    var groupCount :Int = 1;
    
    //MARK: - UIViewController Methods
    override func viewDidLoad() {
        super.viewDidLoad()
        
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
        
        
        //Check that we are connected to bridge.
        if !((UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!.localConnected()){
            //Connect to bridge
            (UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!.enableLocalConnection()
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
        var light = cache?.lights["\(indexPath.row+1)"] as? PHLight
        //        var sdk = ((UIApplication.sharedApplication().delegate) as AppDelegate).hueSDK!
        if(light != nil){
            
            
            cell!.SetBulbLabel(light!.name)
            if(light!.lightState.on == 0){
                cell!.SetBulbImage(false)
            } else{
                //cell!.SetBulbImage(true)
                var point = CGPoint(x: Double(light!.lightState.x), y: Double(light!.lightState.y))
                var color = PHUtilities.colorFromXY(point, forModel: light!.modelNumber)
                //                var color = UIColor(hue: CGFloat(light!.lightState.hue), saturation: CGFloat(light!.lightState.saturation), brightness: CGFloat(light!.lightState.brightness), alpha: 1)
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
        if skipNextHeartbeat{
            skipNextHeartbeat = false;
            return
        }
        
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
        
        if(beenConnected){
            if(retryConnection){
                retryConnection = false
                return
            }
            hueSDK.disableLocalConnection()
            beenConnected = false
            //Create the alert
            var alert = UIAlertController(title: "No Connection", message: "Connection to bridge lost. Insure the bridge is available and your network is working", preferredStyle: UIAlertControllerStyle.Alert)
            let cancelButton = UIAlertAction(title: "Okay", style: UIAlertActionStyle.Cancel) { (cancelButton) -> Void in     }
            alert.addAction(cancelButton)
            //TODO: Add retry connection button.
            //Show the alert to the user
            self.presentViewController(alert, animated: true) { () -> Void in}
        } else{
            hueSDK.disableLocalConnection()
            beenConnected = false
            let bridgeSearch = PHBridgeSearching(upnpSearch: true, andPortalSearch: false, andIpAdressSearch: false)
            
            bridgeSearch.startSearchWithCompletionHandler { (dict:[NSObject : AnyObject]!) -> Void in
                var addresses = dict as [String:String]
                var macAddresses = [String](addresses.keys)
                if(addresses.count == 1){
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
                    //TODO: More though search on a different screen
                    var alert = UIAlertController(title: "No Bridge Found", message: "Please ensure you are connected to the wireless.", preferredStyle: UIAlertControllerStyle.Alert)
                    let cancelButton = UIAlertAction(title: "Okay", style: UIAlertActionStyle.Cancel) { (cancelButton) -> Void in     }
                    alert.addAction(cancelButton)
                    //TODO: Add retry connection button.
                    //Show the alert to the user
                    self.presentViewController(alert, animated: true) { () -> Void in}
                    
                }
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
    
    
}

