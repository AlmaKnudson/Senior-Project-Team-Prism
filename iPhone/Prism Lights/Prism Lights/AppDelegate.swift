//
//  AppDelegate.swift
//  Prism Lights
//
//  Created by Cody Foltz on 11/19/14.
//  Copyright (c) 2014 Prism. All rights reserved.
//

import UIKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?
    var hueSDK :PHHueSDK?
    var x :PHNotificationManager?
    
    func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject: AnyObject]?) -> Bool {
        
        hueSDK = PHHueSDK()
        hueSDK!.enableLogging(true)
        hueSDK!.startUpSDK()
        hueSDK!.setBridgeToUseWithIpAddress("50.168.199.108", macAddress: "00:17:88:0A:6D:13")
        x = PHNotificationManager.defaultManager()
        x!.registerObject(self, withSelector: "woot", forNotification: "LOCAL_CONNECTION_NOTIFICATION")
        
        x!.registerObject(self, withSelector: "woot2", forNotification: "NO_LOCAL_CONNECTION_NOTIFICATION")
        
        x!.registerObject(self, withSelector: "woot3", forNotification: "NO_LOCAL_AUTHENTICATION_NOTIFICATION")
        
        hueSDK!.enableLocalConnection()
        // Override point for customization after application launch.
        return true
    }
    func woot(){
        println("WOOT")
    }
    
    func woot2(){
        println("#*#*#*#*#*##* Number 2")
    }
    
    func woot3(){
        println("*#*#*#*#*#* Number 3")
    }
    
    

    func applicationWillResignActive(application: UIApplication) {
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
    }

    func applicationDidEnterBackground(application: UIApplication) {
        // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
        // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    }

    func applicationWillEnterForeground(application: UIApplication) {
        // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
    }

    func applicationDidBecomeActive(application: UIApplication) {
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    }

    func applicationWillTerminate(application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    }


}

