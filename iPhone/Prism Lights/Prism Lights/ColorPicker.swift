//
//  ColorPicker.swift
//  Prism Lights
//
//  Created by Trudy Firestone on 2/17/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import UIKit
let HALF_SELECTOR_WIDTH: CGFloat = 20

protocol ColorChangedDelegate: class {
    func onColorChanged(color: CGPoint)
}

class ColorPicker: UIView {

    private var _currentBounds: CGRect
    private var _currentInnerBounds: CGRect
    private var _positionX: CGFloat
    private var _positionY: CGFloat
    private var _currentXYColor: CGPoint
    private var _tempXYColor: CGPoint
    private var _colorBackgroundView: NewColorPickerView
    private var _selectorView: ColorSelectorView
    
    weak var colorChangedDelegate: ColorChangedDelegate?
    
    var positionX: CGFloat {
        get {
            return _positionX
        }
        set {
            if(newValue < _currentInnerBounds.minX) {
                _positionX = _currentInnerBounds.minX
            }
            else if (newValue > _currentInnerBounds.maxX) {
                _positionX = _currentInnerBounds.maxX
            }
            else {
                _positionX = newValue
            }
            _selectorView.positonX = _positionX
        }
    }
    
    var positionY: CGFloat {
        get {
            return _positionY
        }
        set {
            if(newValue < _currentInnerBounds.minY) {
                _positionY = _currentInnerBounds.minY
            }
            else if (newValue > _currentInnerBounds.maxY) {
                _positionY = _currentInnerBounds.maxY
            }
            else {
                _positionY = newValue
            }
            _selectorView.positonY = _positionY
        }
    }
    
    var color: CGPoint {
        get {
            return _currentXYColor
        }
        set {
            if(!colorsEqual(_tempXYColor, color2: newValue)) {
                _currentXYColor = newValue
                tempColor = newValue
                setPositionFromColor()
            }
        }
    }
    
    private var tempColor: CGPoint {
        get {
            return _tempXYColor
        }
        set {
            _tempXYColor = newValue
            _selectorView.color = PHUtilities.colorFromXY(newValue, forModel: COLOR_MODEL).CGColor
        }
    }
    
    override init(frame: CGRect) {
        _currentBounds = CGRectZero
        _currentInnerBounds = CGRectZero
        _positionX = 0
        _positionY = 0
        _currentXYColor = CGPoint()
        _tempXYColor = CGPoint()
        _colorBackgroundView = NewColorPickerView(frame: CGRectZero)
        _selectorView = ColorSelectorView(frame: CGRectZero)
        super.init(frame: frame)
        addSubview(_colorBackgroundView)
        addSubview(_selectorView)
    }

    required init(coder aDecoder: NSCoder) {
        _currentBounds = CGRectZero
        _currentInnerBounds = CGRectZero
        _positionX = 0
        _positionY = 0
        _currentXYColor = CGPoint()
        _tempXYColor = CGPoint()
        _colorBackgroundView = NewColorPickerView(frame: CGRectZero)
        _selectorView = ColorSelectorView(frame: CGRectZero)
        super.init(coder: aDecoder)
        addSubview(_colorBackgroundView)
        addSubview(_selectorView)
//        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        _currentBounds = bounds
        _currentInnerBounds = bounds
        _currentInnerBounds.inset(dx: HALF_SELECTOR_WIDTH, dy: HALF_SELECTOR_WIDTH)
        _selectorView.frame = _currentBounds
        _colorBackgroundView.frame = _currentBounds
    }
    
    func colorsEqual(color1: CGPoint, color2: CGPoint) -> Bool{
        if(abs(color1.x - color2.x) > 0.001) {
            return false
        }
        if(abs(color1.y - color2.y) > 0.001) {
            return false
        }
        return true
    }
    
    private func setPositionFromColor() {
        let position = _colorBackgroundView.getPositionFromColor(_currentXYColor)
        positionX = position.x
        positionY = position.y
    }
    
    private func setColorFromPosition(x: CGFloat, y: CGFloat) {
        tempColor = _colorBackgroundView.getColorFromPosition(x, y: y)
    }
    
    func updateColorFromTouch(touch: UITouch) {
        var point = touch.locationInView(self)
        positionX = point.x
        positionY = point.y
        setColorFromPosition(positionX, y: positionY)
    }
    
    override func touchesBegan(touches: NSSet, withEvent event: UIEvent) {
        updateColorFromTouch(touches.anyObject() as UITouch)
    }
    
    override func touchesCancelled(touches: NSSet!, withEvent event: UIEvent!) {
        tempColor = _currentXYColor
        setPositionFromColor()
    }
 
    override func touchesMoved(touches: NSSet, withEvent event: UIEvent) {
        updateColorFromTouch(touches.anyObject() as UITouch)
    }
    
    override func touchesEnded(touches: NSSet, withEvent event: UIEvent) {
        updateColorFromTouch(touches.anyObject() as UITouch)
        _currentXYColor = tempColor
        colorChangedDelegate?.onColorChanged(_currentXYColor)
    }
}
