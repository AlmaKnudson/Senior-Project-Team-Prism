//
//  ColorSelectorView.swift
//  Prism Lights
//
//  Created by Trudy Firestone on 2/23/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import UIKit

class ColorSelectorView: UIView {

    private var _location: CGPoint
    private var _color: CGColor
    
    var positonX: CGFloat {
        get {
            return _location.x
        }
        set {
            _location.x = newValue
            setNeedsDisplay()
        }
    }
    
    var positonY: CGFloat {
        get {
            return _location.y
        }
        set {
            _location.y = newValue
            setNeedsDisplay()
        }
    }
    
    var color: CGColor {
        get {
            return _color
        }
        set {
            _color = newValue
            setNeedsDisplay()
        }
    }
    
    override init(frame: CGRect) {
        _location = CGPoint(x: 0, y: 0)
        _color = UIColor.grayColor().CGColor
        super.init(frame: frame)
        backgroundColor = UIColor(hue: 0, saturation: 0, brightness: 0, alpha: 0)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func drawRect(rect: CGRect) {
        let context = UIGraphicsGetCurrentContext()
        var selectorBounds = CGRectMake(_location.x - HALF_SELECTOR_WIDTH, _location.y - HALF_SELECTOR_WIDTH, 2 * HALF_SELECTOR_WIDTH, 2 * HALF_SELECTOR_WIDTH)
        selectorBounds.inset(dx: 2, dy: 2)
        CGContextSetFillColorWithColor(context, color)
        CGContextSetStrokeColorWithColor(context, UIColor.whiteColor().CGColor)
        CGContextAddEllipseInRect(context, selectorBounds)
        CGContextSetLineWidth(context, 2.0)
        CGContextDrawPath(context, kCGPathFillStroke)
        
    }
    

    
}
