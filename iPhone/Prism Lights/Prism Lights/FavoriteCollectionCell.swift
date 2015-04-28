//
//  FavoriteCollectionCell.swift
//  Prism Lights
//
//  Created by Cody Foltz on 4/20/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

class FavoriteCollectionCell : UICollectionViewCell {
    
    @IBOutlet weak var favoriteView: SingleFavoriteView!
    @IBOutlet weak var favoriteLabel: UILabel!
    @IBOutlet weak var selectedImage: UIImageView!
    
    func SetColors(colors:[CGColor]){
        favoriteView.colors = colors
    }
    
    func AddColor(color:CGColor){
        favoriteView.addColor(color)
    }
    
    /**
    Setups of the view with the colors and name of the favorite
    
    :param: colors The list of colors in the favorite
    :param: name   Name of the favorite
    */
    func SetupView(colors:[CGColor], name:String){
        favoriteView.colors = colors
        favoriteLabel.text = name
        MakeUnSelected()
    }
    
    func MakeSelected(){
        selectedImage.hidden = false
    }
    
    func MakeUnSelected(){
        selectedImage.hidden = true
    }
    
}