//
//  BridgeConnect.swift
//  Prism Lights
//
//  Created by Cody Foltz on 11/29/14.
//  Copyright (c) 2014 Prism. All rights reserved.
//

import UIKit

class BridgeConnect: UIViewController, UITableViewDataSource, UITableViewDelegate {
    
    //MARK: - Class Vars
    @IBOutlet weak var ipLabel: UILabel!
    @IBOutlet weak var macLabel: UILabel!
    @IBOutlet weak var heartbeatLabel: UILabel!

    @IBOutlet weak var blur: UIVisualEffectView!
    @IBOutlet weak var loadingView: UIActivityIndicatorView!
    @IBOutlet weak var bridgesFound: UITableView!
    var addresses:[String:String] = [:]
    var macAddresses :[String] = []
    
    
    // MARK: - Actions
    @IBAction func SearchForBridge(sender: AnyObject) {
        self.blur.hidden = false
        loadingView.startAnimating()
        self.SearchForBridge()
    }
    
    
    
    //MARK: - UIViewController
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        self.title = "Bridge Information"
    }
    
    override func viewDidDisappear(animated: Bool) {
        PHNotificationManager.defaultManager().deregisterObjectForAllNotifications(self)
    }
    
    override func viewWillAppear(animated: Bool) {
        var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
        var hueSDK = (UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!
        if(hueSDK.localConnected()){
            LoadBridgeValues()
        }
    }
    
    override func viewDidAppear(animated: Bool) {
        var manager = PHNotificationManager.defaultManager()
        manager!.registerObject(self, withSelector: "ConnectedToBridge", forNotification: "LOCAL_CONNECTION_NOTIFICATION")
        manager!.registerObject(self, withSelector: "BridgeUnavailable", forNotification: "NO_LOCAL_CONNECTION_NOTIFICATION")
        manager!.registerObject(self, withSelector: "NotAuthorized", forNotification: "NO_LOCAL_AUTHENTICATION_NOTIFICATION")
        
        (UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!.enableLocalConnection()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func LoadBridgeValues(){
        let cache = PHBridgeResourcesReader.readBridgeResourcesCache()
        
        if (cache != nil && cache.bridgeConfiguration != nil && cache.bridgeConfiguration.ipaddress != nil){
            let ip = cache.bridgeConfiguration.ipaddress
            let mac = cache.bridgeConfiguration.mac
            var lastHeartbeat :String
            if (UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!.localConnected(){
                let formatter = NSDateFormatter()
                formatter.dateStyle = NSDateFormatterStyle.NoStyle
                formatter.timeStyle = NSDateFormatterStyle.MediumStyle
                lastHeartbeat = formatter.stringFromDate(NSDate())
            } else{
                lastHeartbeat = "Waiting..."
            }
            ipLabel.text = ip
            macLabel.text = mac
            heartbeatLabel.text = lastHeartbeat
            
        }
        
        
    }

    
    //MARK: - UITableView
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cellIdentifier = "bridgeCell"
        var cell = tableView.dequeueReusableCellWithIdentifier(cellIdentifier) as? UITableViewCell
        
        if cell == nil {
            cell = UITableViewCell(style: UITableViewCellStyle.Subtitle, reuseIdentifier: cellIdentifier)
        }
        
        let mac = macAddresses[indexPath.row]
        let ip:String = addresses[mac]!
        cell!.textLabel?.text = mac
        cell!.detailTextLabel!.text = ip
        
        return cell!
        
    }
    
    func tableView(tableView: UITableView, editActionsForRowAtIndexPath indexPath: NSIndexPath) -> [AnyObject]? {
        return []
        
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        return macAddresses.count
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        let mac = macAddresses[indexPath.row]
        let ip = addresses[mac]!
        let appDelegate = UIApplication.sharedApplication().delegate as AppDelegate
        let hueSDK = appDelegate.hueSDK
        hueSDK!.disableLocalConnection()
        hueSDK!.setBridgeToUseWithIpAddress(ip, macAddress: mac)
        hueSDK!.enableLocalConnection()
        
    }
    
    //MARK: - HueSDK Methods
    
    func ConnectedToBridge(){
        println("Connected to Bridge")
        LoadBridgeValues()
    }
    
    func BridgeUnavailable(){
        (UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!.disableLocalConnection()
        ipLabel.text = "Not Connected"
        macLabel.text = "Not Connected"
        heartbeatLabel.text = "Not Connected"
        
        //TODO: No alert when starting for first time
        
        var alert = UIAlertController(title: "No Connection", message: "Unable to connect to bridge. Insure the bridge is available and your network is working", preferredStyle: UIAlertControllerStyle.Alert)
        let cancelButton = UIAlertAction(title: "Okay", style: UIAlertActionStyle.Cancel) { (cancelButton) -> Void in     }
        alert.addAction(cancelButton)
        self.presentViewController(alert, animated: true) { () -> Void in}
        
        
    }
    
    func NotAuthorized(){
        (UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!.disableLocalConnection()
        
        self.performSegueWithIdentifier("pushAuth", sender: self)

    }
    
    
    func SearchForBridge(){
        let bridgeSearch = PHBridgeSearching(upnpSearch: true, andPortalSearch: true, andIpAdressSearch: true)
        bridgeSearch.startSearchWithCompletionHandler { (dict:[NSObject : AnyObject]!) -> Void in
            self.addresses = dict as [String:String]
            self.macAddresses = [String](self.addresses.keys)
            self.bridgesFound.reloadData()
            self.loadingView.stopAnimating()
            self.blur.hidden = true
        }

    }
    
    
    
}