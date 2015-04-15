//
//  HomePageController.swift
//  Prism Lights
//
//  Created by Cody Foltz on 4/14/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation


class HomePageController : UIViewController, UIPageViewControllerDataSource, UIPageViewControllerDelegate {
    
    
    // MARK: - Variables
    private var pageViewController: UIPageViewController?
    
    var index:Int = 0
    var PAGE_COUNT:Int = 3
    
    // MARK: - View Lifecycle
    override func viewDidLoad() {
        super.viewDidLoad()
        createPageViewController()
        setupPageControl()
    }
    
    private func createPageViewController() {
        
        let pageController = self.storyboard!.instantiateViewControllerWithIdentifier("PageViewController") as! UIPageViewController
        pageController.dataSource = self
        
        let bulbCollection = self.storyboard!.instantiateViewControllerWithIdentifier("BulbCollection") as! BulbsCollectionController
        let startingViewControllers: NSArray = [bulbCollection]

        pageController.setViewControllers(startingViewControllers as [AnyObject], direction: UIPageViewControllerNavigationDirection.Forward, animated: false, completion: nil)
        
        pageViewController = pageController
        addChildViewController(pageViewController!)
        self.view.addSubview(pageViewController!.view)
        pageViewController!.didMoveToParentViewController(self)
    }
    
    private func setupPageControl() {
        let appearance = UIPageControl.appearance()
        appearance.pageIndicatorTintColor = UIColor.grayColor()
        appearance.currentPageIndicatorTintColor = UIColor.whiteColor()
        appearance.backgroundColor = UIColor.darkGrayColor()
    }
    
    // MARK: - UIPageViewControllerDataSource
    
    
    func pageViewController(pageViewController: UIPageViewController, viewControllerBeforeViewController viewController: UIViewController) -> UIViewController?{
        
        if index <= 0 {
            return nil
        }
        index--
        
        
        return GetController()
    }
    func pageViewController(pageViewController: UIPageViewController, viewControllerAfterViewController viewController: UIViewController) -> UIViewController?{
        
        if index >= PAGE_COUNT-1 {
            return nil
        }
        index++
        return GetController()
    }
    
    func GetController() -> (UIViewController?){
        
        switch index {
        case 0:
            return self.storyboard?.instantiateViewControllerWithIdentifier("BulbCollection") as! BulbsCollectionController
        case 1:

            return
                self.storyboard?.instantiateViewControllerWithIdentifier("GroupCollection") as! GroupCollectionController
        case 2:
            return nil
        default:
            return nil
        }
    }
    
    
    // MARK: - Page Indicator
    
    // A page indicator will be visible if both methods are implemented, transition style is 'UIPageViewControllerTransitionStyleScroll', and navigation orientation is 'UIPageViewControllerNavigationOrientationHorizontal'.
    // Both methods are called in response to a 'setViewControllers:...' call, but the presentation index is updated automatically in the case of gesture-driven navigation.
    // The number of items reflected in the page indicator.
    func presentationCountForPageViewController(pageViewController: UIPageViewController) -> Int{
        return PAGE_COUNT
    }

    // The selected item reflected in the page indicator.
    func presentationIndexForPageViewController(pageViewController: UIPageViewController) -> Int{
        return index
    }
    
    
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
}