//
//  AlarmDetail.swift
//  Prism Lights
//
//  Created by Cody Foltz on 2/23/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

class AlarmDetail:UITableViewController {
    
    var alarmName:String?
    var lightState:PHLightState?
    var date:NSDate?
    
    @IBOutlet weak var colorView: UIView!
    @IBOutlet weak var lightOnLabel: UILabel!
    
    
    @IBAction func nameField(sender: UITextField) {
        
        
    }

    @IBAction func LightOnSliderChanged(sender: UISwitch) {
        if(sender.on){
            lightOnLabel.text = "On"
        } else{
            lightOnLabel.text = "Off"
        }
    }
    
    
    @IBAction func brightnessValueChanged(sender: UISlider) {
        
    }
    
    @IBAction func brightnessSliderTouchUpInside(sender: UISlider) {
        
    }
    
    @IBAction func BrightnessTouchUpOutside(sender: UISlider) {
        
    }
    

    
    
    
    
    
    
}


