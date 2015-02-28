//
//  Alarms.swift
//  Prism Lights
//
//  Created by Cody Foltz on 2/20/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

/**
Creates an alarm for a group of lights

:param: name            Name to give the alarm
:param: lightState      The lightState to set the group to when the alarm goes off
:param: groupIdentifier The group identifier of the group
:param: day             The day
:param: month           The month integer Jan(1) Feb(2) etc
:param: year            The year
:param: hour            The hour
:param: minute          The minute 1-59
:param: second          The second 1-59
*/
func CreateGroupAlarm(name:String, lightState:PHLightState, groupIdentifier:String, day:Int, month:Int, year:Int, hour:Int, minute:Int, second:Int ){
    
    
    // Create a new schedule object
    var newSchedule = PHSchedule()
    
    // Give it a name
    newSchedule.name = name;
    
    // Set schedule to use local time, so you can specify you date in the same timezone as your bridge.
    newSchedule.localTime = true;
    
    // Create a time for when you want to schedule your action. Use NSCalendar and NSDateComponents for this.
    var date:NSDate = NSDate()
    var calendar:NSCalendar = NSCalendar(calendarIdentifier: NSCalendarIdentifierGregorian)!
    var components:NSDateComponents = calendar.components(NSCalendarUnit.CalendarUnitEra, fromDate: date)
    
    // Set components to 21.00 PM
    components.day = day
    components.month = month
    components.year = year
    
    components.hour = hour
    components.minute = minute
    components.second = second
    
    // Set created time
    newSchedule.date = calendar.dateFromComponents(components)
    
    // Set light state to schedule object
    newSchedule.state = lightState;
    
    // Set group identifier to 0, this way all lights have their light state updated
    newSchedule.groupIdentifier = groupIdentifier;
    
    // Create PHBridgeSendAPI object
    var bridgeSendAPI:PHBridgeSendAPI = PHBridgeSendAPI()
    
    // Call create schedule on bridge API
    bridgeSendAPI.createSchedule(newSchedule, completionHandler: { (identifier, errorArray) -> Void in
        if errorArray != nil {
            if DEBUG {
                println("Error creating group schedule")
            }
            //TODO: Better error handling
            return
        }
        
    })

}

/**
Creates an Alarm for a single light bulb

:param: name            The name to give the alarm
:param: lightState      The light state to give the bulb when the alarm goes off
:param: lightIdentifier The identifier of the single bulb
:param: day             The day
:param: month           The month
:param: year            The year
:param: hour            The hour
:param: minute          The minute
:param: second          The second
*/
func CreateSingleLightAlarm(name:String, lightState:PHLightState, lightIdentifier:String, day:Int, month:Int, year:Int, hour:Int, minute:Int, second:Int ){
    
    
    // Create a new schedule object
    var newSchedule = PHSchedule()
    
    // Give it a name
    newSchedule.name = name;
    
    // Set schedule to use local time, so you can specify you date in the same timezone as your bridge.
    newSchedule.localTime = true;
    
    // Create a time for when you want to schedule your action. Use NSCalendar and NSDateComponents for this.
    var date:NSDate = NSDate()
    var calendar:NSCalendar = NSCalendar(calendarIdentifier: NSCalendarIdentifierGregorian)!
    var components:NSDateComponents = calendar.components(NSCalendarUnit.CalendarUnitEra, fromDate: date)
    
    // Set components to 21.00 PM
    components.day = day
    components.month = month
    components.year = year
    
    components.hour = hour
    components.minute = minute
    components.second = second
    
    // Set created time
    newSchedule.date = calendar.dateFromComponents(components)
    
    // Set light state to schedule object
    newSchedule.state = lightState;
    
    // Set group identifier to 0, this way all lights have their light state updated
    newSchedule.lightIdentifier = lightIdentifier;
    
    // Create PHBridgeSendAPI object
    var bridgeSendAPI:PHBridgeSendAPI = PHBridgeSendAPI()
    
    // Call create schedule on bridge API
    bridgeSendAPI.createSchedule(newSchedule, completionHandler: { (identifier, errorArray) -> Void in
        if errorArray != nil {
            if DEBUG {
                println("Error creating single schedule")
            }
            //TODO: Better error handling
            return
        }
        
    })
    
}

/**
Deletes the schedule

:param: schedule The PHSchedule needs to have the schedule identifier
*/
func DeleteAlarm(schedule:PHSchedule){
    
    // Create PHBridgeSendAPI object
    var bridgeSendAPI:PHBridgeSendAPI = PHBridgeSendAPI()
    
    // Call create schedule on bridge API
    bridgeSendAPI.updateScheduleWithSchedule(schedule, completionHandler: { ( errorArray) -> Void in
        if errorArray != nil {
            if DEBUG {
                println("Error deleting group schedule")
            }
            //TODO: Better error handling
            return
        }
        
    })
}


func CreateGroupTimer(){
    
}

func CreateSingleTimer(){
    
}

/**
Gets a list of all the alarms that are for the specified bulb

:param: identifier The single bulb's identifier

:returns: An array of alarms for that bulb.
*/
func AlarmsWithLightIdentifier(identifier :String) -> [PHSchedule]{
    var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
    
    var schedulesList:[PHSchedule] = []
    
    if let schedules = cache.schedules as? [String:PHSchedule]{
        for schedule in schedules{
            if(schedule.1.lightIdentifier? == identifier){
                schedulesList.append(schedule.1)
            }
        }
    }
    
    
    return schedulesList
    
}

/**
Gets a list of all the alarms that are for the specified group of bulbs

:param: identifier The group identifier

:returns: An array of alarms for the group
*/
func AlarmsWithGroupIdentifier(identifier :String) -> [PHSchedule]{
    var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
    
    var schedulesList:[PHSchedule] = []
    
    if let schedules = cache.schedules as? [String:PHSchedule]{
        for schedule in schedules{
            if(schedule.1.groupIdentifier == identifier){
                schedulesList.append(schedule.1)
            }
        }
    }
    
    
    return schedulesList
    
}








