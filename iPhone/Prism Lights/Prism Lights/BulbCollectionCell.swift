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
    
    
    func SetBulbImage(on:Bool){
        if(on){
            top_bulb!.image = UIImage(named: "bulb_top")
            bottom_bulb!.image = UIImage(named: "bulb_bottom")
        } else{
            top_bulb!.image = UIImage(named: "bulb_absent")
            bottom_bulb!.image = UIImage(named: "bulb_bottom")
        }
    }
    
    func SetBulbLabel(name :String){
        bulbLabel!.text = name
    }
    
    func SetBulbUnreachable(){
        bottom_bulb!.image = UIImage(named: "bulb_absent")
        top_bulb!.image = UIImage(named: "bulb_absent")
    }
    
    

    func SetBulbColor(color:UIColor){
        
        //Load image
        var image = UIImage(named: "bulb_top")
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
    
}

