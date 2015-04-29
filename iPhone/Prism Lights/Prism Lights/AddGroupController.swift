//
//  AddGroupController.swift
//  Prism Lights
//
//  Created by Cody Foltz on 4/28/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation


class AddGroupController: UIViewController, UICollectionViewDataSource, UICollectionViewDelegate {
    
    
    @IBOutlet weak var bulbCollectionView: UICollectionView!
    
    @IBAction func finishedButton(sender: UIButton) {
        var list = [String]()
        for index in selected {
            list.append(BulbsModel[index])
        }
        let count = Groups.count() + 1
        CreateGroup(list, "Group \(count)")
        dismissDeleget?.DismissMe()
    }
    
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
                return BulbsModel.count()
        }
        return 0
    }
    
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        var cell:UICollectionViewCell! = nil
        
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
        
            var cell = (collectionView.cellForItemAtIndexPath(indexPath) as! BulbCollectionCell)
            if shouldBeSelected {
                cell.MakeSelected()
            } else {
                cell.MakeUnSelected()
            }
    }
    
    
    
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
            
            return headerView
        default:
            //4
            assert(false, "Unexpected element kind. Should only have headers.")
        }
    }
    
    /**
    Sets the phone status bar to be light colored for dark background
    
    :returns: UIStatusBarStyle
    */
    override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return UIStatusBarStyle.LightContent
    }
    
    
    
    
    
}