//
//  Lights.swift
//  Prism Lights
//
//  Created by Cody Foltz on 3/22/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation


//MARK - Individual Bulb Methods

func GetBulbName(bulbId:String) -> String? {
    
    let cache:PHBridgeResourcesCache = PHBridgeResourcesReader.readBridgeResourcesCache()
    
    let lights = cache.lights as! [String:PHLight]
    let light:PHLight? = lights[bulbId]
    let name:String? = light?.name
    
    return name
}

func SetBulbName(bulbId:String, name:String) {
    
    let cache:PHBridgeResourcesCache = PHBridgeResourcesReader.readBridgeResourcesCache()
    let lights = cache.lights as! [String:PHLight]
    let light:PHLight? = lights[bulbId]
    
    if light != nil {
        light!.name = name
        var bridgeSendAPI = PHBridgeSendAPI()
        bridgeSendAPI.updateLightWithLight(light){
            error -> Void in
            if error != nil {
                if(DEBUG){
                    println("Error updating light state.")
                }
                return
            }
        
        }
    }
}


func GetBulbColorType(bulbId:String) -> PHLightColormode? {
    let cache:PHBridgeResourcesCache = PHBridgeResourcesReader.readBridgeResourcesCache()
    let lights = cache.lights as! [String:PHLight]
    let light:PHLight? = lights[bulbId]
    
    let colorMode:PHLightColormode? = light?.lightState?.colormode
    
    return colorMode
}

func GetBulbColorXY(bulbId:String) -> (x:Double?, y:Double?){
    let cache:PHBridgeResourcesCache = PHBridgeResourcesReader.readBridgeResourcesCache()
    let lights = cache.lights as! [String:PHLight]
    let light:PHLight? = lights[bulbId]
    
    let x:Double? = light?.lightState?.x.doubleValue
    let y:Double? = light?.lightState?.y.doubleValue
    
    return (x,y)
}

func GetBulbIsReachable(bulbId:String) -> Bool? {
    let cache:PHBridgeResourcesCache = PHBridgeResourcesReader.readBridgeResourcesCache()
    let lights = cache.lights as! [String:PHLight]
    let light:PHLight? = lights[bulbId]
    let isReachable = light?.lightState?.reachable.boolValue
    
    return isReachable

}

func GetBulbUIColor(bulbId:String) -> UIColor? {
    let cache:PHBridgeResourcesCache = PHBridgeResourcesReader.readBridgeResourcesCache()
    let lights = cache.lights as! [String:PHLight]
    let light:PHLight? = lights[bulbId]
    let lightState:PHLightState! = light?.lightState
    
    if( lightState == nil ){
        return nil
    }
    var xy = CGPoint(x: lightState.x.doubleValue, y: lightState.y.doubleValue)
    return PHUtilities.colorFromXY(xy, forModel: light!.modelNumber)
}

func IsBulbOn(bulbId:String) -> Bool {
    let cache:PHBridgeResourcesCache = PHBridgeResourcesReader.readBridgeResourcesCache()
    let lights = cache.lights as! [String:PHLight]
    let light:PHLight? = lights[bulbId]
    
    if light == nil{
        return false
    }
    
    return light!.lightState.on.boolValue
}

func GetBulbLightState(bulbId:String) -> PHLightState? {
    let cache:PHBridgeResourcesCache = PHBridgeResourcesReader.readBridgeResourcesCache()
    let lights = cache.lights as! [String:PHLight]
    let light:PHLight? = lights[bulbId]
    
    if light == nil{
        return nil
    }
    
    return light!.lightState
}


func SetBulbLightState(bulbId:String, lightState:PHLightState){
    
    var bridgeSendAPI = PHBridgeSendAPI()
    bridgeSendAPI.updateLightStateForId(bulbId, withLightState: lightState){
        error -> Void in
        if error != nil {
            if(DEBUG){
                println("Error updating light state.")
            }
            return
        }
        
    }
}

func SearchForNewLightsAuto(){
    var bridgeSendAPI = PHBridgeSendAPI()
    bridgeSendAPI.searchForNewLights(){
        error -> Void in
        if error != nil {
            if(DEBUG){
                println("Could not find new lights")
            }
            return
        }
        
    }
}


//MARK: - Group Methods

func GetGroupPHGroup(groupId:String) -> PHGroup? {
    
    let cache:PHBridgeResourcesCache = PHBridgeResourcesReader.readBridgeResourcesCache()
    let groups = cache.groups as! [String:PHGroup]
    let group:PHGroup! = groups[groupId]
    return group
}

func SetGroupLightState(groupId:String, lightState:PHLightState){
    var bridgeSendAPI = PHBridgeSendAPI()
    bridgeSendAPI.setLightStateForGroupWithId(groupId, lightState: lightState){
        error -> Void in
        if error != nil {
            if(DEBUG){
                println("Error updating light state.")
            }
            return
        }
    }
}

func GetBulbsInGroup(groupId:String) -> [String] {
    
    //make sure the cache is ready to go
    let cache:PHBridgeResourcesCache = PHBridgeResourcesReader.readBridgeResourcesCache()
    if cache.groups == nil {
        return [String]()
    }
    
    //Group 0 is never in the cache.  Group 0 is all bulbs
    if groupId == "0" {
        var bulbs = [String]()
        //Make sure the lights in the cache have been populated
        if cache.lights == nil {
            return bulbs
        }
        //Get all the bubls
        let lights = cache.lights as! [String:PHLight]
        for (key,light) in lights {
            bulbs.append(light.identifier)
        }
        return bulbs
    }
    
    //All other groups
    let groups = cache.groups as! [String:PHGroup]
    let group:PHGroup! = groups[groupId]
    if group == nil {
        return [String]()
    }
    //Group exists
    var bulbs:[String] = group.lightIdentifiers as! [String]
    return bulbs
}

func CreateGroup(bulbIds:[String], groupName:String){
    var bridgeSendAPI = PHBridgeSendAPI()
    bridgeSendAPI.createGroupWithName(groupName, lightIds: bulbIds, completionHandler: nil)
}

func DeleteGroup(groupId:String, completionHandler:PHBridgeSendErrorArrayCompletionHandler?) {
    var bridgeSendAPI = PHBridgeSendAPI()
    bridgeSendAPI.removeGroupWithId(groupId, completionHandler: completionHandler)
}

func UpdateGroupWithGroup(group:PHGroup){
    var bridgeSendAPI = PHBridgeSendAPI()
    bridgeSendAPI.updateGroupWithGroup(group, completionHandler: nil)
}

func GetGroupState(groupId:String) -> (lightState:PHLightState, modelNumber:String) {
    
    var groupLightState = PHLightState()
    groupLightState.on = false
    groupLightState.x = 0.5
    groupLightState.y = 0.5
    groupLightState.brightness = 255
    groupLightState.reachable = false
    
    //Get the Cache
    var cache:PHBridgeResourcesCache! = PHBridgeResourcesReader.readBridgeResourcesCache()
    
    var lightIds:[String] = GetBulbsInGroup(groupId)
    var lights:[String:PHLight] = cache.lights as! [String:PHLight]
    for lightId in lightIds {
        
        var light = lights[lightId]
        if light == nil {
            continue
        }
        
        if light!.lightState.reachable.boolValue {
            groupLightState.reachable = true
            if light!.lightState.on.boolValue {
                groupLightState.on = true
                groupLightState.brightness = light!.lightState.brightness
                groupLightState.x = light!.lightState.x
                groupLightState.y = light!.lightState.y
                return (groupLightState, light!.modelNumber)
            } else {
                groupLightState.on = false
                groupLightState.brightness = light!.lightState.brightness
                groupLightState.x = light!.lightState.x
                groupLightState.y = light!.lightState.y
            }
        }
        
    }
    return (groupLightState, "nil")
}



func GetGroupName(groupId:String) -> String? {
    
    if groupId == "0" {
        return "All Bulbs"
    }
    
    
    
    let cache:PHBridgeResourcesCache = PHBridgeResourcesReader.readBridgeResourcesCache()
    
    let groups = cache.groups as! [String:PHGroup]
    let group:PHGroup? = groups[groupId]
    let name:String? = group?.name
    
    return name
}


func SetGroupName(groupId:String, name:String){
    var bridgeSendAPI = PHBridgeSendAPI()
    let cache:PHBridgeResourcesCache = PHBridgeResourcesReader.readBridgeResourcesCache()
    let groups = cache.groups as! [String:PHGroup]
    let group:PHGroup? = groups[groupId]
    if group != nil {
        group!.name = name
        bridgeSendAPI.updateGroupWithGroup(group, completionHandler: nil)
    }
}

