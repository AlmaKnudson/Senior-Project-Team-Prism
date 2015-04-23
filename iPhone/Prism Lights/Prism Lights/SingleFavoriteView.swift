//
//  SingleFavoriteView.swift
//  Prism Lights
//
//  Created by Trudy Firestone on 4/19/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import UIKit

class SingleFavoriteView: UIView {
    private var _colors: [CGColor];
    let strokeWidth: CGFloat = 5
    
    var colors: [CGColor] {
        get {
            return _colors
        }
        set {
            _colors = newValue
            setNeedsDisplay();
        }
    }
    
    override init(frame: CGRect) {
        _colors = []
        super.init(frame: frame)
        backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0)
    }
    
    required init(coder aDecoder: NSCoder) {
        _colors = []
        super.init(coder: aDecoder)
        backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0)
        //fatalError("init(coder:) has not been implemented")
    }
    
    func addColor(color: CGColor) {
        _colors.append(color)
        setNeedsDisplay()
    }
    
    override func drawRect(rect: CGRect) {
        var currentBounds = bounds
        let context = UIGraphicsGetCurrentContext()
        let roundRectPath: CGMutablePath = CGPathCreateMutable()
        //        let transform = CGAffineTransform(a: 0, b: 0, c: 0, d: 0, tx: 0, ty: 0)
        CGPathAddRoundedRect(roundRectPath, nil, bounds, 5, 5)
        CGContextAddPath(context, roundRectPath)
        CGContextClip(context)
        let two: CGFloat = 2
        let inset = strokeWidth / two;
        currentBounds.inset(dx: inset, dy: inset)
        let colorSize: CGFloat = CGFloat(colors.count)
        let rectWidth = currentBounds.width / colorSize
        var currentRect = CGRectZero
        for color in _colors {
            (currentRect, currentBounds) = currentBounds.rectsByDividing(rectWidth, fromEdge: CGRectEdge.MinXEdge)
            CGContextAddRect(context, currentRect)
            CGContextSetFillColorWithColor(context, color)
            CGContextDrawPath(context, kCGPathFill)
        }
        CGContextAddPath(context, roundRectPath)
        CGContextSetStrokeColorWithColor(context, UIColor.whiteColor().CGColor)
        CGContextSetLineWidth(context, strokeWidth)
        CGContextDrawPath(context, kCGPathStroke)
        
    }
    
}
