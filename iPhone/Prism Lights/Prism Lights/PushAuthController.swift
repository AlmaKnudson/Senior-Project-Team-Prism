//
//  PushAuthController.swift
//  Prism Lights
//
//  Created by Cody Foltz on 12/1/14.
//  Copyright (c) 2014 Prism. All rights reserved.
//

import UIKit

class PushAuthController : UIViewController {

    @IBOutlet weak var pushImageView: UIImageView!
    
    var delegate:DismissPresentedController? = nil
    
    override func viewDidLoad() {
        self.title = "Authenticate"
    }
    //MARK: - UIViewController Methods
    
    override func viewDidAppear(animated: Bool) {
        StartPushLink()
    }
    
    
    func StartPushLink(){
        
        var manager = PHNotificationManager.defaultManager()
        manager!.registerObject(self, withSelector: "AuthSuccessful", forNotification: "PUSHLINK_LOCAL_AUTHENTICATION_SUCCESS_NOTIFICATION")
        
        manager!.registerObject(self, withSelector: "AuthFailed", forNotification: "PUSHLINK_LOCAL_AUTHENTICATION_FAILED_NOTIFICATION")

        manager!.registerObject(self, withSelector: "NoConnection", forNotification: "PUSHLINK_NO_LOCAL_CONNECTION_NOTIFICATION")
        
        manager!.registerObject(self, withSelector: "NoBridgeSet", forNotification: "PUSHLINK_NO_LOCAL_BRIDGE_KNOWN_NOTIFICATION")

        manager!.registerObject(self, withSelector: "ButtonNotPressed", forNotification: "PUSHLINK_BUTTON_NOT_PRESSED_NOTIFICATION")
        
        (UIApplication.sharedApplication().delegate as! AppDelegate).hueSDK!.startPushlinkAuthentication()
    
    }
    
    
    //MARK: - Notification Methods
    func AuthSuccessful(){
        PHNotificationManager.defaultManager().deregisterObjectForAllNotifications(self)
        navigationController?.popViewControllerAnimated(true)
        delegate?.DismissMe()
    }
    
    
    func AuthFailed(){
        PHNotificationManager.defaultManager().deregisterObjectForAllNotifications(self)
        //TODO: AuthFailed Method
        navigationController?.popViewControllerAnimated(true)
        delegate?.DismissMe()
    }
    
    func NoConnection(){
        PHNotificationManager.defaultManager().deregisterObjectForAllNotifications(self)
        //TODO: Inform user of Network connection going down.
        navigationController?.popViewControllerAnimated(true)
        delegate?.DismissMe()
    }
    
    func NoBridgeSet(){
        PHNotificationManager.defaultManager().deregisterObjectForAllNotifications(self)
        //TODO: Provide message to user to fill out bug report
        navigationController?.popViewControllerAnimated(true)
        delegate?.DismissMe()
    }
    
    func ButtonNotPressed(){
        //PHNotificationManager.defaultManager().deregisterObjectForAllNotifications(self)
        //TODO: Provide a timer for amount left to push button
    }
    
}
