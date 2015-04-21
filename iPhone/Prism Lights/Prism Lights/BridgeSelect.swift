//
//  BridgeSelect.swift
//  Prism Lights
//
//  Created by Cody Foltz on 4/18/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

class BridgeSelect: UIViewController, UITableViewDataSource, UITableViewDelegate {
    
    
    var bridges:[String:String] = [:]
    var macs:[String] = []
    
    
    @IBOutlet weak var tableView: UITableView!
    
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
            }
    
    override func viewDidLoad() {
        tableView.dataSource = self
        tableView.delegate = self

    }
    
    
    
    
    //MARK: - UITableView
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cellIdentifier = "bridgeCell"
        var cell = tableView.dequeueReusableCellWithIdentifier(cellIdentifier) as? UITableViewCell
        
        if cell == nil {
            cell = UITableViewCell(style: UITableViewCellStyle.Subtitle, reuseIdentifier: cellIdentifier)
        }
        
        let mac = macs[indexPath.row]
        let ip:String = bridges[mac]!
        cell!.textLabel?.text = mac
        cell!.detailTextLabel!.text = ip
        
        return cell!
        
    }
    
    func tableView(tableView: UITableView, editActionsForRowAtIndexPath indexPath: NSIndexPath) -> [AnyObject]? {
        return []
        
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        return macs.count
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        let mac = macs[indexPath.row]
        let ip = bridges[mac]!
        let appDelegate = UIApplication.sharedApplication().delegate as! AppDelegate
        let hueSDK = appDelegate.hueSDK
        hueSDK!.disableLocalConnection()
        hueSDK!.setBridgeToUseWithIpAddress(ip, macAddress: mac)
        hueSDK!.enableLocalConnection()
        
    }

    
    
    
    
}