//
//  EstViewController.swift
//  Prism Lights
//
//  Created by Alma Knudson on 3/5/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

import UIKit

class EstViewController:UITableViewController, ESTBeaconManagerDelegate{
    
    //Create beacon manager instance
    let beaconManager : ESTBeaconManager = ESTBeaconManager()
    
    override func viewDidLoad(){
        super.viewDidLoad()
        
        //set beacon manager delegate
        beaconManager.delegate = self
        var beaconRegion : ESTBeaconRegion = ESTBeaconRegion(proximityUUID: NSUUID(UUIDString:"B9407F30-F5F8-466E-AFF9-25556B57FE6D"), identifier: "regionName")
        
        
        beaconManager.requestWhenInUseAuthorization();
        
        
        beaconManager.startRangingBeaconsInRegion(beaconRegion);
    
        println("Starting ranging...")
        //TODO--Additional setup
    }
    
    
    func beaconManager(manager: ESTBeaconRegion, didRangeBeacons: [ESTBeacon], inRegion: ESTBeaconRegion){
        println("I've found \(didRangeBeacons.count) beacons in range.")
//        var cell:UITableViewCell = self.tableView.dequeueReusableCellWithIdentifier("cell") as UITableViewCell
        
//        cell.textLabel?.text = self.items[0]
        
//        return cell
    }
    
//    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
//      
//        
//    }
    
    
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        //Dispose of any resources that can be recreated.
    }
}
