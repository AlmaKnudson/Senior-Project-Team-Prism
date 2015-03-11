//
//  MusicBulbRangeSelection.swift
//  Prism Lights
//
//  Created by Alma Knudson on 3/10/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation


@objc class MusicBulbRangeSelection: NSObject{
    var bulbName: NSString
    var bulbIdentifier: NSString
    var bulbRange: Int
    override init(){
         bulbName = "noname"
         bulbIdentifier = "-1"
         bulbRange = 0
    }
}