//
//  datething.swift
//  Prism Lights
//
//  Created by Cody Foltz on 3/8/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

class ColoredDatePicker: UIDatePicker {
    var changed = false
    override func addSubview(view: UIView) {
        if !changed {
            changed = true
            self.setValue((UIColor.whiteColor()), forKey: "textColor")
        }
        super.addSubview(view)
    }
}