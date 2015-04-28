//
//  BulbModel.swift
//  Prism Lights
//
//  Created by Cody Foltz on 4/23/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

var BulbsModel = BulbModel()

class BulbModel : NSObject {
    
    var bulbs:[String:PHLightState] = [:]
    var bulbIds:[String]
    
    func CompareToCache(){
        
        var cache:PHBridgeResourcesCache! = PHBridgeResourcesReader.readBridgeResourcesCache()
        
        if cache == nil || cache.lights == nil {
            return
        }
        
        var cacheIds:[String] = (cache.lights.keys.array as! [NSString]) as! [String]
        
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
        
        saveToFile()
        
    }
    
    override init(){
        bulbs = [:]
        bulbIds = []
        super.init()
        loadFromFile()
        CompareToCache()
        var manager = PHNotificationManager.defaultManager()
        manager!.registerObject(self, withSelector: "HeartBeatReceivedBulb", forNotification: "LOCAL_CONNECTION_NOTIFICATION")
    }
    
    func HeartBeatReceivedBulb() {
        CompareToCache()
    }
    
    subscript(index: Int) -> String {
        if index < 0 || index >= bulbIds.count {
            assertionFailure("Index out of bound within BulbModel")
        }
        return bulbIds[index]
    }
    
    func count() -> Int {
        return bulbIds.count
    }
    
    func idAtIndex(index:Int) -> String {
        
        if index < 0 || index >= bulbIds.count {
            assertionFailure("Index out of bound within BulbModel")
        }
        return bulbIds[index]
    }
    
    
    
    func CanMoveItem(fromIndex:Int, toIndex:Int ) -> Bool {
        if fromIndex < 0 || fromIndex >= bulbIds.count {
            return false
        }
        if toIndex < 0 || toIndex >= bulbIds.count {
            return false
        }
        return true
        
    }
    
    func MoveItem(fromIndex:Int, toIndex:Int) -> Bool {
        if CanMoveItem(fromIndex, toIndex: toIndex) {
            var fromTemp = bulbIds.removeAtIndex(fromIndex)
            bulbIds.insert(fromTemp, atIndex: toIndex)
            saveToFile()
            return true
        } else {
            return false
        }
    }

    func CanDeleteItemAt(index:Int) -> Bool {
        if index < 0 || index >= bulbIds.count {
            return false
        }
        
        //Can't remove bulbs at the moment
        return false
    }
    
    func DeleteItemAt(index:Int) -> Bool {
        if CanDeleteItemAt(index) {
            bulbIds.removeAtIndex(index)
            saveToFile()
        }
        return false
    }
    
//    
//    func RemoveBulbs(toRemove: [Int]) {
//        var subtractIndex = 0
//        for index in toRemove {
//            bulbIds.removeAtIndex(index - subtractIndex)
//            subtractIndex++
//        }
//        saveToFile()
//    }
    
    
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