//
//  ColorView.swift
//  Prism Lights
//
//  Created by Alma Knudson on 3/22/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import UIKit
@IBDesignable class CustomView : UIView {
    @IBInspectable var borderColor: UIColor = UIColor.clearColor()
    @IBInspectable var borderWidth: CGFloat = 0
    @IBInspectable var cornerRadius: CGFloat = 0
}


class ColorView: UIView {
  
    
    @IBInspectable var borderColor: UIColor = UIColor.clearColor() {
        didSet {
            layer.borderColor = borderColor.CGColor
        }
    }
    
    @IBInspectable var borderWidth: CGFloat = 0 {
        didSet {
            layer.borderWidth = borderWidth
        }
    }
    
    @IBInspectable var cornerRadius: CGFloat = 0 {
        didSet {
            layer.cornerRadius = cornerRadius
        }
    }
    
}
