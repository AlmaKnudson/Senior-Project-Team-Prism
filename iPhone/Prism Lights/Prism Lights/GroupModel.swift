//
//  GroupModel.swift
//  Prism Lights
//
//  Created by Cody Foltz on 4/23/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

var Groups = GroupModel()

class GroupModel {
    
    var groupIds:[String]
    
    func CompareToCache(){
        
        var cache:PHBridgeResourcesCache! = PHBridgeResourcesReader.readBridgeResourcesCache()
        
        if cache == nil || cache.groups == nil {
            return
        }
        
        var cacheIds:[String] = (cache.groups.keys.array as! [NSString]) as! [String]
        
        //Account for group "0" which is not in the cache
        //Place in the front of the array so that it is first if it needs to be added.
        cacheIds.insert("0", atIndex: 0)
        
        //Account for group "0" which is not in the cache
        if groupIds.count == (cacheIds.count) {
            return
        }
        
        
        //Adds new groups and removes groups that have been removed.
        
        //Create a mapping to insure all bulbs in the cache have been recorded
        var cachedGroups = [String:Bool]()
        for id in cacheIds {
            cachedGroups[id] = false
        }
        
        
        //Remove bulbId if it is not in the cache
        for var i = 0; i < groupIds.count; i++ {
            
            if cachedGroups.indexForKey(groupIds[i]) == nil {
                groupIds.removeAtIndex(i)
            } else {
                cachedGroups[groupIds[i]] = true
            }
        }
        
        //Add bulbs from cache that is not already accounted for
        for (cachedGroupId, foundFlag) in cachedGroups {
            if !foundFlag {
                groupIds.append(cachedGroupId)
            }
        }
        
        saveToFile()
        
    }
    
    init(){
        groupIds = []
        loadFromFile()
        CompareToCache()
    }
    
    subscript(index: Int) -> String {
        if index < 0 || index >= groupIds.count {
            assertionFailure("Index out of bound within BulbModel")
        }
        return groupIds[index]
    }
    
    func count() -> Int {
        return groupIds.count
    }
    
    func idAtIndex(index:Int) -> String {
        
        if index < 0 || index >= groupIds.count {
            assertionFailure("Index out of bound within BulbModel")
        }
        return groupIds[index]
    }
    
    
    private func saveToFile() -> Bool {
        var idsArray: NSMutableArray = []
        for bulbId in groupIds {
            idsArray.addObject(bulbId)
        }
        let documentsDirectory: String? = NSSearchPathForDirectoriesInDomains(
            .DocumentDirectory, .UserDomainMask, true)?[0] as! String?
        let filePath: String? = documentsDirectory?.stringByAppendingPathComponent("groupIds.plist")
        if(filePath != nil) {
            idsArray.writeToFile(filePath!, atomically: true)
            return true
        }
        return false
    }
    
    private func loadFromFile() {
        let documentsDirectory: String? = NSSearchPathForDirectoriesInDomains(
            .DocumentDirectory, .UserDomainMask, true)?[0] as! String?
        let filePath: String? = documentsDirectory?.stringByAppendingPathComponent("groupIds.plist")
        if let actualFilePath = filePath {
            let optStoredFavorites: NSArray? = NSArray(contentsOfFile: actualFilePath)
            if let storedFavorites = optStoredFavorites {
                groupIds = []
                for id in storedFavorites {
                    var bulbId = id as! String
                    groupIds.append(bulbId)
                }
                return
            }
            
        }
        CompareToCache()
    }
    

    
    
    
    
}


