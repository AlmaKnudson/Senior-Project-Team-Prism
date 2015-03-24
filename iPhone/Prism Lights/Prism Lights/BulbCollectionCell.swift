//
//  BulbCollectionCell.swift
//  Prism Lights
//
//  Created by Cody Foltz on 12/1/14.
//  Copyright (c) 2014 Prism. All rights reserved.
//



import UIKit

class BulbCollectionCell : UICollectionViewCell{
    
    @IBOutlet weak var top_bulb: UIImageView!
    @IBOutlet weak var bulbLabel: UILabel!
    @IBOutlet weak var bottom_bulb: UIImageView!
    
    
    func initBulbCell(name:String){
        bulbLabel!.text = name
        top_bulb!.image = UIImage(named: "bulb_top")
        bottom_bulb!.image = UIImage(named: "bulb_bottom")
    }
    
    func initGroupCell(name:String){
        bulbLabel!.text = name
        top_bulb!.image = UIImage(named: "groupTop")
        bottom_bulb!.image = UIImage(named: "groupBottom")
    }
    
    func turnOff(animate:Bool){
        if animate {
            var pulseAnimation:CABasicAnimation = CABasicAnimation(keyPath: "opacity");
            pulseAnimation.duration = 3.0;
            pulseAnimation.toValue = NSNumber(float: 0.1);
            pulseAnimation.timingFunction = CAMediaTimingFunction(name: kCAMediaTimingFunctionEaseInEaseOut);
            pulseAnimation.autoreverses = false;
            pulseAnimation.repeatCount = 1;
            self.top_bulb.layer.addAnimation(pulseAnimation, forKey: nil)
        } else {
            top_bulb.layer.opacity = 0.2
            bottom_bulb.layer.opacity = 1.0
        }
    }
    
    func turnOn(animate:Bool){
        if animate {
            var pulseAnimation:CABasicAnimation = CABasicAnimation(keyPath: "opacity");
            pulseAnimation.duration = 3.0;
            pulseAnimation.toValue = NSNumber(float: 1.0);
            pulseAnimation.timingFunction = CAMediaTimingFunction(name: kCAMediaTimingFunctionEaseInEaseOut);
            pulseAnimation.autoreverses = false;
            pulseAnimation.repeatCount = 1;
            self.top_bulb.layer.addAnimation(pulseAnimation, forKey: nil)
        } else {
            top_bulb.layer.opacity = 1.0
            bottom_bulb.layer.opacity = 1.0
        }
    }
    
    
    
    
    func SetUnreachable(){
        self.top_bulb!.layer.opacity = 0.5
        self.bottom_bulb!.layer.opacity = 0.5
        
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
        
        top_bulb!.image = coloredImage
    }
    
    func SetGroupColor(color:UIColor){
        changeTopColor("groupTop", color: color)
    }
    
    
    func SetBulbColor(color:UIColor){
        changeTopColor("bulb_top", color: color)
    }
    
}

