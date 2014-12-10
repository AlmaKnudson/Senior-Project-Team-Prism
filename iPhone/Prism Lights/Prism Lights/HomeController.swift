//
//  HomeController.swift
//  Prism Lights
//
//  Created by Cody Foltz on 11/19/14.
//  Copyright (c) 2014 Prism. All rights reserved.
//

import UIKit
let MAX_HUE:UInt32 = 65535

class HomeController: UIViewController, UICollectionViewDataSource, UICollectionViewDelegate {

    @IBOutlet weak var bulbCollectionView: UICollectionView!
    
    
    var cache :PHBridgeResourcesCache? = nil;
    var lightCount :Int = 0;
    
    //MARK: - UIViewController Methods
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
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
            cache = PHBridgeResourcesReader.readBridgeResourcesCache()
            lightCount = (cache?.lights.count)!
        }
    }
    
    
    /**
    Lorem ipsum dolor sit amet.
    
    :param: bar Consectetur adipisicing elit.
    
    :returns: Sed do eiusmod tempor.
    */ 
    override func viewDidAppear(animated: Bool) {
        
        
        
        
    }
    
    
    override func viewDidDisappear(animated: Bool) {
        PHNotificationManager.defaultManager().deregisterObjectForAllNotifications(self)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //MARK: - UICollectionView Methods
    
    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int{
        return lightCount;
    }
    
    // The cell that is returned must be retrieved from a call to -dequeueReusableCellWithReuseIdentifier:forIndexPath:
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell{
        
        var cell = bulbCollectionView.dequeueReusableCellWithReuseIdentifier("bulb", forIndexPath: indexPath) as? BulbCollectionCell
        if( cell == nil){
            cell = BulbCollectionCell()
        }
        cell!.SetBulbLabel("asjfdas")
        
        //TODO: Return a correct cell for the view
        return cell!
    }
    
    //TODO: Handle taps and long presses
    
    
    //MARK: Notification Methods
    
    func HeartBeatReceived(){
        cache = PHBridgeResourcesReader.readBridgeResourcesCache()
        lightCount = (cache?.lights.count)!
                //TODO: Update UI based on new info in the cache.
        self.bulbCollectionView.reloadData()
    }
    
    func NetworkConnectionLost(){
        var hueSDK = (UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!
        hueSDK.disableLocalConnection()
        //TODO: Notify user of lost network connection
        
    }
    
    func NotAuthorized(){
        var hueSDK = (UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!
        hueSDK.disableLocalConnection()
        //TODO: Notify user of lost Authorization
    }
    
    
    
    /*
    
    for light in cache!.lights.values{
    var lightState = PHLightState()
    
    lightState.hue = 65535
    lightState.brightness = 150
    lightState.saturation = 200
    
    var bridgeSendAPI = PHBridgeSendAPI()
    bridgeSendAPI.updateLightStateForId(light.identifier, withLightState: lightState, completionHandler: nil)
    }
    */


}

