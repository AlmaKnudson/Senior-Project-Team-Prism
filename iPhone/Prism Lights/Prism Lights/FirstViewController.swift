//
//  FirstViewController.swift
//  Prism Lights
//
//  Created by Cody Foltz on 11/19/14.
//  Copyright (c) 2014 Prism. All rights reserved.
//

import UIKit
let MAX_HUE:UInt32 = 65535

class FirstViewController: UIViewController, UICollectionViewDataSource, UICollectionViewDelegate {
    
    
    //MARK: - UIViewController Methods
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        
    }
    
    
    override func viewDidAppear(animated: Bool) {
        
        
        
//        [Wit sharedInstance].accessToken = "2UZT7OIHBRHNJTFZLOW222ND5SVNRYM7"; // replace xxx by your Wit.AI access token
//        //enabling detectSpeechStop will automatically stop listening the microphone when the user stop talking
//        [Wit sharedInstance].detectSpeechStop = WITVadConfigDetectSpeechStop;
//        return YES;
        
        var manager = PHNotificationManager.defaultManager()
        manager!.registerObject(self, withSelector: "HeartBeatReceived", forNotification: "LOCAL_CONNECTION_NOTIFICATION")
        manager!.registerObject(self, withSelector: "NetworkConnectionLost", forNotification: "NO_LOCAL_CONNECTION_NOTIFICATION")
        manager!.registerObject(self, withSelector: "NotAuthorized", forNotification: "NO_LOCAL_AUTHENTICATION_NOTIFICATION")
        
        if !((UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!.localConnected()){
            //TODO: Popup notification telling user that there is no connection yet...
        } else{
            //TODO: Grab list of bulbs/lights
            var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
            for light in cache.lights.values{
                var lightState = PHLightState()
                
                lightState.hue = 65535
                lightState.brightness = 150
                lightState.saturation = 200
                
                var bridgeSendAPI = PHBridgeSendAPI()
                bridgeSendAPI.updateLightStateForId(light.identifier, withLightState: lightState, completionHandler: nil)
            }
            
        }
        
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
        
        //TODO: Return the correct number of light bulbs
        return 0;
    }
    
    // The cell that is returned must be retrieved from a call to -dequeueReusableCellWithReuseIdentifier:forIndexPath:
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell{
        
        
        //TODO: Return a correct cell for the view
        return BulbCollectionCell()
    }
    
    //TODO: Handle taps and long presses
    
    
    //MARK: Notification Methods
    
    func HeartBeatReceived(){
        //TODO: Update UI based on new info in the cache
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


}
