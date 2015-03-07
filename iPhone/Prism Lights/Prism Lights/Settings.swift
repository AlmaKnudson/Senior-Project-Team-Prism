//
//  Settings.swift
//  Prism Lights
//
//  Created by Cody Foltz on 2/9/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

class Settings: UITableViewController, UITableViewDataSource, UITableViewDelegate {
    
    //MARK: - UITableViewDataSource Methods
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int{
        
        return 4
    }
    
    // Row display. Implementers should *always* try to reuse cells by setting each cell's reuseIdentifier and querying for available reusable cells with dequeueReusableCellWithIdentifier:
    // Cell gets various attributes set automatically based on table (separators) and data source (accessory views, editing controls)
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell{
        
        var cell = tableView.dequeueReusableCellWithIdentifier("ModeCell", forIndexPath: indexPath) as? ModeCell
        if( cell == nil){
            cell = ModeCell()
        }
        
        
        switch(indexPath.section){
        case 0:
            switch(indexPath.row){
            case 0:
                cell?.modeLabel.text = "Debug Mode"
                if(DEBUG){
                    cell?.enabledLabel.text = "On"
                } else{
                    cell?.enabledLabel.text = "Off"
                }
            case 1:
                cell?.modeLabel.text = "Demo Mode"
                if(DEMO){
                    cell?.enabledLabel.text = "On"
                }else{
                    cell?.enabledLabel.text = "Off"
                }
            case 2:
                cell?.modeLabel.text = "Bridge-less Mode"
                if(BRIDGELESS){
                    cell?.enabledLabel.text = "On"
                }else{
                    cell?.enabledLabel.text = "Off"
                }
            case 3:
                cell?.modeLabel.text = "Bridge Logging Mode"
                if(BRIDGELOGGING){
                    cell?.enabledLabel.text = "On"
                }else{
                    cell?.enabledLabel.text = "Off"
                }
            default:
                fatalError("Only 3 rows for sections 0")
            }
        default:
            fatalError("Only 1 section in Settings.")
        }
        return cell!
    }
    
    
    //MARK: - UITableViewDataSource Methods
    
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath){
        switch(indexPath.section){
        case 0:
            switch(indexPath.row){
                //Debug Mode
            case 0:
                if(DEBUG){
                    DEBUG = false
                    (tableView.cellForRowAtIndexPath(indexPath) as ModeCell).Enabled(false)
                }else{
                    DEBUG = true
                    (tableView.cellForRowAtIndexPath(indexPath) as ModeCell).Enabled(true)
                }
                //Demo Mode
            case 1:
                if(DEMO){
                    DEMO = false
                    (tableView.cellForRowAtIndexPath(indexPath) as ModeCell).Enabled(false)
                }else{
                    DEMO = true
                    (tableView.cellForRowAtIndexPath(indexPath) as ModeCell).Enabled(true)
                }
            case 2:
                if(BRIDGELESS){
                    BRIDGELESS = false
                    (tableView.cellForRowAtIndexPath(indexPath) as ModeCell).Enabled(false)
                }else{
                    BRIDGELESS = true
                    (tableView.cellForRowAtIndexPath(indexPath) as ModeCell).Enabled(true)
                }
            case 3:
                if(BRIDGELOGGING){
                    BRIDGELOGGING = false
                    (tableView.cellForRowAtIndexPath(indexPath) as ModeCell).Enabled(false)
                    (UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!.enableLogging(false)
                }else{
                    BRIDGELOGGING = true
                    (tableView.cellForRowAtIndexPath(indexPath) as ModeCell).Enabled(true)
                    (UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!.enableLogging(true)
                }
            default:
                fatalError("Only 3 rows for sections 0")
            }
        default:
            fatalError("Only 1 section in Settings.")
        }
     
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
    }
    
    
    
    
    
    
    
} //End of class