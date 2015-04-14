//
//  AlarmDetail.swift
//  Prism Lights
//
//  Created by Cody Foltz on 2/23/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

class AlarmDetail:UITableViewController, ColorChangedDelegate {
    
    var alarmName:String?
    var lightState:PHLightState?
    var date:NSDate?
    let dateSection = 2
    let datePickerIndex = 1
    let datePickerHeight:CGFloat = 175.0
    var datePickerShowing = false
    var schedule :PHSchedule?
    

    //MARK - Outlets
    
    @IBOutlet weak var lightOnLabel: UILabel!
    @IBOutlet weak var datePicker: ColoredDatePicker!
    @IBOutlet weak var colorButton: UIButton!
    @IBOutlet weak var addAlarm: UIBarButtonItem!
    
    
    //MARK - Events
    
    @IBAction func nameField(sender: UITextField) {
        
        
    }
    
    @IBAction func LightOnSliderChanged(sender: UISwitch) {
        if(sender.on){
            lightOnLabel.text = "On"
        } else {
            lightOnLabel.text = "Off"
        }
    }
    
    @IBAction func brightnessValueChanged(sender: UISlider) {
        
    }
    
    @IBAction func brightnessSliderTouchUpInside(sender: UISlider) {
        
    }
    
    @IBAction func BrightnessTouchUpOutside(sender: UISlider) {
        
    }
    
    func SetBrightness(brightness:Int){
        
    }
    
    override func viewWillAppear(animated: Bool) {
        self.datePicker.hidden = true
    }
    
    override func viewDidAppear(animated: Bool) {
        
    }
    
    
    //MARK: - Tableview Methods
    
    /**
    Sets the height of the date picker.
    
    :param: tableView The table view containing the alarm details
    :param: indexPath The index of each cell
    
    :returns: The height of the cell
    */
    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        var height = self.tableView.rowHeight;
        if indexPath.section == dateSection {
            if indexPath.row == datePickerIndex {
                if(self.datePickerShowing){
                    height = datePickerHeight
                } else {
                    height = 0.0;
                }
            }
        }
        return height;
    }
    
    
    /**
    Used to start the showing and hiding of the date pickers
    Refer to the story boards for the static cells
    
    :param: tableView The table
    :param: indexPath The index of the cells
    */
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        
        if indexPath.section == dateSection {
            if(indexPath.row == datePickerIndex-1){
                if self.datePickerShowing {
                    HideDatePickerCell()
                } else {
                    ShowDatePickerCell()
                }
            } else {
                HideDatePickerCell()
            }
        } else{
            HideDatePickerCell()
        }
        self.tableView.deselectRowAtIndexPath(indexPath, animated: true)
    }
    
    
    
    //MARK - Date Picker Helper Functions
    
    /**
        Makes the date picker shown to the user.
    */
    func ShowDatePickerCell() {
        self.datePickerShowing = true
        self.tableView.beginUpdates()
        
        self.datePicker.hidden = false
        self.datePicker.alpha = 0.0
        
        UIView.animateWithDuration(0.25, animations: {
            () -> () in
            self.datePicker.alpha = 1
        })
        self.tableView.endUpdates()
    }
    
    
    /**
        Hides the date picker from the user.
    */
    func HideDatePickerCell(){
        
        if !self.datePickerShowing {
            return
        }
        
        self.datePickerShowing = false
        self.tableView.beginUpdates()
        
        UIView.animateWithDuration(0.25, animations: {
            () -> () in
            self.datePicker.alpha = 0.0
            }, completion: {
                (finished) -> () in
                self.datePicker.hidden = true
        })
        self.tableView.endUpdates()
    }
    
    
    
    //MARK - ColorPicker Methods
    
    
    /**
        Protocol method the receives the x,y color from the color picker
    
        :param: color The x,y color
    */
    func onColorChanged(color: CGPoint){
        
    }
    
    
    /**
        Prepares to transition to the Color Picker
    
        :param: segue  The segue to the next controller
        :param: sender The object that started the transition
    */
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "colorPickerController" {
            var colorPicker = segue.destinationViewController as! ColorPickerController
            colorPicker.colorChangedDelegate = self
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}


