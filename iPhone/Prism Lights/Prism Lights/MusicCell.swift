//
//  MusicCell.swift
//  Prism Lights
//
//  Created by Alma Knudson on 3/8/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

import UIKit


protocol CellRangeChangeDelegate: class {
    //    func onBulbRangeChange(identifier: NSString?, range:NSNumber?)
    func onCellRangeChanged(cell: MusicCell)
}

class MusicCell : UITableViewCell{
    
    @IBOutlet weak var bulbRangeSegment: UISegmentedControl!
    
    var cellDelegate: CellRangeChangeDelegate? = nil
    
    
    
    var bulbName: NSString = "noname"
    var bulbIdentifier: NSString = "-1"
    var bulbRange: Int = 1
    
    func SetBulbRange(range:NSNumber){
        bulbRange = range.integerValue;
        bulbRangeSegment.selectedSegmentIndex = range.integerValue;
    }
    
    func getSelectedSegmentIndex() -> Int{
        return bulbRangeSegment.selectedSegmentIndex;
    }
    
    @IBAction func onRangeChanged(sender: UISegmentedControl) {
        bulbRange = sender.selectedSegmentIndex;
        self.SetBulbRange(bulbRange);
        
        if(cellDelegate != nil){
            NSLog("NEW Range:\(bulbRange) For bulb:\(bulbName)--Identifier \(bulbIdentifier)")
            cellDelegate?.onCellRangeChanged(self);
        }
        /* 1-->Low
        * 2-->Mid
        * 3-->High
        */
    }
    
    
}



