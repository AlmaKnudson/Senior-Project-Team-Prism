//
//  ModeCell.swift
//  Prism Lights
//
//  Created by Cody Foltz on 2/9/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation


class ModeCell: UITableViewCell{
    
    @IBOutlet weak var modeLabel: UILabel!
    @IBOutlet weak var enabledLabel: UILabel!
    
    
    func Enabled(enabled:Bool){
        if(enabled){
            enabledLabel.text = "On"
        } else{
            enabledLabel.text = "Off"
        }
        
    }
    
}