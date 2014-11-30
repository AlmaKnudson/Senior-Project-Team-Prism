//
//  BridgeConnect.swift
//  Prism Lights
//
//  Created by Cody Foltz on 11/29/14.
//  Copyright (c) 2014 Prism. All rights reserved.
//

import UIKit

class BridgeConnect: UIViewController {
    
    @IBOutlet weak var IP_Label: UILabel!
    @IBOutlet weak var macLabel: NSLayoutConstraint!
    @IBOutlet weak var heartbeatLabel: NSLayoutConstraint!
    
    var dictionary:[String:String]?
    
    @IBAction func SearchForBridge(sender: AnyObject) {
//        let appDelegate = UIApplication.sharedApplication().delegate as AppDelegate
//        let hueSDK = appDelegate.hueSDK

        let bridgeSearch = PHBridgeSearching(upnpSearch: true, andPortalSearch: true, andIpAdressSearch: true)

        
        bridgeSearch.startSearchWithCompletionHandler { ([NSObject : AnyObject]!) -> Void in
            
            //Convert returned obj-C dictionary to strong typed swift dictionary
            // I believe the type is string string
        }
        
        
    }
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        
        
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    
}