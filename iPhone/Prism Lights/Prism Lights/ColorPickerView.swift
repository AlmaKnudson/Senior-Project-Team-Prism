//
//  ColorPickerView.swift
//  Prism Lights
//
//  Created by Cody Foltz on 12/11/14.
//  Copyright (c) 2014 Prism. All rights reserved.
//

import UIKit

class ColorPickerView: UIViewController {
    
    
    @IBOutlet weak var colorSelected: UIView!
    @IBOutlet weak var colorPicker: UIImageView!
    
    var delegate :ColorSelectedProtocol? = nil
    
    //MARK - Touch Events
    
    override func touchesBegan(touches: NSSet, withEvent event: UIEvent) {
        touchesMoved(touches, withEvent: event)
    }
    
    override func touchesMoved(touches: NSSet, withEvent event: UIEvent) {
        var touch = (touches.allObjects[0]) as UITouch
        var point = touch.locationInView(self.colorPicker)
        touch = event.allTouches()?.anyObject() as UITouch
        if touch.view == self.colorPicker{
            point = touch.locationInView(self.colorPicker)
        }

        var color = getPixelColorAtLocation(point)
        colorSelected.backgroundColor = color
    }
    
    override func touchesEnded(touches: NSSet, withEvent event: UIEvent) {
        
        var touch = (touches.allObjects[0]) as UITouch
        var point = touch.locationInView(self.colorPicker)
        var color = getPixelColorAtLocation(point)
        colorSelected.backgroundColor = color
        
        if color != nil{
            delegate?.ColorSelected(color!)
        }
        
    }
    
    
    //MARK - Pixel Methods
    
    func getPixelColorAtLocation(point :CGPoint) -> (UIColor?){
        var color :UIColor? = nil
        var inImage :CGImageRef = (colorPicker.image?.CGImage!)!
        
        // Create off screen bitmap context to draw the image into. Format ARGB is 4 bytes for each pixel: Alpa, Red, Green, Blue
        var cgctx = createARGBBitmapContextFromImage(inImage)
        if (cgctx == nil) { return nil; /* error */ }
        
        
        var w :CGFloat = CGFloat(CGImageGetWidth(inImage))
        var h :CGFloat = CGFloat(CGImageGetHeight(inImage))
        var rect = CGRect(x: 0, y: 0, width: w, height: h)
        
        if(DEBUG){
            println("Frame Height: \(self.colorPicker.frame.height)")
            println("Scale: \((colorPicker.image?.scale)!) ")
            println("Point x: \(point.x * (colorPicker.image?.scale)!) y: \(point.y)")
            println("Width: \(w) Height: \(h)")
        }
        // Draw the image to the bitmap context. Once we draw, the memory
        // allocated for the context for rendering will then contain the
        // raw image data in the specified color space.
        CGContextDrawImage(cgctx, rect, inImage);
        
        // Now we can get a pointer to the image data associated with the bitmap
        // context.
        var data = (CGBitmapContextGetData (cgctx))
        var dataInt = unsafeBitCast(data, UnsafeMutablePointer<UInt8>.self )
        
        var xScaler = w/colorPicker.frame.width
        var yScaler = h/colorPicker.frame.height
        if (data != nil) {
            //offset locates the pixel in the data from x,y.
            //4 for 4 bytes of data per pixel, w is width of one row of data.
            var offset :Int = 4*((Int(w)*Int(round(point.y*yScaler)))+Int(round(point.x*xScaler)))
            var alpha :UInt8 =  dataInt[offset]
            var red :UInt8 = dataInt[offset+1];
            var green :UInt8 = dataInt[offset+2];
            var blue :UInt8 = dataInt[offset+3];
            
            if(DEBUG){
                println("offset \(offset) colors: RGB A \(red) \(green) \(blue)   \(alpha)")
            }
            
            var rf :CGFloat = CGFloat(Float(red)/255.0)
            var gf :CGFloat = CGFloat(Float(green)/255.0)
            var bf :CGFloat = CGFloat(Float(blue)/255.0)
            var af :CGFloat = CGFloat(Float(alpha)/255.0)
            
            if(DEBUG){
                println("offset 2: \(offset) colors: RGB A \(rf) \(gf) \(bf)   \(af)")
            }
            color = UIColor(red:rf, green:gf, blue:bf, alpha:af)
        }

        return color?
    }
    
    

    
    func createARGBBitmapContextFromImage(inImage :CGImageRef) -> (CGContextRef?){
        var context :CGContextRef? = nil
        var colorSpace :CGColorSpaceRef? = nil
        //void *          bitmapData;
        var bitmapByteCount :size_t = 0
        var bitmapBytesPerRow :size_t = 0
        
        // Get image width, height. We'll use the entire image.
        var pixelsWide :size_t = CGImageGetWidth(inImage)  //size_t
        var pixelsHigh :size_t = CGImageGetHeight(inImage) //size_t
        
        // Declare the number of bytes per row. Each pixel in the bitmap in this
        // example is represented by 4 bytes; 8 bits each of red, green, blue, and
        // alpha.
        bitmapBytesPerRow   = (pixelsWide * size_t(4));
        bitmapByteCount     = (bitmapBytesPerRow * pixelsHigh);
        
        // Use the generic RGB color space.
        colorSpace = CGColorSpaceCreateDeviceRGB();
        
        if (colorSpace == nil)
        {

            println("Error allocating color space\n")

            return nil;
        }
        
        // Allocate memory for image data. This is the destination in memory
        // where any drawing to the bitmap context will be rendered.
//        var bitmapData = malloc( bitmapByteCount );
//        if (bitmapData == nil)
//        {
//            println("Memory not allocated!");
//            return nil;
//        }
        
        // Create the bitmap context. We want pre-multiplied ARGB, 8-bits
        // per component. Regardless of what the source image format is
        // (CMYK, Grayscale, and so on) it will be converted over to the format
        // specified here by CGBitmapContextCreate.
        context = CGBitmapContextCreate (nil,
            pixelsWide,
            pixelsHigh,
            8,      // bits per component
            bitmapBytesPerRow,
            colorSpace,
            CGBitmapInfo(CGImageAlphaInfo.PremultipliedFirst.rawValue))
            
//            CGBitmapInfo(CGImageAlphaInfo.PremultipliedFirst))
//            CGBitmapInfo(kCGImageAlphaPremultipliedFirst))
//            CGImageAlphaInfo.PremultipliedFirst);
        if (context == nil)
        {
//            free (bitmapData);
            println("Context not created!");
        }

        
        return context;
    
    }

}
