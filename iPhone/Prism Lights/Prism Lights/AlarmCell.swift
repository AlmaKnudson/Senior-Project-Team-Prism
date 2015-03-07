//
//  AlarmCell.swift
//  Prism Lights
//
//  Created by Cody Foltz on 2/9/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation


import UIKit

class AlarmCell : UITableViewCell{
    
    @IBOutlet weak var alarmTitle: UILabel!
    @IBOutlet weak var enabledLabel: UILabel!
    @IBOutlet weak var enabledSwitch: UISwitch!
    @IBOutlet weak var colorView: UIView!
    @IBOutlet weak var alarmState: UILabel!
    
    
    @IBAction func enableAlarm() {

        if enabledSwitch.on {
            enabledLabel.text = "On"
        } else{
            enabledLabel.text = "Off"
        }
        
        
        
    }
    
    
    func setupView(schedule :PHSchedule){
        alarmTitle.text = schedule.name
        alarmState.text = alarmState.text! + " hello"
    }
    
    
    
    
    
    
}
