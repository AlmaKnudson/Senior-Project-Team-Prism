//
//  BulbSettings.swift
//  Prism Lights
//
//  Created by Cody Foltz on 12/11/14.
//  Copyright (c) 2014 Prism. All rights reserved.
//

import UIKit

class BulbSettingsController : UIViewController{
    
    @IBOutlet weak var nameTextField: UITextField!
    @IBOutlet weak var onSwitch: UISwitch!
    @IBOutlet weak var brightnessPercentLabel: UILabel!
    
    var brightness :Int? = nil
    var homeDelegate :BulbSettingsProtocol?
    var bulbId :String?
    
    

    @IBAction func onSwitchToggle(sender: UISwitch) {
        println("On Switch Toggled")
    }
    @IBAction func BrightnessFinished(sender: UISlider) {
        println("Finished")
    }
    
    @IBAction func brightnessChanged(sender: UISlider) {
        var value = sender.value
        self.brightnessPercentLabel.text = "\(Int(value*100))%"
        brightness = Int(255*value)
    }
    
    @IBAction func ApplySettings(sender: UIBarButtonItem) {
        homeDelegate?.ApplySettings()
    }
    
    
    override func viewWillAppear(animated: Bool) {
        
    }
    
}
