//
//  BulbCollectionCell.swift
//  Prism Lights
//
//  Created by Cody Foltz on 12/1/14.
//  Copyright (c) 2014 Prism. All rights reserved.
//


//TODO: Split the bulb into 2 parts, top and bottom so I can color just the top.


import UIKit

class BulbCollectionCell : UICollectionViewCell{
    
    @IBOutlet weak var bulbImageView: UIImageView!
    @IBOutlet weak var bulbLabel: UILabel!
    
    
    func SetBulbImage(on:Bool){
        if(on){
            bulbImageView!.image = UIImage(named: "bulb")
        } else{
            bulbImageView!.image = UIImage(named: "bulb_absent")
        }
    }
    
    func SetBulbLabel(name :String){
        bulbLabel!.text = name
    }
    

    func SetBulbColor(color:UIColor){
        
        //Load image
        var image = UIImage(named: "bulb")
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
        
        bulbImageView!.image = coloredImage
        }
    
}

