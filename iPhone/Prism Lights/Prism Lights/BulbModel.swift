//
//  BulbModel.swift
//  Prism Lights
//
//  Created by Cody Foltz on 4/23/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

var BulbsModel = BulbModel()

class BulbModel {
    
    var bulbs:[String:PHLightState] = [:]
    var bulbIds:[String]
    
    func CompareToCache(){
        
        var cache:PHBridgeResourcesCache! = PHBridgeResourcesReader.readBridgeResourcesCache()
        
        if cache == nil || cache.lights == nil {
            return
        }
        
        var cacheIds:[String] = (cache.lights.keys.array as! [NSString]) as! [String]
        
        if bulbIds.count == cacheIds.count {
            return
        }
        
        
        //Adds new bulbs and removes bulbs that have been removed.
        
        //Create a mapping to insure all bulbs in the cache have been recorded
        var cachedBulbs = [String:Bool]()
        for id in cacheIds {
            cachedBulbs[id] = false
        }
        
        //Remove bulbId if it is not in the cache
        for var i = 0; i < bulbIds.count; i++ {
            if cachedBulbs.indexForKey(bulbIds[i]) == nil {
                bulbIds.removeAtIndex(i)
            } else {
                cachedBulbs[bulbIds[i]] = true
            }
        }
        
        //Add bulbs from cache that is not already accounted for
        for (cachedBulbId, foundFlag) in cachedBulbs {
            if !foundFlag {
                bulbIds.append(cachedBulbId)
            }
        }
        
        
        
    }
    
    init(){
        bulbs = [:]
        bulbIds = []
        loadFromFile()
    }
    
    
    func bulbIdAtIndex(index:Int) -> String {
        
        if index < 0 || index >= bulbIds.count {
            return "-1"
        }
        return bulbIds[index]
    }
    
    
    private func saveToFile() -> Bool {
        var idsArray: NSMutableArray = []
        for bulbId in bulbIds {
            idsArray.addObject(bulbId)
        }
        let documentsDirectory: String? = NSSearchPathForDirectoriesInDomains(
            .DocumentDirectory, .UserDomainMask, true)?[0] as! String?
        let filePath: String? = documentsDirectory?.stringByAppendingPathComponent("bulbIds.plist")
        if(filePath != nil) {
            idsArray.writeToFile(filePath!, atomically: true)
            return true
        }
        return false
    }
    
    private func loadFromFile() {
        let documentsDirectory: String? = NSSearchPathForDirectoriesInDomains(
            .DocumentDirectory, .UserDomainMask, true)?[0] as! String?
        let filePath: String? = documentsDirectory?.stringByAppendingPathComponent("bulbIds.plist")
        if let actualFilePath = filePath {
            let optStoredFavorites: NSArray? = NSArray(contentsOfFile: actualFilePath)
            if let storedFavorites = optStoredFavorites {
                bulbIds = []
                for id in storedFavorites {
                    var bulbId = id as! String
                    bulbIds.append(bulbId)
                }
                return
            }
            
        }
        CompareToCache()
    }
    
    
}