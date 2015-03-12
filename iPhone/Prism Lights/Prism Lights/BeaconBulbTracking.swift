//
//  BeaconBulbTracking.swift
//  Prism Lights
//
//  Created by Alma Knudson on 3/11/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation


class BeaconBulbTracking: NSObject{
//    var bulbName: NSString
    var bulbIdentifier: NSString
    var beaconRange: Int
    var beaconIdentifier: NSString
//    var turnLightsOn: Bool
    
    override init(){
//        bulbName = "noname"
        bulbIdentifier = "-1"
        beaconRange = 0
        beaconIdentifier = ""
//        turnLightsOn = true
    }
}