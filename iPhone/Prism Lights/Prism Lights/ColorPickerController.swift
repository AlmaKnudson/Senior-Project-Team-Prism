//
//  ColorPickerController.swift
//  Prism Lights
//
//  Created by Cody Foltz on 3/10/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation


class ColorPickerController : UIViewController {
    
    weak var colorChangedDelegate: ColorChangedDelegate?
    
    
    override func viewWillAppear(animated: Bool) {
        var colorPickerView = self.view as ColorPicker
        colorPickerView.colorChangedDelegate = self.colorChangedDelegate
    }
    
    
    
}