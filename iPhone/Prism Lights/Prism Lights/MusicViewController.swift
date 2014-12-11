//
//  SecondViewController.swift
//  Prism Lights
//
//  Created by Cody Foltz on 11/19/14.
//  Copyright (c) 2014 Prism. All rights reserved.
//

import UIKit


class MusicViewController: UIViewController, WitDelegate {
    
    var statusView:UILabel = UILabel();
    var intentView:UILabel = UILabel();
    var entitiesView:UITextView = UITextView();
    var witButton:WITMicButton = WITMicButton();
    
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        Wit.sharedInstance().delegate = self;
        
        
        Wit.sharedInstance().accessToken = "2UZT7OIHBRHNJTFZLOW222ND5SVNRYM7";
        // WITVadConfig.DetectSpeechStop;
        Wit.sharedInstance().detectSpeechStop = WITVadConfig.DetectSpeechStop;
        //WITVadConfig.Full;
        //        [Wit sharedInstance].accessToken = @"xxx"; // replace xxx by your Wit.AI access token
        //        //enabling detectSpeechStop will automatically stop listening the microphone when the user stop talking
        //        [Wit sharedInstance].detectSpeechStop = WITVadConfigDetectSpeechStop;
        //        return YES;
        
        self.setupUI();
    }
    
    func setupUI(){
        var screen:CGRect = UIScreen.mainScreen().bounds;
        var w:CGFloat = 100;
        var rect:CGRect = CGRectMake(screen.size.width/2 - w/2, 60, w, 100);
        witButton = WITMicButton(frame: rect);
        self.view.addSubview(witButton);
        
        //create the label
        intentView = UILabel(frame: CGRectMake(0, 200, screen.size.width, 50));
        intentView.textAlignment = NSTextAlignment.Center;
        entitiesView = UITextView(frame: CGRectMake(0, 250, screen.size.width, screen.size.height - 300));
        entitiesView.backgroundColor = UIColor.purpleColor();
        self.view.addSubview(entitiesView);
        self.view.addSubview(intentView);
        
        intentView.text = "Intent will show up here";
        entitiesView.textAlignment = NSTextAlignment.Center;
        entitiesView.text = "Entities will show up here";
        entitiesView.editable = false;
        entitiesView.font = UIFont.systemFontOfSize(17);
        
        statusView = UILabel(frame: CGRectMake(0, 150, screen.size.width, 50));
        statusView.textAlignment = NSTextAlignment.Center;
        self.view.addSubview(statusView);
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func witActivityDetectorStarted() {
        statusView.text =  "Just listening... Waiting for voice activity";
    }
    
    func witDidStartRecording() {
        statusView.text = "Witting...";
        entitiesView.text = "";
    }
    
    func witDidStopRecording() {
        statusView.text = "Processing...";
        entitiesView.text = "";
    }
    
    
    //witDidGraspIntent:(NSArray *)outcomes messageId:(NSString *)messageId customData:(id) customData error:(NSError*)e;
    func witDidGraspIntent(outcomes: [AnyObject]!, messageId: String!, customData: AnyObject!, error e: NSError!) {
        
        intentView.text = "MADE IT";
        var s = "";
        
        let o = outcomes[0] as NSObject;
        var intent = o.valueForKey("intent") as NSString;
        //
        //        var outcome = o.objectAtIndex(0) as NSDictionary;
        //        var intent = outcome.objectForKey("intent") as NSString;
        s = s + intent + " ";
        
        intentView.text = s;
        
        // intentView.text = NSString(data: outcomes.removeAtIndex(0), NSUTF8StringEncoding
        /*
        if (e.localizedDescription != ""){
        NSLog("[Wit] error: " + e.localizedDescription);
        return;
        }
        
        var firstOutcome:NSDictionary = outcomes.objectAtIndex(0) as NSDictionary;
        
        var intent:NSString = firstOutcome.objectForKey("intent") as NSString;
        
        intentView.text = "intent = " + intent;
        statusView.text = "";
        
        var json = NSData();
        var error = NSError();
        if (NSJSONSerialization.isValidJSONObject(outcomes))
        {
        entitiesView.textAlignment = NSTextAlignment.Left;
        
        json = NSJSONSerialization.dataWithJSONObject(outcomes, options: NSJSONWritingOptions.PrettyPrinted, error: nil)!;
        
        var jsonString = NSString(data: json, encoding: NSUTF8StringEncoding);
        
        NSLog("JSON: " + jsonString!);
        entitiesView.text = jsonString;
        } 
        */
        
    }
    
    
}

