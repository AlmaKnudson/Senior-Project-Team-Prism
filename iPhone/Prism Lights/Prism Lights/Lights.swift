//
//  Lights.swift
//  Prism Lights
//
//  Created by Cody Foltz on 3/22/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation


func GetBulbName(bulbId:String) -> String? {
    
    let cache:PHBridgeResourcesCache = PHBridgeResourcesReader.readBridgeResourcesCache()
    
    let lights = cache.lights as! [String:PHLight]
    let light:PHLight? = lights[bulbId]
    let name:String? = light?.identifier
    
    return name
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





// var hueSDK = (UIApplication.sharedApplication().delegate as! AppDelegate).hueSDK!




//
//    
//    
//    
//    
//    
//    
//    if(DEBUG){
//        println("Bulb tapped")
//    }
//    var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
//    var light = cache!.lights["\(indexPath.row+1)"] as PHLight
//    
//    if(light.lightState.reachable == 0){
//        return
//    }
//    var lightState = PHLightState()
//    var cell = bulbCollectionView.cellForItemAtIndexPath(indexPath) as BulbCollectionCell
//    if light.lightState.on == 1{
//        light.lightState.on = false
//        lightState.on = false
//    } else{
//        lightState.on = true
//        light.lightState.on = true
//        //cell.SetBulbImage(true)
//        
//    }
//    var bridgeSendAPI = PHBridgeSendAPI()
//    bridgeSendAPI.updateLightStateForId(light.identifier, withLightState: lightState, completionHandler: nil)
//    if(light.lightState.on == 1){
//        var point = CGPoint(x: Double(light.lightState.x), y: Double(light.lightState.y))
//        var color = PHUtilities.colorFromXY(point, forModel: light.modelNumber)
//        cell.SetBulbColor(color)
//    } else{
//        cell.SetBulbImage(false)
//    }
//    bridgeSendAPI.updateLightStateForId(light.identifier, withLightState: lightState){
//        error -> Void in
//        if error != nil {
//            if(DEBUG){
//                println("Error updating light state.")
//            }
//            return
//        }
//        
//        if(light.lightState.on == 1){
//            var point = CGPoint(x: Double(light.lightState.x), y: Double(light.lightState.y))
//            var color = PHUtilities.colorFromXY(point, forModel: light.modelNumber)
//            cell.SetBulbColor(color)
//        } else{
//            cell.SetBulbImage(false)
//        }
//        self.skipNextHeartbeat = true
//    }

    
    
