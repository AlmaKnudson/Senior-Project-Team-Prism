//
//  AlarmsTable.swift
//  Prism Lights
//
//  Created by Cody Foltz on 2/9/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

class AlarmTable: UITableViewController, UITableViewDataSource, UITableViewDelegate {
    
    
    
    //MARK: - UITableViewDataSource Methods
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int{
        
        return 0
    }
    
    // Row display. Implementers should *always* try to reuse cells by setting each cell's reuseIdentifier and querying for available reusable cells with dequeueReusableCellWithIdentifier:
    // Cell gets various attributes set automatically based on table (separators) and data source (accessory views, editing controls)
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell{
        
//        var cell = tableView.dequeueReusableCellWithReuseIdentifier("bulb", forIndexPath: indexPath) as? AlarmCell
        var cell = tableView.dequeueReusableCellWithIdentifier("AlarmCell", forIndexPath: indexPath) as? AlarmCell
        if( cell == nil){
            cell = AlarmCell()
        }
        
        
        return cell!

    }

    
    //MARK: - UITableViewDataSource Methods
    
    
}