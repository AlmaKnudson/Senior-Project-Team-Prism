//
//  BulbCollectionCell.swift
//  Prism Lights
//
//  Created by Cody Foltz on 12/1/14.
//  Copyright (c) 2014 Prism. All rights reserved.
//

import UIKit

class BulbCollectionCell : UICollectionViewCell{
    
    @IBOutlet weak var bulbImageView: UIImageView!
    @IBOutlet weak var bulbLabel: UILabel!
    
    
    func SetBulbImage(on:Bool){
        bulbImageView!.image = UIImage(named: "pushAuth")
    }
    
    func SetBulbLabel(){
        
    }
    
    
    
    
}
