//
//  ConnectingViewController.swift
//  Prism Lights
//
//  Created by Cody Foltz on 4/11/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

class ConnectingViewController : UIViewController, DismissPresentedController {
    
    
    //MARK: - Message Strings
    let LAST_BRIDGE_MESSAGE = "Looking for last used Bridge..."
    let SCANNING_MESSAGE = "Searching for a New Bridge..."
    let CONNECTED_MESSAGE = "Connected!"
    var macs:[String] = []
    var bridges:[String:String] = [:]
    
    //MARK: - Properties
    var activityIndicator: BulbActivity!
    
    @IBOutlet weak var connectingMessageLabel: UILabel!
    @IBOutlet weak var rescanButton: UIButton!
    
    @IBAction func scanButtonPressed(sender: UIButton) {
        
        if(BRIDGELESS){
            performSegueWithIdentifier( "HomeTabView", sender: self)
            return
        }
        
        rescanButton.hidden = true
        BeginConnection()
        
        
    }
    
    //MARK: - UIViewController Methods
    
    /**
    Sets the phone status bar to be light colored for dark background
    
    :returns: UIStatusBarStyle
    */
    override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return UIStatusBarStyle.LightContent
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //Button look and feel
        rescanButton.backgroundColor = UIColor.whiteColor()
        rescanButton.layer.cornerRadius = 10
        rescanButton.layer.borderWidth = 2
        rescanButton.layer.borderColor = UIColor.darkGrayColor().CGColor
        
        var frame = CGRect(x: 0, y: 0, width: 50, height: 50)
        activityIndicator = BulbActivity(frame: frame)
        self.view.addSubview(activityIndicator)
        
        
    }
    
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        
    }
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: NSBundle?) {
        super.init(nibName: nil, bundle: nil)
    }
    
    override func viewWillAppear(animated: Bool) {
        activityIndicator.center = self.view.center
    }
    
    override func viewDidAppear(animated: Bool) {
        
        if(BRIDGELESS){
            performSegueWithIdentifier( "HomeTabView", sender: self)
        }
        
        
        
        
        //Subscribe to the notifications about connection
        var manager = PHNotificationManager.defaultManager()
        manager!.registerObject(self, withSelector: "HeartBeatReceived", forNotification: "LOCAL_CONNECTION_NOTIFICATION")
        manager!.registerObject(self, withSelector: "NetworkConnectionLost", forNotification: "NO_LOCAL_CONNECTION_NOTIFICATION")
        manager!.registerObject(self, withSelector: "NotAuthorized", forNotification: "NO_LOCAL_AUTHENTICATION_NOTIFICATION")
        
        //Check for previous bridge connection
        var cache:PHBridgeResourcesCache! = PHBridgeResourcesReader.readBridgeResourcesCache()
        if(cache != nil){
            
            if(cache.bridgeConfiguration == nil || cache.bridgeConfiguration.ipaddress == nil ){
                
                //This line is here so that there is always an IP set if they haven't connected to a bridge in the past.
                (UIApplication.sharedApplication().delegate as! AppDelegate).hueSDK!.setBridgeToUseWithIpAddress("1.1.1.1", macAddress: "ab:ab:ab:ab:ab:ab")
            }
            
        }
        
        BeginConnection()
        
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
        if segue.identifier == "pushAuth" {
            var dest = segue.destinationViewController as! PushAuthController
            dest.delegate = self
        } else if segue.identifier == "bridgeSelect" {
            var dest = segue.destinationViewController as! BridgeSelect
            dest.macs = macs
            dest.bridges = bridges
        }
    }
    //MARK: - Notification Methods
    
    /**
    Handles the heartbeat event from the PHNotification
    
    :param:
    
    :returns:
    */
    func HeartBeatReceived(){
        performSegueWithIdentifier( "HomeTabView", sender: self)
    }
    
    /**
    
    Handles the not authorized with the bridge PHNotification
    
    :param: void
    
    :returns: void
    
    */
    func NotAuthorized(){
        var hueSDK = (UIApplication.sharedApplication().delegate as! AppDelegate).hueSDK!
        self.performSegueWithIdentifier("pushAuth", sender: self)
    }
    /**
    Handles the unable to connect bridge PHNotification
    
    :param: void
    
    :returns: void
    */
    func NetworkConnectionLost(){
        //Unable to connect to the bridge
        
        var hueSDK = (UIApplication.sharedApplication().delegate as! AppDelegate).hueSDK!
        //returns for one more heartbeat timer
        hueSDK.disableLocalConnection()
        
        SearchForBridge(true, false, false, SearchFinished)
        
    }
    
    
    //MARK: - Helper Methods
    
    
    
    func BeginConnection(){
        //Check that we are connected to bridge.
        if !((UIApplication.sharedApplication().delegate as! AppDelegate).hueSDK!.localConnected()){
            //Connect to bridge
            (UIApplication.sharedApplication().delegate as! AppDelegate).hueSDK!.enableLocalConnection()
        }
        activityIndicator.StartActivityIndicator()
        
        
        
    }
    
    
    func SearchFinished(dict:[NSObject:AnyObject]!){
        activityIndicator.StopActivityIndicator()
        var addresses = dict as! [String:String]
        var macAddresses = [String](addresses.keys)
        if(addresses.count == 1){
            //will not be nil since I checked that the count is one
            var mac = macAddresses.first!
            var ipaddress = addresses[mac]!
            SingleBridgeFound(mac, ip: ipaddress)
        } else if(addresses.count > 1){
            MultipleBridgesFound(addresses)
        } else{
            NoBridgesFound()
        }
    }
    
    func SingleBridgeFound(mac:String, ip:String){
        var hueSDK = (UIApplication.sharedApplication().delegate as! AppDelegate).hueSDK!
        hueSDK.setBridgeToUseWithIpAddress(ip, macAddress: mac)
        hueSDK.enableLocalConnection()
    }
    
    func MultipleBridgesFound(dict:[String:String]){
        //TODO: Present choices
        bridges = dict
        macs = dict.keys.array
        
        var alert = UIAlertController(title: "1+ Bridges Found", message: "Select a Bridge to make a connection.", preferredStyle: UIAlertControllerStyle.Alert)
        let button = UIAlertAction(title: "Select a Bridge", style: UIAlertActionStyle.Default) {
            (Button) -> Void in
            self.performSegueWithIdentifier("bridgeSelect", sender: self)
        }
        let cancelButton = UIAlertAction(title: "Don't Connect", style: UIAlertActionStyle.Cancel) { (cancelButton) -> Void in     }
        alert.addAction(button)
        alert.addAction(cancelButton)
        //Show the alert to the user
        self.presentViewController(alert, animated: true) { () -> Void in}
    }
    
    func NoBridgesFound(){
        var alert = UIAlertController(title: "No Bridge Found", message: "Please ensure you are connected to the wireless.", preferredStyle: UIAlertControllerStyle.Alert)
        let cancelButton = UIAlertAction(title: "Okay", style: UIAlertActionStyle.Cancel) { (cancelButton) -> Void in     }
        alert.addAction(cancelButton)
        //Show the alert to the user
        self.presentViewController(alert, animated: true) { () -> Void in}
        rescanButton.hidden = false
    }
    
    func DismissMe(){
        self.dismissViewControllerAnimated(true, completion: nil)
    }
    
    
    
    
    
}





func SearchForBridge(upnpSearch:Bool, portalSearch:Bool, ipAddressSearch:Bool, searchFinishedFunc:([NSObject:AnyObject]!)->()){
    
    let bridgeSearch = PHBridgeSearching(upnpSearch: upnpSearch, andPortalSearch: portalSearch, andIpAdressSearch: ipAddressSearch)
    bridgeSearch.startSearchWithCompletionHandler(searchFinishedFunc)
    
}



/*




self.loadingView.addSubview(activityIndicator)
activityIndicator.center = self.loadingView.center

*/