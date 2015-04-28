//
//  EditBulbsCollection.swift
//  Prism Lights
//
//  Created by Cody Foltz on 4/21/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

class EditBulbsCollection: UIViewController, //UICollectionViewDataSource, UICollectionViewDelegate, 
RAReorderableLayoutDelegate, RAReorderableLayoutDataSource {
    
    
    @IBOutlet weak var bulbCollectionView: UICollectionView!
    
    @IBAction func finishedButton(sender: UIButton) {
        
        dismissDeleget?.DismissMe()
    }
    @IBAction func deleteButton(sender: UIButton) {
        var list = [Int]()
        for index in selected {
            list.append(index)
        }
        
        switch editType! {
        case "single":
            return
        case "group":
            Groups.RemoveGroups(list)
        case "favorite":
            
            favoritesDataModel.removeFavorites(list)
        default:
            assertionFailure("Edit wasn't setup with a type")
        }

        
        dismissDeleget?.DismissMe()
    }
    
    
    var editType:String? = nil
    var dismissDeleget:DismissPresentedController? = nil
    var selected:Set<Int> = Set<Int>()
    
    
    override func viewDidLoad() {
        bulbCollectionView.dataSource = self
        bulbCollectionView.delegate = self
        
    }
    
    
    override func viewWillAppear(animated: Bool) {
    }
    
    
    
    
    func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumInteritemSpacingForSectionAtIndex section: Int) -> CGFloat {
        return 2.0
    }
    
    func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumLineSpacingForSectionAtIndex section: Int) -> CGFloat {
        return 2.0
    }
    
    func numberOfSectionsInCollectionView(collectionView: UICollectionView) -> Int {
        return 1
    }
    
    func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAtIndex section: Int) -> UIEdgeInsets {
        return UIEdgeInsetsMake(0, 0, 2.0, 0)
    }
    
    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        if section == 0 {
            switch editType! {
            case "single":
                return BulbsModel.count()
            case "group":
                return Groups.count()
            case "favorite":
                return favoritesDataModel.count
            default:
                assertionFailure("Edit wasn't setup with a type")
            }
        }
        return 0
    }
    
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        var cell:UICollectionViewCell! = nil
        
        switch editType! {
        case "single":
            cell = collectionView.dequeueReusableCellWithReuseIdentifier("bulbCell", forIndexPath: indexPath) as! BulbCollectionCell
            if cell == nil {
                cell = BulbCollectionCell()
            }
            (cell as! BulbCollectionCell).initBulbCell(GetBulbName(BulbsModel[indexPath.row])!)
            
            if !GetBulbIsReachable(BulbsModel[indexPath.row])! {
                (cell as! BulbCollectionCell).SetUnreachable()
            } else if IsBulbOn(BulbsModel[indexPath.row]){
                (cell as! BulbCollectionCell).turnOff(false)
            } else{
                (cell as! BulbCollectionCell).SetBulbColor(GetBulbUIColor(BulbsModel[indexPath.row])!)
                
            }
            
            
            
            return cell
        case "group":
            cell = collectionView.dequeueReusableCellWithReuseIdentifier("groupCell", forIndexPath: indexPath) as! GroupBulbCell
            if cell == nil {
                cell = GroupBulbCell()
            }
            (cell as! GroupBulbCell).initGroupCell(GetGroupName(Groups[indexPath.row])!)
            //TODO: is off?
            
            return cell
            
        case "favorite":
            cell = collectionView.dequeueReusableCellWithReuseIdentifier("favoriteCell", forIndexPath: indexPath) as! FavoriteCollectionCell
            if cell == nil {
                cell = FavoriteCollectionCell()
                
            }
            var fav = favoritesDataModel.getFavorite(atIndex: indexPath.row)
            (cell as! FavoriteCollectionCell).SetupView(fav.favoriteColors, name: fav.name)
            return cell
        default:
            assertionFailure("Edit wasn't setup with a type")

            
        }
        
        
        return cell
    }
    
    func collectionView(collectionView: UICollectionView, allowMoveAtIndexPath indexPath: NSIndexPath) -> Bool {
        return true
    }
    
    func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
        var shouldBeSelected = true
        if selected.contains(indexPath.row) {
            shouldBeSelected = false
            selected.remove(indexPath.row)
        } else {
            selected.insert(indexPath.row)
        }
        
        
        switch editType! {
            case "single":
                var cell = (collectionView.cellForItemAtIndexPath(indexPath) as! BulbCollectionCell)
                if shouldBeSelected {
                    cell.MakeSelected()
                } else {
                    cell.MakeUnSelected()
                }
            
            case "group":
                if Groups[indexPath.row] == "0" {
                    selected.remove(indexPath.row)
                    return
                }
                
                
                var cell = (collectionView.cellForItemAtIndexPath(indexPath) as! GroupBulbCell)
                if shouldBeSelected {
                    cell.MakeSelected()
                } else {
                    cell.MakeUnSelected()
                }
            case "favorite":
                var cell = (collectionView.cellForItemAtIndexPath(indexPath) as! FavoriteCollectionCell)
                if shouldBeSelected {
                    cell.MakeSelected()
                } else {
                    cell.MakeUnSelected()
            }
            default:
            assertionFailure("Edit wasn't setup with a type")
        }
    }
    
    func collectionView(collectionView: UICollectionView, atIndexPath: NSIndexPath, didMoveToIndexPath toIndexPath: NSIndexPath) {
        
        switch editType! {
        case "single":
            BulbsModel.MoveItem(atIndexPath.row, toIndex: toIndexPath.row)
        case "group":
            Groups.MoveItem(atIndexPath.row, toIndex: toIndexPath.row)
        case "favorite":
            favoritesDataModel.reoderFavorites(atIndexPath.row, shiftedTo: toIndexPath.row)
        default:
            assertionFailure("Edit wasn't setup with a type")
        }
    }
    
    func collectionView(collectionView: UICollectionView, collectionViewLayout layout: RAReorderableLayout, didEndDraggingItemToIndexPath indexPath: NSIndexPath) {
        selected.removeAll(keepCapacity: true)
        collectionView.reloadData()
        return
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
    
    
    func collectionView(collectionView: UICollectionView, viewForSupplementaryElementOfKind kind: String, atIndexPath indexPath: NSIndexPath) -> UICollectionReusableView{
        //1
        switch kind {
            //2
        case UICollectionElementKindSectionHeader:
            //3
            let headerView =
            collectionView.dequeueReusableSupplementaryViewOfKind(kind,
                withReuseIdentifier: "EditHeader",
                forIndexPath: indexPath)
                as! EditHeader
            
            switch editType! {
            case "single":
                headerView.headerLabel.text = "Edit Bulbs"
                headerView.trashButton.hidden = true
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