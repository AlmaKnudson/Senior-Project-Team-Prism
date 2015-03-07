//
//  AdvancedSettingsController.swift
//  Prism Lights
//
//  Created by Cody Foltz on 2/21/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

class AdvancedSettingsController: UITableViewController {
    
    var bulbId :String?
    var isGroup :Bool?
    
    
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "AlarmsTable" {
            var alarmTable:AlarmsTable = segue.destinationViewController as AlarmsTable
            alarmTable.bulbId = self.bulbId
            alarmTable.isGroup = isGroup
        } 
    }
    
    override func tableView(tableView: UITableView, didDeselectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
    }
    
    
}