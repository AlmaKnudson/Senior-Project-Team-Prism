//
//  Favorite.swift
//  Prism Lights
//
//  Created by Trudy Firestone on 4/20/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import UIKit

class Favorite {
    private var _stateMap: [String: PHLightState]
    private var _all: Bool
    private var _allLightState: PHLightState?
    private var _favoriteColors: [CGColor]
    private var _name: String
    
    var stateMap: [String: PHLightState] {
        return _stateMap
    }
    
    var favoriteColors: [CGColor] {
        return _favoriteColors
    }
    
    var name: String {
        return _name
    }
    
    var isAll: Bool {
        return _all
    }
    
    var allLightState: PHLightState? {
        return _allLightState
    }
    
    init(name: String, lightIds: [String]) {
        _all = false
        _favoriteColors = []
        _name = name
        _stateMap = [:]
        let lightCache = PHBridgeResourcesReader.readBridgeResourcesCache()!.lights
        for lightId in lightIds {
            var oldLightState: PHLightState = (lightCache[lightId] as PHLight).lightState
            let lightState = copyLightState(oldLightState)
            _favoriteColors.append(getDisplayColor(lightState))
            _stateMap[lightId] = lightState
        }
    }
    
    init(name: String, oldLightState: PHLightState) {
        _all = true
        _name = name
        _favoriteColors = []
        _stateMap=[:]
        _allLightState = copyLightState(oldLightState)
        _favoriteColors.append(getDisplayColor(allLightState!))
    }
    
    private func copyLightState(oldLightState: PHLightState)->PHLightState {
        var lightState = PHLightState()
        lightState.brightness = oldLightState.brightness
        lightState.on = oldLightState.on
        lightState.x = oldLightState.x
        lightState.y = oldLightState.y
        return lightState
    }
    
    private func getDisplayColor(lightState: PHLightState) -> CGColor {
        if(lightState.x != nil && lightState.y != nil) {
            if(lightState.on != nil && lightState.on! == 1) {
                let point = CGPoint(x: Double(lightState.x), y: Double(lightState.y))
                return PHUtilities.colorFromXY(point, forModel: COLOR_MODEL).CGColor
            } else {
                return UIColor.blackColor().CGColor
            }
        }
        return UIColor.blackColor().CGColor
    }
    
    func saveState()-> NSDictionary {
        let favoriteState: NSMutableDictionary = [
            "name" : _name,
            "all" : _all,
            "lightStates": saveLightStates()]
        if(_allLightState != nil) {
            favoriteState["allLightState"] =  saveLightState(_allLightState!)
        }
        return favoriteState
    }
    
    init(lightStates: NSDictionary) {
        _name = lightStates["name"] as String
        _all = lightStates["all"] as Bool
        _stateMap = [:]
        _favoriteColors = []
        loadLightStates(lightStates["lightStates"] as NSDictionary)
        let allLightsState: AnyObject? = lightStates["allLightState"];
        if(allLightsState != nil) {
            _allLightState = loadLightState(allLightsState as NSDictionary)
        }
    }
    
    func saveLightStates()-> NSDictionary {
        let lightStates: NSMutableDictionary = [:]
        for (lightId, state) in _stateMap {
            lightStates[lightId] = saveLightState(state)
        }
        return lightStates
    }
    
    func loadLightStates(lightStates: NSDictionary) {
        for (lightId, lightState) in lightStates {
            _stateMap[(lightId as String)] = loadLightState(lightState as NSDictionary)
        }
    }
    
    func saveLightState(lightState: PHLightState)-> NSDictionary {
        let savedLightState: NSDictionary = [
            "on": lightState.on,
            "brightness": lightState.brightness,
            "x" : lightState.x,
            "y" : lightState.y]
        return savedLightState
    }
    
    func loadLightState(oldLightState: NSDictionary)-> PHLightState {
        let lightState = PHLightState()
        lightState.on = oldLightState["on"] as Bool
        lightState.brightness = oldLightState["brightness"] as Int
        lightState.x = oldLightState["x"] as Float
        lightState.y = oldLightState["y"] as Float
        _favoriteColors.append(getDisplayColor(lightState))
        return lightState
        
    }
    
}
