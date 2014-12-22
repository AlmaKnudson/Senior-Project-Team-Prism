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
    
    
    var cache :PHBridgeResourcesCache? = nil;
    var hueSDK :PHHueSDK?
    var x :PHNotificationManager?
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        Wit.sharedInstance().delegate = self;
        
        //Check that we are connected to bridge.
        if ((UIApplication.sharedApplication().delegate as AppDelegate).hueSDK!.localConnected()){
            cache = PHBridgeResourcesReader.readBridgeResourcesCache();
        }
        
        
        Wit.sharedInstance().accessToken = "2UZT7OIHBRHNJTFZLOW222ND5SVNRYM7";
        Wit.sharedInstance().detectSpeechStop = WITVadConfig.DetectSpeechStop;

        
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
    
    
    /**
    * Called when the Wit request is completed.
    * param outcomes a NSDictionary of outcomes returned by the Wit API. Outcomes are ordered by confidence, highest first. Each outcome contains (at least) the following keys:
    *       intent, entities[], confidence, _text. For more information please refer to our online documentation: https://wit.ai/docs/http/20141022#get-intent-via-text-link
    *
    * param messageId the message id returned by the api
    * param customData any data attached when starting the request. See [Wit sharedInstance toggleCaptureVoiceIntent:... (id)customData] and [[Wit sharedInstance] start:... (id)customData];
    * param error Nil if no error occurred during processing
    */
    func witDidGraspIntent(outcomes: [AnyObject]!, messageId: String!, customData: AnyObject!, error e: NSError!) {
        //initialize flags
        var onOff = false;
        var color = false;
        //default color
        var onOffVal = "off"
        var colorVal = "white";
        //change all bulbs by default
        var bulb = "all lights"
        var message_subject = "";
        //brightness not handled for demo
        var brightness = 100;
        
    
        //duration and date time
        var durationVal = 0;
        var durationUnit = "second";
        var datetimegrain = "";
        var datetimeval = "";
        
        //Boolean flags for demo:
        var changeBulb = false;
        var changeBulbs = false;
        var setTimer = false;
        var setAlarm = false;
//        
//        var json:NSData = NSJSONSerialization.dataWithJSONObject(outcomes, options: NSJSONWritingOptions.PrettyPrinted, error: nil)!;
//        var jsonString = NSString(data: json, encoding: NSUTF8StringEncoding);
//        println(jsonString);
        if outcomes == nil{
            return;
        }
        
        
        var firstOutcome:NSDictionary = outcomes[0] as NSDictionary;
        if let intent:NSString = firstOutcome.objectForKey("intent") as? NSString{
            println(intent);
        }
        if let entities:NSDictionary = firstOutcome.objectForKey("entities") as? NSDictionary{
            
            if let color:NSArray = entities.objectForKey("color") as? NSArray{
                for el in color{
                    if let value:NSString = el["value"] as? NSString{
                        println("Color is: " + value);
                        colorVal = value.uppercaseString;
                    }
                }
            }
            
            if let on_off:NSArray = entities.objectForKey("on_off") as? NSArray{
                for el in on_off{
                    if let value:NSString = el["value"] as? NSString{
                        println("on_of value is:" + value);
                        onOffVal = value;
                        onOff = true;
                    }
                }
            }
            
            if let alarm:NSArray = entities.objectForKey("alarm") as? NSArray{
                for el in alarm{
                    if let value:NSString = el["value"] as? NSString{
                        println("alarm is: " + value);
                        setAlarm = true;
                    }
                }
            }
            
            if let timer:NSArray = entities.objectForKey("timer") as? NSArray{
                for el in timer{
                    if let value:NSString = el["value"] as? NSString{
                        println("timer is: " + value);
                        setTimer = true;
                    }
                }
            }
            
            
            if let duration:NSArray = entities.objectForKey("duration") as? NSArray{
                if let norm:NSDictionary = duration[0].objectForKey("normalized") as? NSDictionary{
                    if let val:NSInteger = norm.objectForKey("value") as? NSInteger{
                        println("duration value: \(val)")
                        durationVal = val;
                    }
                    if let unit:NSString = norm.objectForKey("unit") as? NSString{
                        println("duration unit: \(unit)")
                        durationUnit = unit;
                    }
                }
            }
            
            if let b:NSArray = entities.objectForKey("bulb") as? NSArray{
                for el in b{
                    if let value:NSString = el["value"] as? NSString{
                        println("bulb is: " + value);
                        bulb = value;
                    }
                }
            }
            
            if let date_time:NSArray = entities.objectForKey("datetime") as? NSArray{
                for el in date_time{
                    if let value:NSString = el["grain"] as? NSString{
                        println("grain of datetime is: " + value);
                        datetimegrain = value;
                    }
                    if let value:NSString = el["value"] as? NSString{
                        println("datetime is: " + value);
                        datetimeval = value;
                    }
                }
            }
            
            if let ms:NSArray = entities.objectForKey("message_subject") as? NSArray{
                for el in ms{
                    if let value:NSString = el["value"] as? NSString{
                        println("message_subject is: " + value);
                        message_subject = value;
                    }
                }
            }
            
        }
        if let confidence:NSString = firstOutcome.objectForKey("confidence") as? NSString{
            println(confidence);
        }
        if let text:NSString = firstOutcome.objectForKey("_text") as? NSString{
            println(text);
            intentView.text = text;
        }
        
        //Criteria for determining what to do with lights.
        
        
        if (setAlarm) {
            println("Setting an alarm for \(durationVal)  \(durationUnit) in the future");
            return;
        } else if (setTimer){
            println("Setting time for \(datetimeval). Grain \(datetimegrain)");
            return;
        }
        
        
        println("Will send-- on_off: \(onOffVal). Color: \(colorVal). Bulb: \(bulb). Message_subject: \(message_subject)." );
        

        var COLORDIC:NSDictionary = ["RED":65280, "YELLOW":12950, "WHITE":36210, "BLUE":46920, "PURPLE":56100, "PINK":53505, "ORANGE":10000, "GREEN": 25500];
        
        for light in cache!.lights.values{
            
            //println("Light \(light.identifier)  \(light.lightState.description)");
            var lightState = PHLightState();
            if onOffVal == "off"{
                lightState.on = false;
            } else if(onOffVal == "on"){
                lightState.on = true;
            }
            if let hue:NSInteger = COLORDIC[colorVal] as? NSInteger{
                lightState.hue = hue;
                lightState.on = true;
                lightState.saturation = 254;
            }
            lightState.brightness = 254;
            
//            if light.lightState.on == 1{
//                light.lightState.on = false
//                lightState.on = false
//            } else{
//                lightState.on = true
//                light.lightState.on = true
//            }
            var bridgeSendAPI = PHBridgeSendAPI();
            bridgeSendAPI.updateLightStateForId(light.identifier, withLightState: lightState, completionHandler: nil);
        }
    
    }
    
   
}



