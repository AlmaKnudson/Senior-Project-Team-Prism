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
    
    
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    //
    //    override init() {
    //        super.init()
    //    }
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: NSBundle?) {
        super.init(nibName: nil, bundle: nil)
    }
    
    
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        Wit.sharedInstance().delegate = self;
        
        //Check that we are connected to bridge.
        if ((UIApplication.sharedApplication().delegate as! AppDelegate).hueSDK!.localConnected()){
            cache = PHBridgeResourcesReader.readBridgeResourcesCache();
        }
        
        
        Wit.sharedInstance().accessToken = "SAIGZN56HURQBH5PSTPPQQ545ZNVBSSF";
        Wit.sharedInstance().detectSpeechStop = WITVadConfig.DetectSpeechStop;
        
        
        self.setupUI();
    }
    
    func setupUI(){
        var screen:CGRect = UIScreen.mainScreen().bounds;
        var w:CGFloat = 100;
        var rect:CGRect = CGRectMake(screen.size.width/2 - w/2, 60, w, 100);
        witButton = WITMicButton(frame: rect);
        witButton.backgroundColor = UIColor.blackColor();
        self.view.backgroundColor = UIColor.blackColor();
        self.view.addSubview(witButton);
        
        //create the label
        intentView = UILabel(frame: CGRectMake(0, 200, screen.size.width, 50));
        intentView.textAlignment = NSTextAlignment.Center;
        intentView.backgroundColor = UIColor.blackColor();
        intentView.textColor = UIColor.whiteColor();
        entitiesView = UITextView(frame: CGRectMake(0, 250, screen.size.width, screen.size.height - 300));
        entitiesView.backgroundColor = UIColor.blackColor();
        self.view.addSubview(entitiesView);
        self.view.addSubview(intentView);
        
        intentView.text = "Intent will show up here";
        entitiesView.textAlignment = NSTextAlignment.Center;
        entitiesView.textColor = UIColor.whiteColor();
        entitiesView.text = "Entities will show up here";
        entitiesView.editable = false;
        entitiesView.font = UIFont.systemFontOfSize(17);
        
        statusView = UILabel(frame: CGRectMake(0, 150, screen.size.width, 50));
        statusView.textAlignment = NSTextAlignment.Center;
        statusView.textColor = UIColor.whiteColor();
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
        statusView.text = "Listening...";
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
        var onOff = false; //onOff = false when off and true when on or toggle
        var color = false;
        //default color
        var onOffVal = "off"
        var colorVal = "white";
        var hueVal = "36210"; //default hue color for white
        //brightness not handled for demo
        var brightness = 100; //brightness in % of [0-254]
        var brighter = true;
        var adjustBrightness = false;
        
        
        //duration and date time
        var durationVal = 0;
        var durationUnit = "second";
        var datetimegrain = "";
        var datetimeval = "";
        
        //Boolean flags for demo:
        var setAlarm = false;
        
        //        var json:NSData = NSJSONSerialization.dataWithJSONObject(outcomes, options: NSJSONWritingOptions.PrettyPrinted, error: nil)!;
        //        var jsonString = NSString(data: json, encoding: NSUTF8StringEncoding);
        //        println(jsonString);
        if outcomes == nil{
            return;
        }
        
        
        var firstOutcome:NSDictionary = outcomes[0] as! NSDictionary;
        //        intentView.text = firstOutcome as NSString;
        
        if let spokenWords:NSString = firstOutcome.objectForKey("_text") as? NSString{
//            entitiesView.text = spokenWords as String;
            statusView.text = spokenWords as String;
        }
        
        //Intent will always be "lights" because we are only controlling lights for now.
        if let intent:NSString = firstOutcome.objectForKey("intent") as? NSString{
            println(intent)
        }
        //        if(intent.equals("alarm")){
        //
        //            if (entities.containsKey("datetime") ){
        //                JsonElement datetime =  entities.get("datetime");
        //                String dateString = datetime.getAsJsonArray().get(0).getAsJsonObject().get("value").getAsJsonObject().get("from").getAsString();
        //                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        //                try {
        //                    Date myDate = sdf.parse(dateString);
        //                    witResponse.setText("Setting alarm for:\n" + myDate.toString());
        //
        //                } catch (Exception e){
        //                    witResponse.setText("Error parsing the date: " + dateString);
        //                }
        //            }
        //            //                witResponse.setText();
        //        }
        
        
        //There will always be entities...
        if let entities:NSDictionary = firstOutcome.objectForKey("entities") as? NSDictionary{
            //Grab on_off value if it exists
            if let on_off:NSArray = entities.objectForKey("on_off") as? NSArray{
                for el in on_off{
                    if let value:NSString = el["value"] as? NSString{
                        if(value == "off"){
                            onOff = false
                        } else {
                            onOff = true
                        }
                    }
                }
            }
            //Grab color value if it exists:
            if let color:NSArray = entities.objectForKey("color") as? NSArray{
                for el in color{
                    if let value:NSString = el["value"] as? NSString{
                        var colorPlusHueValue = value.componentsSeparatedByString("~!~")
                        if(colorPlusHueValue.count != 2){
                            break
                        } else {
                            if let colorName:String = colorPlusHueValue[0] as? String{
                                if let colorHue:String = colorPlusHueValue[1] as? String{
                                    colorVal = colorName
                                    hueVal = colorHue
                                }
                            }
                        }
                    }
                }
            }
            //Grab whether alarm intent exists
            if let alarm:NSArray = entities.objectForKey("alarm") as? NSArray{
                setAlarm = true
            }
            
            if let date_time:NSArray = entities.objectForKey("datetime") as? NSArray{
                if let el: NSDictionary = date_time.objectAtIndex(0) as? NSDictionary{
                    if let value:NSString = el["grain"] as? NSString{
                        //Grain seems to always be minute and doesn't really matter too much.
                    }
                    if let value:NSString = el["value"] as? NSString{
                        //2015-04-29T08:00:00.000-06:00
                        datetimeval = value as! String
                    }
                }
            }
            
            if let duration:NSArray = entities.objectForKey("duration") as? NSArray{
                if let norm:NSDictionary = duration[0].objectForKey("normalized") as? NSDictionary{
                    if let val:NSInteger = norm.objectForKey("value") as? NSInteger{
                        //                        println("duration value: \(val)")
                        //                        entitiesView.text = entitiesView.text + "DURATION:  \(val)" + "\n";
                        durationVal = val
                    }
                    if let unit:NSString = norm.objectForKey("unit") as? NSString{
                        //                        println("duration unit: \(unit)")
                        //                        entitiesView.text = entitiesView.text + "DURATION UNIT:  \(unit)" + "\n";
                        durationUnit = (unit as! String)
                    }
                }
            }
            
            
            if let brightness:NSArray = entities.objectForKey("brightness") as? NSArray{
                //                entitiesView.text = entitiesView.text + "MAKING BULBS BRIGHTER\n";
                if let val:NSString = brightness[0].objectForKey("value") as? NSString{
                    if (val == "brighter"){
                        adjustBrightness = true
                        brighter = true
                    } else if (val == "dimmer"){
                        adjustBrightness = true
                        brighter = false
                    }
                }
            }
            
            
        }
        
        
        
        
        /*
        {
        "msg_id" : "2e7103a6-8324-49a0-826f-751f0227ea1d",
        "_text" : "bulbs dimmer",
        "outcomes" : [ {
        "_text" : "bulbs dimmer",
        "intent" : "lights",
        "entities" : {
        "bulbname" : [ {
        "suggested" : true,
        "value" : "bulbs",
        "type" : "value"
        } ],
        "on_off" : [ {
        "value" : "on"
        } ],
        "brightness" : [ {
        "value" : "dimmer"
        } ]
        },
        "confidence" : 1.0
        } ]
        ---------------------------------------------------------
        {
        "msg_id" : "274c17e4-d78e-4251-8062-e435967fa8b4",
        "_text" : "wake me up at 8:00AM",
        "outcomes" : [ {
        "_text" : "wake me up at 8:00AM",
        "intent" : "lights",
        "entities" : {
        "datetime" : [ {
        "grain" : "minute",
        "value" : "2015-04-29T08:00:00.000-06:00",
        "type" : "value",
        "values" : [ {
        "type" : "value",
        "value" : "2015-04-29T08:00:00.000-06:00",
        "grain" : "minute"
        }, {
        "type" : "value",
        "value" : "2015-04-30T08:00:00.000-06:00",
        "grain" : "minute"
        }, {
        "type" : "value",
        "value" : "2015-05-01T08:00:00.000-06:00",
        "grain" : "minute"
        } ]
        } ],
        "on_off" : [ {
        "value" : "on"
        } ],
        "alarm" : [ {
        "value" : "alarm"
        } ]
        },
        "confidence" : 1.0
        } ]
        
        ----------------------------------------------------------------
        {
        "msg_id" : "214af95e-c0f1-4ca5-ba75-c3d9b23b705d",
        "_text" : "set 5 minute timer",
        "outcomes" : [ {
        "_text" : "set 5 minute timer",
        "intent" : "lights",
        "entities" : {
        "duration" : [ {
        "minute" : 5,
        "value" : 5,
        "unit" : "minute",
        "normalized" : {
        "value" : 300,
        "unit" : "second"
        }
        } ],
        "on_off" : [ {
        "value" : "off"
        } ],
        "alarm" : [ {
        "value" : "timer"
        } ]
        },
        "confidence" : 1.0
        } ]
        */
        
        
        //Now it is time to send appropriate commands to the light/group.... 
        var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
        let lightState:PHLightState
        
        //Double check for the existence of the cahce:
        if(cache != nil){
            /*
            //initialize flags
            var onOff = false; //onOff = false when off and true when on or toggle
            var color = false;
            //default color
            var onOffVal = "off"
            var colorVal = "white";
            var hueVal = "";
            //brightness not handled for demo
            var brightness = 100;
            var brighter = true;
            var adjustBrightness = false;
            
            
            //duration and date time
            var durationVal = 0;
            var durationUnit = "second";
            var datetimegrain = "";
            var datetimeval = "";
            
            //Boolean flags for demo:
            var setAlarm = false;
            */
            
            
            //Do we set alarm/timer?
            if(setAlarm){
                if(durationVal != 0){
                    //We found a duration... Setting Timer
                    intentView.text = "Setting Timer for \(durationVal) seconds."
                } else {
                    var newSchedule:PHSchedule = PHSchedule()
                    //Name Schedule: 
                    newSchedule.name = "\(datetimeval)"
                    newSchedule.localTime = true
                    var date : NSDate
                    var dateFormatter : NSDateFormatter = NSDateFormatter()
                    //Specify Format of String to Parse
                    dateFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ"
                    dateFormatter.locale = NSLocale.currentLocale()
                    dateFormatter.timeZone = NSTimeZone(abbreviation: "MST")
//                    [dateFormatter setTimeZone: [NSTimeZone timeZoneWithAbbreviation: @"MST"]];
                    
                    /*

                    NSDateFormatter *dateFormatter=[[NSDateFormatter alloc]init];
                    
                    NSString *currentDateString = @"2014-01-08T21:21:22.737+05:30";
                    [dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ"];
                    NSDate *currentDate = [dateFormatter dateFromString:currentDateString];
                    
                    NSLog(@"CurrentDate:%@", currentDate);
*/
                    
                    date = dateFormatter.dateFromString(datetimeval)!
                    intentView.text = "Setting Alarm \(datetimeval)"
                    newSchedule.date = date
                    newSchedule.identifier = "20"
                    
                    var lightState:PHLightState = PHLightState()
                    
                    lightState.on = 1
                    newSchedule.state = lightState
                    newSchedule.lightIdentifier = "1"
                    
                    var bridgeSendAPI:PHBridgeSendAPI = PHBridgeSendAPI()
                    // bridgeSendAPI.createSchedule(newSchedule, completionHandler: nil)
            
                    
                    /*
                    // Create a new schedule object
                    PHSchedule *newSchedule = [[PHSchedule alloc] init];
                    
                    // Give it a name
                    newSchedule.name = @”Turn off light 1 schedule”;
                    
                    // Set schedule to use local time, so you can specify you date in the same timezone as your bridge.
                    newSchedule.localTime = YES;
                    
                    // Set a date for when you want to schedule your action.
                    newSchedule.date = futureDate;
                    
                    //Create light state object for action of the schedule
                    PHLightState *lightState = [[PHLightState alloc] init];
                    
                    // Configure light state to turn lights of
                    lightState.on = @NO;
                    // Set light state to schedule object
                    newSchedule.lightState = lightState;
                    // Set light identifier that should have its light state updated
                    newSchedule.lightIdentifier = @”1”;
                    
                    // Create PHBridgeSendAPI object
                    PHBridgeSendApi *bridgeSendAPI = [PHBridgeSendAPI alloc] init];
                    
                    // Call create group on bridge API
                    [bridgeSendAPI createScheduleWithSchedule:newSchedule completionHandler:^(NSArray *errors) {
                    if (!errors){
                    // Create successful
                    } else {
                    // Error occurred
                    }
                    }
                    */
                }
                
            } else {
                /*
                //initialize flags
                var onOff = false; //onOff = false when off and true when on or toggle
                var color = false;
                //default color
                var onOffVal = "off"
                var colorVal = "white";
                var hueVal = "";
                //brightness not handled for demo
                var brightness = 100;
                var brighter = true;
                var adjustBrightness = false;
                
*/
                
                
                
                var bridgeSendAPI:PHBridgeSendAPI = PHBridgeSendAPI()
                
                //Just set color/brightness/on_off accordingly
                intentView.text = "Need to adjust color/brightness/on_off"
                if(hueVal != ""){
                    var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
                    if(cache != nil){
                        for light in cache!.lights.values{
                            var currentLightState:PHLightState = light.lightState as PHLightState
                            if((currentLightState.reachable) != nil) {
                                
                                var lightState = PHLightState();
                                lightState.on = onOff;
                                var currentBrightness = currentLightState.brightness
                                if(adjustBrightness){
                                    if(brighter){
                                        currentBrightness = (NSNumber) (double: min(254, Double(currentBrightness) * 1.3))
                                    } else {
                                        currentBrightness = (NSNumber) (double: max(0, Double(currentBrightness) * 0.7))
                                    }
                                    lightState.on = true;
                                }
                                lightState.hue = NSNumber(integer: hueVal.toInt()!);
                                lightState.saturation = 254;
                                lightState.brightness = currentBrightness;
                                var bridgeSendAPI = PHBridgeSendAPI();
                                bridgeSendAPI.updateLightStateForId(light.identifier, withLightState: lightState, completionHandler: nil);
                            }
                        }
                    }
                    if(onOff){
                        intentView.text = "Turning lights \(colorVal)"
                    } else {
                        intentView.text = "Turning lights off"
                    }
                }
                if(adjustBrightness == true){
                    //Default, increment brightness by 30%.
                    if(brighter){
                        
                    } else {
                        
                    }
                }
            }
            
            //Do we set color?
            
            //Do we adjust brightness?
            
            //
            
            
        }
        
    }
    
    
    
}



