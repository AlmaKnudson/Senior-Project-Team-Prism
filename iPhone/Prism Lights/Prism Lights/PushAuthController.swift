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
    
    
    func StartPushLink(){
        //pushImageView!.image = UIImage(named: "pushAuth")
        
        
        let manager = PHNotificationManager.defaultManager()
        manager.registerObject(self, withSelector: "AuthSuccessful", forNotification: "PUSHLINK_LOCAL_AUTHENTICATION_SUCCESS_NOTIFICATION")
        
        manager.registerObject(self, withSelector: "AuthFailed", forNotification: "PUSHLINK_LOCAL_AUTHENTICATION_FAILED_NOTIFICATION")
        
        manager.registerObject(self, withSelector: "NoConnection", forNotification: "PUSHLINK_NO_LOCAL_CONNECTION_NOTIFICATION")
        
        manager.registerObject(self, withSelector: "NoBridgeSet", forNotification:         "PUSHLINK_NO_LOCAL_BRIDGE_KNOWN_NOTIFICATION")
        
        manager.registerObject(self, withSelector: "ButtonNotPressed", forNotification:         "PUSHLINK_BUTTON_NOT_PRESSED_NOTIFICATION")
        
        (UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!.startPushlinkAuthentication()
    
    }
    
    func AuthSuccessful(){
        PHNotificationManager.defaultManager().deregisterObjectForAllNotifications(self)
        self.dismissViewControllerAnimated(true, completion: { () -> Void in})
    }
    
    
    
    //Dv4wWL9lxCRo5CYe
    
    
    
    
    func AuthFailed(){
        
    }
    
    func NoConnection(){
        
    }
    
    func NoBridgeSet(){
        
    }
    
    func ButtonNotPressed(){
        
    }
    
    
    
}
