//
//  TabViewController.swift
//  Prism Lights
//
//  Created by Cody Foltz on 12/9/14.
//  Copyright (c) 2014 Prism. All rights reserved.
//

import UIKit

class TabViewController : UITabBarController{
    
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override init() {
        super.init()
    }
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: NSBundle?) {
        super.init(nibName: nil, bundle: nil)
    }

    
    
    override func viewWillAppear(animated: Bool) {
        self.selectedIndex = 1;
        //self.reloadInputViews()
    }
    
}
