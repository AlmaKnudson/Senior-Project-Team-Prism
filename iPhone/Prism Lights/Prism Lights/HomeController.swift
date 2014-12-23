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
    
    
    var cache :PHBridgeResourcesCache? = nil;
    var lightCount :Int = 0;
    
    //MARK: - UIViewController Methods
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //Add long press Gesture
        var gesture = UILongPressGestureRecognizer(target: self, action: "ShowBulbSettings:")
        gesture.minimumPressDuration = 1
        gesture.delegate = self
        self.bulbCollectionView.addGestureRecognizer(gesture)
        
        //Get the light count
        cache = PHBridgeResourcesReader.readBridgeResourcesCache()
        lightCount = (cache?.lights.count)!
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
    }
    
    override func viewDidAppear(animated: Bool) {
        bulbCollectionView.reloadData()
    }
    
    override func viewDidDisappear(animated: Bool) {
        PHNotificationManager.defaultManager().deregisterObjectForAllNotifications(self)
        println("Home disappeared")
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        println("In Prepare For Segue")
        var dest = segue.destinationViewController as UINavigationController
        var bulbSettingsController = dest.viewControllers[0] as BulbSettingsController
        bulbSettingsController.homeDelegate = self
        bulbSettingsController.bulbId = "\((sender as NSIndexPath).row+1)"
    }
    
    //MARK: - UICollectionView Methods
    
    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int{
        return lightCount;
//        return 4;
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
                cell!.SetBulbImage(true)
            }
        } else{
            cell!.SetBulbImage(false)
        }
        
        //TODO: Return a correct cell for the view
        return cell!
    }
    
    //TODO: Handle taps
    func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
        println("Bulb tapped")
        var light = cache!.lights["\(indexPath.row+1)"] as PHLight
        var lightState = PHLightState()
        var cell = bulbCollectionView.cellForItemAtIndexPath(indexPath) as BulbCollectionCell
        if light.lightState.on == 1{
            light.lightState.on = false
            lightState.on = false
            cell.SetBulbImage(false)
        } else{
            lightState.on = true
            light.lightState.on = true
            cell.SetBulbImage(true)
        }
        println("This is the hue: \(light.lightState.hue)")
        
        
        
        var bridgeSendAPI = PHBridgeSendAPI()
        bridgeSendAPI.updateLightStateForId(light.identifier, withLightState: lightState, completionHandler: nil)
        
        
        
        
    }

    //TODO: Handle long presses
    func ShowBulbSettings( gestureRecognizer: UILongPressGestureRecognizer){
        
        if(gestureRecognizer.state == UIGestureRecognizerState.Began){
            var point = gestureRecognizer.locationInView(self.bulbCollectionView)
            var indexPath = self.bulbCollectionView.indexPathForItemAtPoint(point)
            if indexPath == nil{
                println("Unable to find index")
            } else{
                println("indexPath of cell: \(indexPath)")
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
        cache = PHBridgeResourcesReader.readBridgeResourcesCache()
        lightCount = (cache?.lights.count)!
                //TODO: Update UI based on new info in the cache.
        self.bulbCollectionView.reloadData()
    }
    
    /**
        Handles the unable to connect bridge PHNotification
    
    :param: void
    
    :returns: void
    */
    func NetworkConnectionLost(){
        var hueSDK = (UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!
        hueSDK.disableLocalConnection()
        //TODO: Notify user of lost network connection
    }
    
    /**
        Handles the not authorized with the bridge PHNotification
    
    :param:
    
    :returns:
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

