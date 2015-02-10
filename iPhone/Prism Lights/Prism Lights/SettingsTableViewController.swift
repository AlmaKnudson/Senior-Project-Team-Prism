//
//  SettingsTableViewController.swift
//  Prism Lights
//
//  Created by Cody Foltz on 12/10/14.
//  Copyright (c) 2014 Prism. All rights reserved.
//

import UIKit

class MainSettingsTableViewController: UITableViewController, UITableViewDataSource, UITableViewDelegate {

    override func viewWillAppear(animated: Bool) {
        if(DEBUG){
            println("SettingTableViewController will Appear")
        }
        self.title = "Settings"
    }
    
    
    //MARK: - UITableView
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var cellIdentifier :String
        
        switch indexPath.row{
        case 0:
                cellIdentifier = "bridgeInfo"
            
        case 1:
                cellIdentifier = "settings"
            
        case 2:
                cellIdentifier = "about"
            
        default:
            fatalError("Anything greater than 3 has not been implemented in switch")
        }
        
        var cell = tableView.dequeueReusableCellWithIdentifier(cellIdentifier) as? UITableViewCell
        
        if cell == nil {
            cell = UITableViewCell(style: UITableViewCellStyle.Subtitle, reuseIdentifier: cellIdentifier)
        }
        
        return cell!
        
    }
    
    override func tableView(tableView: UITableView, editActionsForRowAtIndexPath indexPath: NSIndexPath) -> [AnyObject]? {
        return []
        
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        return 3;
    }
override     
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        
    }

    
}