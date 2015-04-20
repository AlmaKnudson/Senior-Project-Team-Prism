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
    
    var pageIndex:Int = 0
    var PAGE_COUNT:Int = 3
    var controllers:[UIViewController] = []
    var controlDict:[String:UIViewController] = [:]
    
    // MARK: - View Lifecycle
    override func viewDidLoad() {
        super.viewDidLoad()
        CreateControllerArray()
        createPageViewController()
        setupPageControl()
    }
    
    /**
    Sets the phone status bar to be light colored for dark background
    
    :returns: UIStatusBarStyle
    */
    override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return UIStatusBarStyle.LightContent
    }
    
    private func CreateControllerArray() {
        let bulbCollection = self.storyboard!.instantiateViewControllerWithIdentifier("BulbCollection") as! BulbsCollectionController
        controllers.append(bulbCollection)
        
        let groupCollection =
            self.storyboard?.instantiateViewControllerWithIdentifier("GroupCollection") as! GroupCollectionController
        controllers.append(groupCollection)
        let favoriteCollection =
            self.storyboard?.instantiateViewControllerWithIdentifier("FavoriteCollection") as! FavoriteCollectionController
        controllers.append(favoriteCollection)
        
    }
    
    
    private func createPageViewController() {
        
        let pageController = self.storyboard!.instantiateViewControllerWithIdentifier("PageViewController") as! UIPageViewController
        pageController.dataSource = self

        
        var controller:[UIViewController] = [controllers[0]]
        pageController.setViewControllers(controller as [AnyObject], direction: UIPageViewControllerNavigationDirection.Forward, animated: false, completion: nil)
        
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
    
    
    func pageViewController(pageViewController: UIPageViewController, viewControllerBeforeViewController viewController: UIViewController) -> UIViewController? {
        
        var index = IndexOfController(viewController)
        if index <= 0 {
            return nil
        }
        return controllers[index-1]
    }
    func pageViewController(pageViewController: UIPageViewController, viewControllerAfterViewController viewController: UIViewController) -> UIViewController? {
        
        
        var index = IndexOfController(viewController)
        if index >= controllers.count-1 {
            return nil
        }
        
        return controllers[index+1]
    }
    
    
    func IndexOfController(viewController:UIViewController) -> Int {
        
        for var i = 0; i < controllers.count; i++ {
            if(controllers[i] === viewController){
                return i
            }
        }
        return -1
    }
    
    
    
    // MARK: - Page Indicator
    
    // A page indicator will be visible if both methods are implemented, transition style is 'UIPageViewControllerTransitionStyleScroll', and navigation orientation is 'UIPageViewControllerNavigationOrientationHorizontal'.
    // Both methods are called in response to a 'setViewControllers:...' call, but the presentation index is updated automatically in the case of gesture-driven navigation.
    // The number of items reflected in the page indicator.
    func presentationCountForPageViewController(pageViewController: UIPageViewController) -> Int{
        return self.controllers.count
    }

    // The selected item reflected in the page indicator.
    func presentationIndexForPageViewController(pageViewController: UIPageViewController) -> Int{
        return pageIndex
    }
    
    
    
    func pageViewController(pageViewController: UIPageViewController, willTransitionToViewControllers pendingViewControllers: [AnyObject]){
        
    }
    
    // Sent when a gesture-initiated transition ends. The 'finished' parameter indicates whether the animation finished, while the 'completed' parameter indicates whether the transition completed or bailed out (if the user let go early).
    func pageViewController(pageViewController: UIPageViewController, didFinishAnimating finished: Bool, previousViewControllers: [AnyObject], transitionCompleted completed: Bool){
        if completed {
            pageIndex++
        }
    }
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
}