//
//  MusicCell.swift
//  Prism Lights
//
//  Created by Alma Knudson on 3/8/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

import UIKit


class MusicCell : UITableViewCell{
    
    @IBOutlet weak var bulbRangeSegment: UISegmentedControl!
    
    
    var bulbName: NSString = "noname"
    var bulbIdentifier: NSString = "-1"
    var bulbRange: NSInteger = 1
    
    func SetBulbRange(range:NSInteger){
        bulbRange = range;
    }
    
    @IBAction func onRangeChanged(sender: UISegmentedControl) {
        bulbRange = sender.selectedSegmentIndex;
        /* 1-->Low
         * 2-->Mid
         * 3-->High
         */
    }
    
    
}



