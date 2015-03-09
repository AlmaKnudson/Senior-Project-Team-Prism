//
//  MusicCell.swift
//  Prism Lights
//
//  Created by Alma Knudson on 3/8/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

import UIKit


class MusicCell : UICollectionViewCell{
    
    var bulbName: NSString = "noname"
    var bulbIdentifier: NSString = "-1"
    var bulbRange: NSInteger = 1
    
    func SetBulbRange(range:NSInteger){
        bulbRange = range;
    }
    
    
    
}