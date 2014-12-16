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
        if(on){
            bulbImageView!.image = UIImage(named: "bulb")
        } else{
            bulbImageView!.image = UIImage(named: "bulb_absent")
        }
    }
    
    func SetBulbLabel(name :String){
        bulbLabel!.text = name
    }
    
    
    
    
}
