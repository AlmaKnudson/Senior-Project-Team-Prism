//
//  MusicSelectBulbsViewController.swift
//  Prism Lights
//
//  Created by Alma Knudson on 3/7/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation


class MusicSelectBulbsViewController: UITableViewController{

    
    var lights: Array<AnyObject> = []
    //    var cache:PHBridgeResourcesCache
    var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
    
    override func viewDidLoad() {
        super.viewDidLoad();
        var index = 0;
        for light in cache!.lights.values{
            //            lights.append(light)
            //            println(light.lightState.description)
            
            if light.lightState.description.rangeOfString("reachable = 1;") != nil{
                //                println("Reachable...")
                lights.insert(light, atIndex: index++)
            }
        }
        
        //        lights = {}
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning();
    }
    
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell
    {
        let cellId:String = "MyCell"
        var cell:UITableViewCell = tableView.dequeueReusableCellWithIdentifier(cellId, forIndexPath:indexPath) as UITableViewCell
        cell.textLabel?.textColor = UIColor.whiteColor()
        cell.textLabel?.text = (lights[indexPath.row].name as String)
        cell.backgroundColor = UIColor.blackColor()
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        return cell
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return lights.count
    }
    
    override func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        
        if editingStyle == .Delete {
//            lights.removeAtIndex([indexPath]);
            lights.removeAtIndex(indexPath.row);
            tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: .Fade)
        } else if editingStyle == .Insert {
            
        }
        
        
        
    }
    
    
}