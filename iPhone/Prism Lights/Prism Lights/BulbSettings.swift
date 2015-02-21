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

class BulbSettingsController : UIViewController, ColorSelectedProtocol{
    
    @IBOutlet weak var nameTextField: UITextField!
    @IBOutlet weak var onSwitch: UISwitch!
    @IBOutlet weak var brightnessPercentLabel: UILabel!
    @IBOutlet weak var brightnessSlider: UISlider!
    
    var brightnessInt :Int? = nil
    var homeDelegate :BulbSettingsProtocol?
    var bulbId :String?
    var isGroup :Bool?

    //MARK - Actions
    @IBAction func onSwitchToggle(sender: UISwitch) {
        if(DEBUG){
            println("On Switch Toggled")
        }
        var lightState = PHLightState()
        lightState.on = sender.on
        var bridgeSend = PHBridgeSendAPI()
        bridgeSend.updateLightStateForId(self.bulbId, withLightState: lightState, completionHandler: nil)
    }
    @IBAction func BrightnessFinished(sender: UISlider) {
        if(DEBUG){
            println("Finished")
        }
        var lightState = PHLightState()
        lightState.brightness = Int(254*sender.value)
        var bridgeSend = PHBridgeSendAPI()
        bridgeSend.updateLightStateForId(self.bulbId, withLightState: lightState, completionHandler: nil)
        
        var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
        var light = (cache.lights?[bulbId!]) as PHLight
        light.lightState.brightness = Int(254*sender.value)
        println(Int(255*sender.value))
        
    }
    
    @IBAction func BrightnessFinishedOutside(sender: UISlider) {
        if(DEBUG){
            println("Outside")
        }
        BrightnessFinished(sender)
    }
    @IBAction func brightnessChanged(sender: UISlider) {
        var value = sender.value
        self.brightnessPercentLabel.text = "\(Int(value*100))%"
        brightnessInt = Int(254*value)
        if(DEBUG){
            println("Changing: \(Int(254*sender.value)) ")
        }
    }
    
    @IBAction func ApplySettings(sender: UIBarButtonItem) {
        homeDelegate?.ApplySettings()
    }
    
    
    //MARK - UIViewController Methods
    override func viewWillAppear(animated: Bool) {
        var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
        
        //Sets the slider to current brightness
        var light = (cache.lights?[bulbId!]) as PHLight
        var lightState = light.lightState
        var brightness = Float(lightState.brightness) / 255
        brightnessSlider.value = brightness
        self.brightnessPercentLabel.text = "\(Int(brightnessSlider.value*100))%"

        //Sets the on-off switch
        if lightState.on == 1{
            onSwitch.on = true
        } else{
            onSwitch.on = false
        }
        
        self.title = light.name
        //Set the Delegate of the Child Controller
        var picker = (self.childViewControllers.last) as ColorPickerView
        picker.delegate = self;
        
        //Set previous color
        var point = CGPoint(x: CGFloat(lightState.x), y: CGFloat(lightState.y))
        var color = PHUtilities.colorFromXY(point, forModel: light.modelNumber)
        picker.colorSelected.backgroundColor = color
    }
    
    
    //MARK - ColorSelectedProtocol Methods
    
    func ColorSelected(color: UIColor) {
        
        var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
        var light = (cache.lights?[bulbId!]) as PHLight
        
        // Convert color red to a XY value
        var point = PHUtilities.calculateXY(color, forModel: light.modelNumber)
        // Create new light state object
        var lightState = PHLightState()
        
        // Set converted XY value to light state
        lightState.x = point.x
        lightState.y = point.y
        // Update light state
        var bridgeSend = PHBridgeSendAPI()
        bridgeSend.updateLightStateForId(self.bulbId, withLightState: lightState, completionHandler: nil)
        
        light.lightState.x = point.x
        light.lightState.y = point.y
    }
    
    
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        
        if segue.identifier == "AlarmsTable" {
            var alarmTable:AlarmsTable = sender as AlarmsTable
            alarmTable.bulbId = self.bulbId
            alarmTable.isGroup = isGroup
        } else if segue.identifier == "CycleColorsTable" {
            
        }
        
    }
    
    
    
    
    
    
    
    
    
    
    
}






























