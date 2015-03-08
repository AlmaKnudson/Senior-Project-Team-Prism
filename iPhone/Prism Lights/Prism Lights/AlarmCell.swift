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
        
        alarmState.text = ""
        var powerState = ""
        var lightState = schedule.state
        if(lightState.on! == 1){
            powerState = "On @ "
        } else{
            powerState = "Off @ "
        }
        
        var time = "Time"
        var date = schedule.date
        
        var dateFormatter = NSDateFormatter()
        var string = "yyyy-MM-dd 'at' HH:mm"
        dateFormatter.dateFormat = string
        time = dateFormatter.stringFromDate(date)
      
        alarmState.text = powerState + time
        
        
    }
    
    
    
    
    
    
}
