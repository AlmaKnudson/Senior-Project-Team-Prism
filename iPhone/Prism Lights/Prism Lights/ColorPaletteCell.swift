//
//  BulbCollectionCell.swift
//  Prism Lights
//
//  Created by Cody Foltz on 12/1/14.
//  Copyright (c) 2014 Prism. All rights reserved.
//



import UIKit

class ColorPaletteCell : UICollectionViewCell{
    
    let colorView: UIImageView!
    
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        colorView = ColorView()
        colorView.contentMode = UIViewContentMode.ScaleAspectFill
        contentView.addSubview(colorView)
        
        
    }
    
    
//    override init(coder: Coder) {
//        super.init(frame: frame)
//        
//        colorView = ColorView(frame: CGRect(x: 0, y: 0, width: self.frame.width, height: self.frame.height))
//        colorView.contentMode = UIViewContentMode.ScaleAspectFill
//        contentView.addSubview(colorView)
//    }
//
//    required init(coder aDecoder: NSCoder) {
//        fatalError("init(coder:) has not been implemented")
//    }
    
}

