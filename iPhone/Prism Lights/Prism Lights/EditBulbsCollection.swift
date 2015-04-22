//
//  EditBulbsCollection.swift
//  Prism Lights
//
//  Created by Cody Foltz on 4/21/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation




class EditBulbsCollection: UICollectionViewController, UICollectionViewDataSource, UICollectionViewDelegate, RAReorderableLayoutDelegate, RAReorderableLayoutDataSource {
    
    
    @IBAction func finishedButton(sender: UIButton) {
        dismissDeleget?.DismissMe()
    }
    @IBAction func deleteButton(sender: UIButton) {
        //TODO: Delete thing
        dismissDeleget?.DismissMe()
    }
    
    
    var editType:String? = nil
    var dismissDeleget:DismissPresentedController? = nil
    
    
    
    func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumInteritemSpacingForSectionAtIndex section: Int) -> CGFloat {
        return 2.0
    }
    
    func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumLineSpacingForSectionAtIndex section: Int) -> CGFloat {
        return 2.0
    }
    
    override func numberOfSectionsInCollectionView(collectionView: UICollectionView) -> Int {
        return 1
    }
    
    func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAtIndex section: Int) -> UIEdgeInsets {
        return UIEdgeInsetsMake(0, 0, 2.0, 0)
    }
    
    override func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        if section == 0 {
            return 1
        }
        return 0
    }
    
     override func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        var cell:UICollectionViewCell! = BulbCollectionCell()
        
        
        
        
        
        switch editType! {
        case "single":
            cell = self.collectionView!.dequeueReusableCellWithReuseIdentifier("bulbCell", forIndexPath: indexPath) as! BulbCollectionCell
            if cell == nil {
                cell = BulbCollectionCell()
            }
            
            return cell
        case "group":
            cell = self.collectionView!.dequeueReusableCellWithReuseIdentifier("groupCell", forIndexPath: indexPath) as! GroupBulbCell
            if cell == nil {
                cell = GroupBulbCell()
            }
            
            return cell
            
        case "favorite":
            cell = self.collectionView!.dequeueReusableCellWithReuseIdentifier("favoriteCell", forIndexPath: indexPath) as! FavoriteCollectionCell
            if cell == nil {
                cell = FavoriteCollectionCell()
            }
            return cell
        default:
            assertionFailure("Edit wasn't setup with a type")

            
        }
        
        
        return cell
    }
    
    func collectionView(collectionView: UICollectionView, allowMoveAtIndexPath indexPath: NSIndexPath) -> Bool {
        if collectionView.numberOfItemsInSection(indexPath.section) <= 1 {
            return false
        }
        return true
    }
    
    func collectionView(collectionView: UICollectionView, atIndexPath: NSIndexPath, didMoveToIndexPath toIndexPath: NSIndexPath) {
        
    }
    
    func scrollTrigerEdgeInsetsInCollectionView(collectionView: UICollectionView) -> UIEdgeInsets {
        return UIEdgeInsetsMake(100.0, 100.0, 100.0, 100.0)
    }
    
    func collectionView(collectionView: UICollectionView, reorderingItemAlphaInSection section: Int) -> CGFloat {
        if section == 0 {
            return 0
        }else {
            return 0.3
        }
    }
    
//    func scrollTrigerPaddingInCollectionView(collectionView: UICollectionView) -> UIEdgeInsets {
//        return UIEdgeInsetsMake(self.collectionView.contentInset.top, 0, self.collectionView.contentInset.bottom, 0)
//    }
    
    
    override func collectionView(collectionView: UICollectionView, viewForSupplementaryElementOfKind kind: String, atIndexPath indexPath: NSIndexPath) -> UICollectionReusableView{
        //1
        switch kind {
            //2
        case UICollectionElementKindSectionHeader:
            //3
            let headerView =
            collectionView.dequeueReusableSupplementaryViewOfKind(kind,
                withReuseIdentifier: "SectionHeader",
                forIndexPath: indexPath)
                as! SectionHeader
            
            switch editType! {
            case "single":
                headerView.headerLabel.text = "Edit Bulbs"
            case "group":
                headerView.headerLabel.text = "Edit Groups"
            case "favorite":
                headerView.headerLabel.text = "Edit Favorites"
            default:
                assertionFailure("Edit type not set")
            }
            return headerView
        default:
            //4
            assert(false, "Unexpected element kind. Should only have headers.")
        }
    }
    
    
    
    
    
    
}