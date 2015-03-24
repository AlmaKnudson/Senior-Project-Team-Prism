//
//  ColorPickerView.swift
//  Prism Lights
//
//  Created by Trudy Firestone on 2/17/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import UIKit

let COLOR_MODEL: String = "LCT001"

class NewColorPickerView: UIView {
  
    var currentBounds: CGRect
    let MIN_Y: CGFloat = 0.0503509;
    let MAX_Y: CGFloat = 0.5157895;
    let MID_Y: CGFloat = 0.3210526;

    let colorDist:CGFloat = 3;
  
    override init(frame: CGRect) {
        currentBounds = CGRectZero
        super.init(frame: frame)

//        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func drawRect(rect: CGRect) {
        currentBounds = bounds
        currentBounds.inset(dx: HALF_SELECTOR_WIDTH, dy: HALF_SELECTOR_WIDTH)
        //(0.4089552, 0.5157895)
        //(0.1661765, 0.0503509)
        //(0.6731343, 0.3210526)
        let context = UIGraphicsGetCurrentContext()
        for var y = currentBounds.minY; y < currentBounds.maxY; y+=colorDist {
            let yPercentage = (CGFloat)(y - currentBounds.minY) / currentBounds.height;
            let currentY = (MAX_Y - MIN_Y) * yPercentage + MIN_Y;
            let minX = getMinXFromY(currentY);
            let maxX = getMaxXFromY(currentY);
            for var x = currentBounds.minX; x < currentBounds.maxX; x+=colorDist {
                let xPercentage = (CGFloat) (x - currentBounds.minX) / currentBounds.width;
                let currentX = (maxX - minX) * xPercentage + minX;
                var xYColor = CGPoint(x: currentX, y: currentY)
                CGContextSetFillColorWithColor(context, PHUtilities.colorFromXY(xYColor, forModel: COLOR_MODEL).CGColor)
                CGContextAddRect(context, CGRectMake(x, y, colorDist, colorDist))
                CGContextDrawPath(context, kCGPathFill)
            }
        }
    }

    //y = mx + b
    //(0.4089552, 0.5157895)
    //(0.1661765, 0.0503509)
    // m = (0.51517895 - 0.0503509) / (0.4089552 - 0.1661765)
    // m = 1.9146162739976778
    // 0.05157895 = 1.9146162739976778 * 0.1661765 + b
    // b = 0.05157895 - 1.9146162739976778 * 0.1661765
    // y = 1.9146162739976778x + -0.2665852812559751
    // y + 0.2665852812559751 = 1.9146162739976778x
    // x = (y + 0.2665852812559751) / 1.9146162739976778
    func getMinXFromY(y: CGFloat) -> CGFloat {
        return (y + 0.2665852812559751) / 1.9146162739976778;
    }

    func getMaxXFromY(y: CGFloat) -> CGFloat {
        if(y > MID_Y) {
            //y = mx + b
            //(0.4089552, 0.5157895)
            //(0.6731343, 0.3210526)
            //m = (0.5157895 - 0.3210526) / (0.4089552 - 0.6731343)
            //m = -0.7371396904599948
            //0.5157895 = -0.7371396904599948 * (0.4089552) + b
            //b = 0.5157895 + 0.7371396904599948 * (0.4089552)
            // b = 0.8172466095400053
            // y = -0.7371396904599948x + 0.8172466095400053
            // y - 0.8172466095400053 = -0.7371396904599948x
            // x = (y - 0.8172466095400053) / -0.7371396904599948
            return (y - 0.8172466095400053) / -0.7371396904599948;
        }
        else {
            //y = mx + b
            //(0.1661765, 0.0503509)
            //(0.6731343, 0.3210526)
            //m = (0.0503509 - 0.3210526) / (0.1661765 - 0.6731343)
            //m = 0.533972847444107
            //0.0503509 = 0.533972847444107 * (0.1661765) + b
            //b =  -0.038382838883295654
            //y = 0.533972847444107x - 0.038382838883295654
            //x = (y + 0.038382838883295654) / 0.533972847444107
            return (y + 0.038382838883295654) / 0.533972847444107;
        }
    }

    func getPositionFromColor(currentXYColor: CGPoint) -> CGPoint {
        let currentXColor = currentXYColor.x;
        let currentYColor = currentXYColor.y;
        let currentYPercentage = (currentYColor - MIN_Y) / (MAX_Y - MIN_Y);
        let currentY = (currentYPercentage * currentBounds.height) + currentBounds.minY;
        let minX = getMinXFromY(currentYColor);
        let maxX = getMaxXFromY(currentYColor);
        let currentXPercentage = (currentXColor - minX) / (maxX - minX);
        let currentX = (currentXPercentage * currentBounds.width) + currentBounds.minX;
        return CGPoint(x: currentX, y: currentY)
    }
    
    func getColorFromPosition(x: CGFloat, y: CGFloat) -> CGPoint {
        let yPercentage = (y - currentBounds.minY) / currentBounds.height;
        let currentY = (MAX_Y - MIN_Y) * yPercentage + MIN_Y;
        let minX = getMinXFromY(currentY);
        let maxX = getMaxXFromY(currentY);
        let xPercentage = (x-currentBounds.minX) / currentBounds.width;
        let currentX = (maxX - minX) * xPercentage + minX;
        let point = CGPoint(x: currentX, y: currentY)
        return point
    }
}
