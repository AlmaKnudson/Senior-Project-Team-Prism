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