//
//  BulbSettings.swift
//  Prism Lights
//
//  Created by Cody Foltz on 12/11/14.
//  Copyright (c) 2014 Prism. All rights reserved.
//

import UIKit

protocol ColorSelectedProtocol{
    func ColorSelected(color :UIColor)
}

class BulbSettingsController : UIViewController, ColorChangedDelegate, UITextFieldDelegate {
    
    @IBOutlet weak var nameTextField: UITextField!
    @IBOutlet weak var onSwitch: UISwitch!
    @IBOutlet weak var brightnessPercentLabel: UILabel!
    @IBOutlet weak var brightnessSlider: UISlider!
    
    var brightnessInt :Int = -1
    var homeDelegate :BulbSettingsProtocol?
    var id :String = ""
    var isGroup :Bool = false
    var setupCalled:Bool = false
    var name:String = ""

    
    
    //MARK - Actions
    @IBAction func onSwitchToggle(sender: UISwitch) {
        self.view.endEditing(true)
        if(DEBUG){
            println("On Switch Toggled")
        }
        var lightState = PHLightState()
        lightState.on = sender.on
        if isGroup {
            SetGroupLightState(id, lightState)
        } else {
            SetBulbLightState(id, lightState)
        }
    }
    @IBAction func BrightnessFinished(sender: UISlider) {
        if(DEBUG){
            println("Finished")
        }
        var lightState = PHLightState()
        lightState.brightness = Int(254*sender.value)
        if isGroup {
            SetGroupLightState(id, lightState)
        } else {
            SetBulbLightState(id, lightState)
        }
    }
    
    @IBAction func BrightnessFinishedOutside(sender: UISlider) {
        if(DEBUG){
            println("Outside")
        }
        BrightnessFinished(sender)
    }
    @IBAction func brightnessChanged(sender: UISlider) {
        self.view.endEditing(true)
        var value = sender.value
        var percent = Int(value*100)
        if percent == 0 {
            self.brightnessPercentLabel.text = "1%"
        } else {
            self.brightnessPercentLabel.text = "\(percent)%"
        }
        
        brightnessInt = Int(254*value)
        if(DEBUG){
            println("Changing: \(Int(254*sender.value)) ")
        }
    }
    
    @IBAction func ApplySettings(sender: UIBarButtonItem) {
        var bridgeSend = PHBridgeSendAPI()
        var lights:NSDictionary = PHBridgeResourcesReader.readBridgeResourcesCache().lights as NSDictionary
        var name = nameTextField.text.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceCharacterSet())
        if count(name) > 20 {
            name = name.substringToIndex(advance(name.startIndex, 21))
        }
        if name != ""{
            if isGroup {
                SetGroupName(self.id, name)
            } else {
                SetBulbName(self.id, name)
            }
        }
        homeDelegate?.ApplySettings()
    }
    

    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: NSBundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }
    
    //MARK - Keyboard Methods
    
    func textFieldShouldReturn(textField: UITextField) -> Bool {
        self.view.endEditing(true)
        return false
    }
    
    
    
    
    
    //MARK - UIViewController Methods
    override func viewWillAppear(animated: Bool) {
        self.nameTextField.delegate = self
        if !setupCalled {
            assertionFailure("Setup on BulbSettingsController not called before view Will Appear")
        }
        var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
        
        var lightState:PHLightState
        
        if self.isGroup {
            lightState = GetGroupState(self.id).lightState
        } else{
            lightState = ((cache.lights?[self.id]) as! PHLight).lightState
        }
        
        
        //Sets the slider to current brightness
        var brightness = Float(lightState.brightness) / 254
        brightnessSlider.value = brightness
        self.brightnessPercentLabel.text = "\(Int(brightnessSlider.value*100))%"
        
        //Sets the on-off switch
        if lightState.on == 1{
            onSwitch.on = true
        } else{
            onSwitch.on = false
        }
        
        if self.isGroup && id == "0" {
            nameTextField.userInteractionEnabled = false
        }
        
        self.title = self.name
    }
    

    override func viewDidAppear(animated: Bool) {
        var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
        let lightState:PHLightState
        
        
        if isGroup {
            lightState = GetGroupState(id).lightState
        } else {
            lightState = GetBulbLightState(id)!
        }
        
        
        //Set the Delegate of the Child Controller
        var picker = ((self.childViewControllers.last)?.view) as! ColorPicker
        picker.colorChangedDelegate = self
        
        
        //Set previous color
        var point = CGPoint(x: CGFloat(lightState.x), y: CGFloat(lightState.y))
        picker.color = point
        nameTextField.delegate = self
    }
    
    
    //MARK - ColorSelectedProtocol Methods
    
    func onColorChanged(color: CGPoint) {
        self.view.endEditing(true)
        
        var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
        
        // Create new light state object
        var lightState = PHLightState()
        // Set converted XY value to light state
        lightState.x = color.x
        lightState.y = color.y
        // Update light state
        if isGroup {
            SetGroupLightState(id, lightState)
        } else{
            SetBulbLightState(id, lightState)
        }
    }
    
    
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        
        if segue.identifier == "advancedSettings" {
            var alarmTable:AdvancedSettingsController = segue.destinationViewController as! AdvancedSettingsController
            alarmTable.bulbId = self.id
            alarmTable.isGroup = self.isGroup
        } else if segue.identifier == "CycleColorsTable" {
            
        }
        
    }
    

    func Setup(brightness:Int, id:String, isGroup:Bool, name:String){
        self.setupCalled = true
        
        self.brightnessInt = brightness
        self.id  = id
        self.isGroup = isGroup
        self.name = name
    }
    
    
    
    
    
}






























