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



class MusicSelectBulbsViewController: UITableViewController{
    var delegate :BulbRangeSelectionDelegate? = nil
//    weak var bulbRangeSelectionDelegate: BulbRangeSelectionDelegate?
    
    var rangeSelectionDelegate: BulbRangeSelectionDelegate? = nil
    
    
    
    var lights: Array<AnyObject> = []
    
    //    var cache:PHBridgeResourcesCache
    var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
    
    @IBOutlet weak var light: UIView!
    
    @IBAction func onRangeChanged(sender: MusicCell) {
        
//        if (rangeSelectionDelegate != nil) {
//            rangeSelectionDelegate!.onBulbRangeChange(sender.bulbIdentifier, range: sender.bulbRange)
//        }
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
    
    /*
    - (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    if ([segue.identifier isEqualToString:@"musicLights"]) {
    NSLog(@"FUCK");
    //        NSIndexPath *indexPath = [self.tableView indexPathForSelectedRow];
    MusicSelectBulbsViewController *destViewController = segue.destinationViewController;
    
    //        destViewController
    //    BulbRangeSelectionDelegate *selectBulbs = [[[self.childViewControllers lastObject] ] bulbRangeSelectionDelegate];
    //    selectBulbs.BulbRangeSelectionDelegate = self;
    //        destViewController.lights = @[@"Alfred"];
    //        destViewController.recipeName = [recipes objectAtIndex:indexPath.row];
    destViewController.lights = @[@"Alpaca", @"Llama", @"Kangaroo", @"4", @"5"];
    }
    }
*/
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        
        //NOt so sure I need this.
    }
    
    
}