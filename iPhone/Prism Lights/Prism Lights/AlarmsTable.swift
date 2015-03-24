//
//  AlarmsTable.swift
//  Prism Lights
//
//  Created by Cody Foltz on 2/9/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

class AlarmsTable: UITableViewController, UITableViewDataSource, UITableViewDelegate {
    
    var bulbId :String?
    var isGroup :Bool?
    var alarms:[PHSchedule]?
    
    //MARK: - UITableViewDataSource Methods
    
    /**
    Gets the number of alarms
    
    :param: tableView Tableview for the alarms
    :param: section   The section
    
    :returns: The number of alarms for that section
    */
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int{
        
        if( isGroup!) {
            alarms = AlarmsWithGroupIdentifier(bulbId!)
        } else{
            alarms = AlarmsWithLightIdentifier(bulbId!)
        }
        
        return alarms!.count
    }
    
    // Row display. Implementers should *always* try to reuse cells by setting each cell's reuseIdentifier and querying for available reusable cells with dequeueReusableCellWithIdentifier:
    // Cell gets various attributes set automatically based on table (separators) and data source (accessory views, editing controls)
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell{
        
        var cell = tableView.dequeueReusableCellWithIdentifier("AlarmCell", forIndexPath: indexPath) as? AlarmCell
        if( cell == nil){
            cell = AlarmCell()
        }
        var x = alarms![indexPath.row]
        cell!.setupView(x)
        
        return cell!
    }

    
    //MARK: - UITableViewDataSource Methods
    
    //MARK: - Segue
    
    
}