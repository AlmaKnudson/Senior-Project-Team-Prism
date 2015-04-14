//
//  MusicSelectBulbsViewController.swift
//  Prism Lights
//
//  Created by Alma Knudson on 3/7/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation


@objc protocol BulbRangeSelectionDelegate: class {
//    func onBulbRangeChange(identifier: NSString?, range:Int?)
    func onBulbRangeChange(cell:MusicCell)
    func thisIsATest()
    
}



@objc class MusicSelectBulbsViewController: UITableViewController, CellRangeChangeDelegate{
    
    var rangeSelectionDelegate: BulbRangeSelectionDelegate? = nil

    var lights: Array<MusicBulbRangeSelection> = []
    
    var moses: NSMutableArray = []
    

    

    
    //    var cache:PHBridgeResourcesCache
    var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
    
    @IBOutlet weak var light: UIView!
    
//    @IBAction func onRangeChanged(sender: UISegmentedControl) {
//
//        sender.
//        NSLog("UMMM--onRangeChanged---TableView");
//        if(rangeSelectionDelegate != nil) {
//            NSLog("UMMM--onRangeChanged---TableView");
////            rangeSelectionDelegate?.onBulbRangeChange("test" as NSString, range: 0 as NSNumber)
//            rangeSelectionDelegate?.thisIsATest()
////            rangeSelectionDelegate?.onBulbRangeChange(sender.bulbIdentifier, range: sender.bulbRange)
//        }
//    }
    
    
    override func viewDidLoad() {
        super.viewDidLoad();
        var index = 0;
        for holy in moses {
            lights.insert(holy as! MusicBulbRangeSelection, atIndex: index++)
        }
        
   
        
//        for light in cache.lights.values{
//            //            lights.append(light)
//            //            println(light.lightState.description)
//
//            if (light.lightState.description.rangeOfString("reachable = 1;") != nil){
//                //                println("Reachable...")
//                lights.insert(light, atIndex: index++)
//            }
////            lights.insert(light, atIndex: index++)
//        }
        
        //        lights = {}
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning();
    }
    
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell
    {
        let cellId:String = "MyCell"
        var cell:MusicCell = tableView.dequeueReusableCellWithIdentifier(cellId, forIndexPath:indexPath) as! MusicCell
        cell.textLabel?.textColor = UIColor.whiteColor()
        cell.bulbIdentifier = (lights[indexPath.row].bulbIdentifier as String)
        cell.bulbName = (lights[indexPath.row].bulbName as String)
        //        cell.bulbRange = 2
        cell.SetBulbRange(lights[indexPath.row].bulbRange)
        cell.textLabel?.text = (lights[indexPath.row].bulbName as String)
        cell.backgroundColor = UIColor.blackColor()
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.cellDelegate = self
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
    
    
    
    func onCellRangeChanged(cell: MusicCell) {
        NSLog("On Cell Range Changed--This is interesting");
//        rangeSelectionDelegate?.onBulbRangeChange(cell.bulbIdentifier, range: cell.bulbRange)
        rangeSelectionDelegate?.onBulbRangeChange(cell)
    }
    
}