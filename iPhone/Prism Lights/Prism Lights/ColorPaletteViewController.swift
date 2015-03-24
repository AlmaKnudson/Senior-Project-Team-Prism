//
//  ColorPaletteViewController.swift
//  Prism Lights
//
//  Created by Alma Knudson on 3/24/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation



class ColorPaletteViewController: UIViewController, UICollectionViewDataSource, UICollectionViewDelegate {

    
    var tableData: [String] = ["1", "2", "3", "4", "5"]
    
    
    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return tableData.count
    }
    
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        let cell:ColorPaletteCell = collectionView.dequeueReusableCellWithReuseIdentifier("colorPaletteCell", forIndexPath: indexPath) as ColorPaletteCell
        cell.cellView.backgroundColor = UIColor.redColor()
        
        return cell
    }
    
    func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) { 
        println("Cell: \(indexPath.row) selected.")
    }

}