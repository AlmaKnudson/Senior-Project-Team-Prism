//
//  ColorUtil.swift
//  Prism Lights
//
//  Created by Cody Foltz on 4/14/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

func changeTopColor(imageName:String, color:UIColor) -> (UIImage){
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
    
    return coloredImage
}
