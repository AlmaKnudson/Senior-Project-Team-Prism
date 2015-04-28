//
//  BulbActivity.swift
//  Prism Lights
//
//  Created by Cody Foltz on 3/24/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

class BulbActivity : UIView {
    
    var isAnimating:Bool
    var bottomImage:UIImageView
    var topBackImage:UIImageView
    var topFrontImage:UIImageView
    
    required init(coder aDecoder: NSCoder) {
        isAnimating = false
        bottomImage = UIImageView(coder: aDecoder)
        bottomImage.image = UIImage(named: "bulb_bottom")!

        topBackImage = UIImageView(coder: aDecoder)
        topBackImage.image = UIImage(named: "bulb_top")!
        
        topFrontImage = UIImageView(coder: aDecoder)
        topFrontImage.image = UIImage(named: "bulb_top")!
        
        super.init(coder: aDecoder)
    }
    
    
    required override init(frame: CGRect) {
        isAnimating = false
        bottomImage = UIImageView(frame: frame)
        bottomImage.image = UIImage(named: "bulb_bottom")!
        bottomImage.contentMode = UIViewContentMode.ScaleAspectFit
        
        topBackImage = UIImageView(frame:frame)
        topBackImage.image = UIImage(named: "bulb_top")!
        topBackImage.contentMode = UIViewContentMode.ScaleAspectFit
        
        topFrontImage = UIImageView(frame: frame)
        topFrontImage.image = UIImage(named: "bulb_top")!
        topFrontImage.contentMode = UIViewContentMode.ScaleAspectFit
        
        super.init(frame: frame)
        self.addSubview(bottomImage)
        self.addSubview(topBackImage)
        self.addSubview(topFrontImage)

        self.changeTopColor("bulb_top", color: UIColor.greenColor())
        self.hidden = true
    }
    
    
    
    
    
    
    
    func StartActivityIndicator() {
        if isAnimating {
           return
        }
        
        isAnimating = true
        self.hidden = false
        

        
        
        
        var pulseAnimation:CABasicAnimation = CABasicAnimation(keyPath: "opacity");
        pulseAnimation.duration = 0.75;
        pulseAnimation.toValue = NSNumber(float: 0.0);
        pulseAnimation.timingFunction = CAMediaTimingFunction(name: kCAMediaTimingFunctionEaseInEaseOut);
        pulseAnimation.autoreverses = true;
        pulseAnimation.repeatCount = 20000
        self.topFrontImage.layer.addAnimation(pulseAnimation, forKey: nil)
        
        
        
    }
    
    func StopActivityIndicator() {
        if !isAnimating {
            return
        }
        
        isAnimating = false
        self.hidden = true
        self.topFrontImage.layer.removeAllAnimations()
        self.topBackImage.opaque = true
        self.topFrontImage.opaque = true
        
        
    }
    
    func IsAnimating() -> Bool {
        
        return isAnimating
    }
    
    
    private func changeTopColor(imageName:String, color:UIColor){
        //Load image
        var image = UIImage(named: imageName)
        UIGraphicsBeginImageContextWithOptions(image!.size, false, 0.0)
        var context = UIGraphicsGetCurrentContext()
        color.setFill()
        
        // translate/flip the graphics context (for transforming from CG* coords to UI* coords
        CGContextTranslateCTM(context, 0, image!.size.height)
        CGContextScaleCTM(context, 1.0, -1.0)
        
        // set the blend mode to color burn, and the original image
        CGContextSetBlendMode(context, kCGBlendModeColorBurn)
        var rect = CGRect(x: 0, y: 0, width: image!.size.width, height: image!.size.height)
        CGContextDrawImage(context, rect, image!.CGImage)
        
        // set a mask that matches the shape of the image, then draw (color burn) a colored rectangle
        CGContextClipToMask(context, rect, image!.CGImage)
        CGContextAddRect(context, rect);
        CGContextDrawPath(context,kCGPathFill);
        
        // generate a new UIImage from the graphics context we drew onto
        var coloredImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext();
        
        topBackImage.image = coloredImage
    }

    
    
}