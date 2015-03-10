//
//  MusicSelectBulbsViewController.swift
//  Prism Lights
//
//  Created by Alma Knudson on 3/7/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation


protocol BulbRangeSelectionDelegate: class {
    func onBulbRangeChange(identifier: NSString, range:NSNumber)
}



class MusicSelectBulbsViewController: UITableViewController, BulbRangeSelectionDelegate{
    
    var rangeSelectionDelegate: BulbRangeSelectionDelegate?
    
    
    var lightCells: Array<MusicCell> = [];
    var lights: Array<AnyObject> = []
    
    //    var cache:PHBridgeResourcesCache
    var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
    
    @IBOutlet weak var light: UIView!
    
    @IBAction func onRangeChanged(sender: MusicCell) {
//        NSLog("NEW INDEX:\(sender.getSelectedSegmentIndex())")
        
//        sender.SetBulbRange(sender.getSelectedSegmentIndex());
        
        //        if (rangeSelectionDelegate != nil) {
//            rangeSelectionDelegate!.onBulbRangeChange(sender.bulbIdentifier, range: sender.bulbRange)
//        }
    }
    
    func onBulbRangeChange(identifier: NSString, range: NSNumber) {
        
    }
    
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
        var cell:MusicCell = tableView.dequeueReusableCellWithIdentifier(cellId, forIndexPath:indexPath) as MusicCell
        cell.textLabel?.textColor = UIColor.whiteColor()
        cell.bulbIdentifier = (lights[indexPath.row].identifier as String)
        cell.bulbName = (lights[indexPath.row].name as String)
//        cell.bulbRange = 2
        cell.SetBulbRange(2)
        cell.textLabel?.text = (lights[indexPath.row].name as String)
        cell.backgroundColor = UIColor.blackColor()
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        lightCells.insert(cell, atIndex: 0)
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

    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        
    }
    
    
}