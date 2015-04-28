//
//  GroupBulbCell.swift
//  Prism Lights
//
//  Created by Cody Foltz on 4/14/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

class GroupBulbCell : UICollectionViewCell{
    
    @IBOutlet weak var top_bulb: UIImageView!
    @IBOutlet weak var bulbLabel: UILabel!
    @IBOutlet weak var bottom_bulb: UIImageView!
    @IBOutlet weak var unReachableImage: UIImageView!
    @IBOutlet weak var selectedImage: UIImageView!
    
    
    func initGroupCell(name:String){
        bulbLabel!.text = name
        top_bulb!.image = UIImage(named: "groupTop")
        bottom_bulb!.image = UIImage(named: "groupBottom")
        unReachableImage.hidden = true
    }
    
    func turnOff(animate:Bool){
        if animate {
            var pulseAnimation:CABasicAnimation = CABasicAnimation(keyPath: "opacity");
            pulseAnimation.duration = 3.0;
            pulseAnimation.toValue = NSNumber(float: 0.5);
            pulseAnimation.timingFunction = CAMediaTimingFunction(name: kCAMediaTimingFunctionEaseInEaseOut);
            pulseAnimation.autoreverses = false;
            pulseAnimation.repeatCount = 0;
            self.top_bulb.layer.addAnimation(pulseAnimation, forKey: nil)
        } else {
            top_bulb.layer.opacity = 0.5
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
            pulseAnimation.repeatCount = 0;
            self.top_bulb.layer.addAnimation(pulseAnimation, forKey: nil)
        } else {
            top_bulb.layer.opacity = 1.0
            bottom_bulb.layer.opacity = 1.0
        }
    }
    
    func MakeSelected(){
        selectedImage.hidden = false
    }
    
    func MakeUnSelected(){
        selectedImage.hidden = true
    }
    
    
    
    
    func SetUnreachable(){
        self.top_bulb!.layer.opacity = 0.5
        self.bottom_bulb!.layer.opacity = 0.5
        unReachableImage.hidden = false
        
    }
    
    func SetGroupColor(color:UIColor){
        top_bulb.image = changeTopColor("groupTop", color)
    }

    
}

