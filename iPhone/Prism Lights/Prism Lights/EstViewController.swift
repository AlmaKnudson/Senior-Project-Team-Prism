//
//  EstViewController.swift
//  Prism Lights
//
//  Created by Alma Knudson on 3/5/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

import UIKit

class EstViewController:UIViewController, ESTBeaconManagerDelegate{
    
    //Create beacon manager instance
    let beaconManager : ESTBeaconManager = ESTBeaconManager()
    var range:NSNumber = 0
    let beaconMA = NSMutableSet()
    var currentBeaconIdentifier: NSString = ""
    
    var bbTrackingArray: Array<BeaconBulbTracking> = []
    var notTracking: Bool = true
    
    @IBOutlet weak var searchingLabel: UILabel!
    
    @IBOutlet weak var distanceLabel: UILabel!
    @IBOutlet weak var distanceSlider: UISlider!
    
    @IBOutlet weak var loadingCircle: UIActivityIndicatorView!
    @IBAction func onDistanceSliderValueChange(sender: UISlider) {
        range = sender.value
        distanceLabel.text = "\(range.integerValue) Feet"
    }
    
    @IBAction func onTrackingDown(sender: UIButton) {
        NSLog("\(sender.titleLabel?.text)")
        if (sender.titleLabel?.text == "Start Tracking") {
            sender.setTitle("Stop Tracking", forState: UIControlState.Normal)
            //            sender.titleLabel?.text = "Stop Tracking"
            notTracking = false
        } else {
            sender.setTitle("Start Tracking", forState: UIControlState.Normal)
            //            sender.titleLabel?.text = "Start Tracking"
            notTracking = true
        }
    }
    //    @IBAction func onTrackingChange(sender: UIButton) {
    //        NSLog("\(sender.titleLabel)")
    //    }
    
    
    override func viewDidLoad(){
        super.viewDidLoad()
        loadingCircle.startAnimating()
        range = distanceSlider.value
        
        
        //set beacon manager delegate
        beaconManager.delegate = self
        
        var beaconRegion : ESTBeaconRegion = ESTBeaconRegion(proximityUUID: NSUUID(UUIDString:"B9407F30-F5F8-466E-AFF9-25556B57FE6D"), identifier: "regionName")
        
        beaconManager.requestWhenInUseAuthorization()
        
        beaconManager.startRangingBeaconsInRegion(beaconRegion)
        
        
        //        beaconManager.startMonitoringForRegion(beaconRegion)
        //        beaconManager.requestStateForRegion(beaconRegion)
        
        //TODO--Additional setup
    }
    
    
    
    
    
    func beaconManager(manager: ESTBeaconRegion, didRangeBeacons: [ESTBeacon], inRegion: ESTBeaconRegion){
        //        println("I've found \(didRangeBeacons.count) beacons in range.")
        
        if(didRangeBeacons.count > 0) {
            //Keeping track of all beacon macAddresses in beaconMA set
            
            for b in didRangeBeacons{
                beaconMA.addObject("\(b.major)_\(b.minor)")
                
            }
            
            //Closest beacon is at index 0
            var closestBeacon:ESTBeacon = didRangeBeacons.first!
            loadingCircle.stopAnimating()
            if (notTracking) {
                currentBeaconIdentifier  = "\(closestBeacon.major)_\(closestBeacon.minor)"
                searchingLabel.text = currentBeaconIdentifier
            } else {
                for b in didRangeBeacons {
                    if ( ("\(b.major)_\(b.minor)") == currentBeaconIdentifier ) {
                        closestBeacon = b
                    }
                }
            }
            
            if ( (closestBeacon.distance).integerValue < range.integerValue/3){
                //update bulbs
                NSLog("Update Light Bulb__\(closestBeacon.distance)___\(range.integerValue)");
            }
        }
    }
    
    
    //    func beaconManager(manager: ESTBeaconManager!, didDetermineState state: CLRegionState, forRegion region: ESTBeaconRegion!) {
    //
    //        if(state == CLRegionState.Inside){
    //
    //        } else {
    //
    //        }
    //    }
    //
    //    func beaconManager(manager: ESTBeaconManager!, didEnterRegion region: ESTBeaconRegion!) {
    //
    //    }
    //
    //    func beaconManager(manager: ESTBeaconManager!, didExitRegion region: ESTBeaconRegion!) {
    //        
    //    }
}
