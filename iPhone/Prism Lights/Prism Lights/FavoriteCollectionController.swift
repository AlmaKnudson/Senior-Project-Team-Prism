//
//  FavoriteCollectionController.swift
//  Prism Lights
//
//  Created by Cody Foltz on 4/14/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

class FavoriteCollectionController : UIViewController, UIGestureRecognizerDelegate, UICollectionViewDataSource, BulbSettingsProtocol, DismissPresentedController {
    
    var retryConnection = true
    var beenConnected = false
    var skipNextHeartbeat = false
    //var favorites:FavoritesDataModel
    
    
    @IBOutlet weak var bulbCollectionView: UICollectionView!
    
    
    //MARK: - UIViewController Methods
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //Add long press Gesture
        var gesture = UILongPressGestureRecognizer(target: self, action: "ShowBulbSettings:")
        gesture.minimumPressDuration = 0.50
        gesture.delegate = self
        self.bulbCollectionView.addGestureRecognizer(gesture)
    }
    
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        
    }
    
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: NSBundle?) {
        super.init(nibName: nil, bundle: nil)

    }
    
    /**
    View Will Appear
    Registers the controller with the PHManager for bridge connections
    Then tries to connect to the bridge
    */
    override func viewWillAppear(animated: Bool) {
        var manager = PHNotificationManager.defaultManager()
        manager!.registerObject(self, withSelector: "HeartBeatReceived", forNotification: "LOCAL_CONNECTION_NOTIFICATION")
        manager!.registerObject(self, withSelector: "NetworkConnectionLost", forNotification: "NO_LOCAL_CONNECTION_NOTIFICATION")
        manager!.registerObject(self, withSelector: "NotAuthorized", forNotification: "NO_LOCAL_AUTHENTICATION_NOTIFICATION")
        
        
        //Check that we are connected to bridge.
        if !((UIApplication.sharedApplication().delegate as! AppDelegate).hueSDK!.localConnected()){
            //Connect to bridge
            (UIApplication.sharedApplication().delegate as! AppDelegate).hueSDK!.enableLocalConnection()
        }
        
    }
    
    override func viewDidAppear(animated: Bool) {
        bulbCollectionView.reloadData()
    }
    
    override func viewWillDisappear(animated: Bool) {
        PHNotificationManager.defaultManager().deregisterObjectForAllNotifications(self)
        
        if(DEBUG){
            println("Favorite Controller will disappeared")
        }
    }
    override func viewDidDisappear(animated: Bool) {
        if(DEBUG){
            println("Favorite disappeared")
        }
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if(DEBUG){
            println("In Prepare For Segue")
        }
        if segue.identifier == "BulbSettingsNav" {
            var dest = segue.destinationViewController as! UINavigationController
            var bulbSettingsController = dest.viewControllers[0] as! BulbSettingsController
            bulbSettingsController.homeDelegate = self
            bulbSettingsController.id = "\((sender as! NSIndexPath).row+1)"
            bulbSettingsController.isGroup = false
        } else if segue.identifier == "pushAuth" {
            var dest = segue.destinationViewController as! PushAuthController
            dest.delegate = self
        } else if segue.identifier == "editCollection" {
            var dest = segue.destinationViewController as! EditBulbsCollection
            dest.editType = "favorite"
            dest.dismissDeleget = self
        }
    }
    
    //MARK: - UICollectionView Methods
    
    /**
    Number of sections
    
    :param: collectionView the bulb collection view
    :param: section        The sections, 0 is group, 1 is individual bulbs
    
    :returns: number of bulbs in group
    */
    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int{
        
        return favoritesDataModel.count
    }
    
    func numberOfSectionsInCollectionView(collectionView: UICollectionView) -> Int {
        return 1
    }
    
    
    // The cell that is returned must be retrieved from a call to -dequeueReusableCellWithReuseIdentifier:forIndexPath:
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell{
        
        var cell:FavoriteCollectionCell! = bulbCollectionView.dequeueReusableCellWithReuseIdentifier("favorite", forIndexPath: indexPath) as! FavoriteCollectionCell
        if( cell == nil){
            cell = FavoriteCollectionCell()
        }
        var favorite = favoritesDataModel.getFavorite(atIndex: indexPath.row)
        cell.SetupView(favorite.favoriteColors, name: favorite.name)
        
        return cell!
    }
    
    func collectionView(collectionView: UICollectionView, viewForSupplementaryElementOfKind kind: String, atIndexPath indexPath: NSIndexPath) -> UICollectionReusableView{
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
            headerView.headerType = "favorite"
            return headerView
        default:
            //4
            assert(false, "Unexpected element kind. Should only have headers.")
        }
    }

    
    
    func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
        if(DEBUG){
            println("Favorite tapped")
        }
        
        EnableFavoriteSetting(favoritesDataModel.getFavorite(atIndex: indexPath.row))

    }
    
    
    
    
    
    /**
    Sets the phone status bar to be light colored for dark background
    
    :returns: UIStatusBarStyle
    */
    override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return UIStatusBarStyle.LightContent
    }
    
    
    func ShowBulbSettings( gestureRecognizer: UILongPressGestureRecognizer){
        
        if(gestureRecognizer.state == UIGestureRecognizerState.Began){
            var point = gestureRecognizer.locationInView(self.bulbCollectionView)
            var indexPath = self.bulbCollectionView.indexPathForItemAtPoint(point)
            if indexPath == nil{
                if(DEBUG){
                    println("Unable to find index")
                }
            } else{
                if(DEBUG){
                    println("indexPath of cell: \(indexPath)")
                }
                
                //self.performSegueWithIdentifier("BulbSettingsNav", sender: indexPath)
            }
        }
        
        if gestureRecognizer.state != UIGestureRecognizerState.Ended{
            return
        }
        
        
    }


    func ApplySettings(){
        self.dismissViewControllerAnimated(true, completion: nil)
        self.bulbCollectionView.reloadData()
    }
    
    func DismissMe() {
        self.bulbCollectionView.reloadData()
        self.dismissViewControllerAnimated(true, completion: nil)
    }
    
    
    //MARK: Notification Methods
    
    /**
    Handles the heartbeat event from the PHNotification
    Updates the Bulbs UI
    
    :param:
    
    :returns:
    */
    func HeartBeatReceived(){
        
        //skip one (this) heartbeat
        if skipNextHeartbeat{
            skipNextHeartbeat = false;
            return
        }
        
        //Successful connect to bridge has been made
        retryConnection = true
        beenConnected = true
        
        self.bulbCollectionView.reloadData()
    }
    
    /**
    
    Handles the not authorized with the bridge PHNotification
    
    :param: void
    
    :returns: void
    
    */
    func NotAuthorized(){
        var hueSDK = (UIApplication.sharedApplication().delegate as! AppDelegate).hueSDK!
        self.performSegueWithIdentifier("pushAuth", sender: self)
    }
    /**
    Handles the unable to connect bridge PHNotification
    
    :param: void
    
    :returns: void
    */
    func NetworkConnectionLost(){
        
        var hueSDK = (UIApplication.sharedApplication().delegate as! AppDelegate).hueSDK!
        
        //Has been connected to the bridge at least once during this session
        
            //returns for one more heartbeat timer

            hueSDK.disableLocalConnection()
        
    }

    
    /**
    Sets all the bubls in the favorite to that favorite setting.
    
    :param: favorite The favorite to be enabled.
    */
    func EnableFavoriteSetting(favorite:Favorite) {
        
        //Favorite is for all lights
        if favorite.isAll {
            var lightState:PHLightState = favorite.allLightState!
            SetGroupLightState("0", lightState)
            
        }
        
        //Iterate over each bulb id and it's lightstate
        for (bulbId, lightState) in favorite.stateMap {
            SetBulbLightState(bulbId, lightState)
        }
    }

    
    
    
    
    
    
    
    
    
    
    
    
}