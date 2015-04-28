//
//  Protocols.swift
//  Prism Lights
//
//  Created by Cody Foltz on 4/11/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

protocol BulbSettingsProtocol{
    /*mutating*/ func ApplySettings()
}

protocol DismissPresentedController{
    func DismissMe()
}

protocol EditLightModel{
    func CanMoveItem(fromIndex:Int, toIndex:Int ) -> Bool
    func MoveItem(fromIndex:Int, toIndex:Int) -> Bool
    func DeleteItemAt(index:Int) -> Bool
}




let MAX_HUE:UInt32 = 65535