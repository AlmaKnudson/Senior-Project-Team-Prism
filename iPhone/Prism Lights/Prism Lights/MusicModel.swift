//
//  MusicModel.swift
//  Prism Lights
//
//  Created by Alma Knudson on 3/8/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation


class MusicModel {
    class var sharedInstance: MusicModel {
        struct Static {
            static var instance: MusicModel?
            static var token: dispatch_once_t = 0
        }
        
        dispatch_once(&Static.token) {
            Static.instance = MusicModel()
        }
        
        return Static.instance!
    }
    
    var 
    
    
}

