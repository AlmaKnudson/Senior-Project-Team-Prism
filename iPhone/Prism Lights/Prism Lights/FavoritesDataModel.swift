//
//  FavoritesDataModel.swift
//  Prism Lights
//
//  Created by Trudy Firestone on 4/20/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import UIKit //so I don't have to recreate CGColor

let favoritesDataModel = FavoritesDataModel()

class FavoritesDataModel {
    private var _favorites: [Favorite]
    
    init() {
        _favorites = []
        //if file not empty load it, else load the defaults
        loadFromFile()
    }
    
    var count: Int {
        return _favorites.count
    }
    
    func loadDefaultFavorites() {
        var normalLightState = PHLightState()
        normalLightState.on = true
        normalLightState.x = 0.4596
        normalLightState.y = 0.4105
        normalLightState.brightness = 254
        addFavoriteState("Normal All On", withLightState: normalLightState)
        var offState = PHLightState()
        offState.on = false
        offState.x = 0.4596
        offState.y = 0.4105
        offState.brightness = 254
        addFavoriteState("All Off", withLightState: offState)
        saveToFile()
    }
    
    func getFavorite(atIndex index: Int)-> Favorite {
        return _favorites[index]
    }
    
    func getNextFavoriteName()-> String {
        return "Favorite \(_favorites.count + 1)"
    }
    
    func addFavoriteState(name: String, withLightIds lightIds: [String]) {
        _favorites.append(Favorite(name: name, lightIds: lightIds))
        saveToFile()
    }
    
    func addFavoriteState(name: String, withLightState lightState: PHLightState) {
        _favorites.append(Favorite(name: name, oldLightState: lightState))
        saveToFile()
    }
    
    func updateFavorite(atIndex index: Int, withName name: String, andWithLightIds lightIds: [String]) {
        _favorites[index] = Favorite(name: name, lightIds: lightIds)
        saveToFile()
    }
    
    func updateFavorite(atIndex index: Int, withName name: String, withState lightState: PHLightState) {
        _favorites[index] =  Favorite(name: name, oldLightState: lightState)
        saveToFile()
    }
    
    func reoderFavorites(shiftedFrom: Int, shiftedTo: Int) {
        let favorite = _favorites[shiftedFrom]
        _favorites.removeAtIndex(shiftedFrom)
        _favorites.insert(favorite, atIndex: shiftedTo)
    }
    
    func doneReorderingFavorites() {
        saveToFile()
    }
    
    func loadFromFile() {
        let documentsDirectory: String? = NSSearchPathForDirectoriesInDomains(
            .DocumentDirectory, .UserDomainMask, true)?[0] as! String?
        let filePath: String? = documentsDirectory?.stringByAppendingPathComponent("favorites.plist")
        if let actualFilePath = filePath {
            let optStoredFavorites: NSArray? = NSArray(contentsOfFile: actualFilePath)
            if let storedFavorites = optStoredFavorites {
                for storedFavorite in storedFavorites {
                    var favorite = Favorite(lightStates: storedFavorite as! NSDictionary)
                    _favorites.append(favorite)
                }
                return
            }
            
        }
        loadDefaultFavorites();
    }
    
    private func saveToFile() {
        var favoritesArray: NSMutableArray = []
        for favorite in _favorites {
            favoritesArray.addObject(favorite.saveState())
        }
        let documentsDirectory: String? = NSSearchPathForDirectoriesInDomains(
            .DocumentDirectory, .UserDomainMask, true)?[0] as! String?
        let filePath: String? = documentsDirectory?.stringByAppendingPathComponent("favorites.plist")
        if(filePath != nil) {
            favoritesArray.writeToFile(filePath!, atomically: true)
        }
    }
    
    func removeFavorites(toRemove: [Int]) {
        var subtractIndex = 0
        for favoriteIndex in toRemove {
            _favorites.removeAtIndex(favoriteIndex - subtractIndex)
            subtractIndex++
        }
        saveToFile()
    }
    
}


