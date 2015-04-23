//
//  SectionHeader.swift
//  Prism Lights
//
//  Created by Cody Foltz on 4/21/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

class SectionHeader :UICollectionReusableView {
    
    var headerType:String? = nil
    
    @IBOutlet weak var headerLabel: UILabel!
    
    @IBAction func AddBulb(sender: AnyObject) {
        
    }
    
    @IBAction func EditBulb(sender: AnyObject) {
//        self.performSegueWithIdentifier("editCollection", sender: self)
    }
    
    func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if(DEBUG){
            println("In Prepare For Segue")
        }
        if segue.identifier == "editCollection" {
            var dest = segue.destinationViewController as! EditBulbsCollection
            dest.editType = headerType
        }
    }
    
    
}