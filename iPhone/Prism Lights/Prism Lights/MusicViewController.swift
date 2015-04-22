//
//  SecondViewController.swift
//  Prism Lights
//
//  Created by Cody Foltz on 11/19/14.
//  Copyright (c) 2014 Prism. All rights reserved.
//

import UIKit


class MusicViewController: UIViewController {
    
    
    
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
        var hueVal = "";
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
        
        
        var firstOutcome:NSDictionary = outcomes[0] as! NSDictionary;
//        intentView.text = firstOutcome as NSString;
        
        if let intent:NSString = firstOutcome.objectForKey("intent") as? NSString{
            println(intent);
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
        
        if let entities:NSDictionary = firstOutcome.objectForKey("entities") as? NSDictionary{
            
            if let on_off:NSArray = entities.objectForKey("on_off") as? NSArray{
                for el in on_off{
                    if let value:NSString = el["value"] as? NSString{
                        println("on_of value is:" + (value as! String));
                        entitiesView.text = entitiesView.text + "TURNING BULB(S): " + (value as! String) + "\n";
                        onOffVal = (value as! String);
                        onOff = true;
                        if(onOffVal == "off"){
                            if(cache != nil) {
                                for light in cache!.lights.values{
                                    if(light.reachable != 0){
//                                        //println("Light \(light.identifier)  \(light.lightState.description)");
//                                        var lightState = PHLightState();
//                                        if onOffVal == "off"{
//                                            lightState.on = false;
//                                        } else if(onOffVal == "on"){
//                                            lightState.on = true;
//                                        }
//                                        lightState.hue = hueVal.toInt();
//                                        lightState.on = false;
//                                        var bridgeSendAPI = PHBridgeSendAPI();
//                                        bridgeSendAPI.updateLightStateForId(light.identifier, withLightState: lightState, completionHandler: nil);
                                        var lightState = PHLightState()
                                        lightState.on = false;
                                        var bridgeSend = PHBridgeSendAPI()
                                        bridgeSend.updateLightStateForId(light.identifier, withLightState: lightState, completionHandler: nil)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            if let color:NSArray = entities.objectForKey("color") as? NSArray{
                for el in color{
                    if let value:NSString = el["value"] as? NSString{
                        var colorPlusHueValue = value.componentsSeparatedByString("~!~")
                        var colorName:String = colorPlusHueValue[0] as! String
                        var colorHue:String = colorPlusHueValue[1] as! String
                        println("Color is: " + (value as! String) );
                        entitiesView.text = entitiesView.text + "COLOR: " + (colorName) + "\n";
                        entitiesView.text = entitiesView.text + "HUE VALUE: " + (colorHue) + "\n";
                        colorVal = value.uppercaseString;
                    }
                    
                    /* Taking this out for now as per follow-up with Laurent from Wit.ai:
                    "Thanks for reaching out. Yes we do have a regression here. We will fix it soon.
                    Sorry for the inconvenience!
                    Laurent"
                    if let tempHueVal:NSString = el["metadata"] as? NSString{
                        println("Hue value is: " + (tempHueVal as! String) );
                        entitiesView.text = entitiesView.text + "HUE VALUE: " + (tempHueVal as! String) + "\n";
                        hueVal = (tempHueVal as! String);
                        //colorVal = value.uppercaseString;
                    }
*/
                }
            }
            
            
            
            if let alarm:NSArray = entities.objectForKey("alarm") as? NSArray{
                for el in alarm{
                    if let value:NSString = el["value"] as? NSString{
                        println("alarm is: " + (value as! String) );
                        entitiesView.text = entitiesView.text + "SETTING AN: " + (value as! String) + "\n";
                        setAlarm = true;
                    }
                }
            }
            
            if let timer:NSArray = entities.objectForKey("timer") as? NSArray{
                for el in timer{
                    if let value:NSString = el["value"] as? NSString{
                        println("timer is: " + (value as! String) );
                        entitiesView.text = entitiesView.text + "SETTING A: " + (value as! String) + "\n";
                        setTimer = true;
                    }
                }
            }
            
            
            if let duration:NSArray = entities.objectForKey("duration") as? NSArray{
                if let norm:NSDictionary = duration[0].objectForKey("normalized") as? NSDictionary{
                    if let val:NSInteger = norm.objectForKey("value") as? NSInteger{
                        println("duration value: \(val)")
                        entitiesView.text = entitiesView.text + "DURATION:  \(val)" + "\n";
                        durationVal = val;
                    }
                    if let unit:NSString = norm.objectForKey("unit") as? NSString{
                        println("duration unit: \(unit)")
                        entitiesView.text = entitiesView.text + "DURATION UNIT:  \(unit)" + "\n";
                        durationUnit = (unit as! String);
                    }
                }
            }
            
            if let b:NSArray = entities.objectForKey("bulb") as? NSArray{
                for el in b{
                    if let value:NSString = el["value"] as? NSString{
                        println("bulb is: " + (value as! String) );
                        entitiesView.text = entitiesView.text + "BULB NAME: " + (value as! String) + "\n";
                        bulb = (value as! String);
                    }
                }
            }
            
            if let dim:NSArray = entities.objectForKey("dim") as? NSArray{
                entitiesView.text = entitiesView.text + "DIMMING BULBS\n";
                if (cache == nil){
                    return;
                }
                for light in cache!.lights.values{
                    if((light.reachable) != nil){
                    //println("Light \(light.identifier)  \(light.lightState.description)");
                    
//                    var convertedBrightness = ((light.brightness * 254.0) / 100);
//                    var currentBrightness = light.brightness;
                    var currentBrightness = light.brightness - 85;
                    var lightState = PHLightState();
                    if onOffVal == "off"{
                        lightState.on = false;
                    } else if(onOffVal == "on"){
                        lightState.on = true;
                    }
//                    lightState.hue = hueVal.toInt();
                    lightState.on = true;
                    lightState.saturation = 254;
                    
                    lightState.brightness = (currentBrightness - 85);
                    
                    
                    var bridgeSendAPI = PHBridgeSendAPI();
                    bridgeSendAPI.updateLightStateForId(light.identifier, withLightState: lightState, completionHandler: nil);
                    }
                }
//                var lightState = PHLightState()
//                lightState.brightness = Int(254)
//                var bridgeSend = PHBridgeSendAPI()
//                bridgeSend.updateLightStateForId(self.bulbId, withLightState: lightState, completionHandler: nil)
//                
//                var cache = PHBridgeResourcesReader.readBridgeResourcesCache()
//                var light = (cache.lights?[bulbId!]) as PHLight
//                light.lightState.brightness = Int(254*sender.value)
            }
            
            if let brighter:NSArray = entities.objectForKey("brighter") as? NSArray{
                entitiesView.text = entitiesView.text + "MAKING BULBS BRIGHTER\n";
                if (cache == nil){
                    return;
                }
                for light in cache!.lights.values{
                    if((light.reachable) != nil){
                    //println("Light \(light.identifier)  \(light.lightState.description)");
                    
//                    var convertedBrightness = ((light.brightness * 254.0) / 100);
                    //                    var currentBrightness = light.brightness;
                    var currentBrightness = light.brightness + 85;
                    var lightState = PHLightState();
                    if onOffVal == "off"{
                        lightState.on = false;
                    } else if(onOffVal == "on"){
                        lightState.on = true;
                    }
                    //                    lightState.hue = hueVal.toInt();
                    lightState.on = true;
                    lightState.saturation = 254;
                    
                    lightState.brightness = currentBrightness;
                    
                    
                    var bridgeSendAPI = PHBridgeSendAPI();
                    bridgeSendAPI.updateLightStateForId(light.identifier, withLightState: lightState, completionHandler: nil);
                    }
                }
            }
            
            if let date_time:NSArray = entities.objectForKey("datetime") as? NSArray{
                for el in date_time{
                    if let value:NSString = el["grain"] as? NSString{
                        println("grain of datetime is: " + (value as! String) );
//                        entitiesView.text = entitiesView.text + "DATETIME GRAIN: " + value + "\n";
                        datetimegrain = (value as! String);
                    }
                    if let value:NSString = el["value"] as? NSString{
                        println("datetime is: " + (value as! String) );
                        entitiesView.text = entitiesView.text + "DATETIME: \n" + (value as! String) + "\n";
                        datetimeval = (value as! String);
                    }
                }
            }
            
            if let ms:NSArray = entities.objectForKey("message_subject") as? NSArray{
                for el in ms{
                    if let value:NSString = el["value"] as? NSString{
                        println("message_subject is: " + (value as String) );
                        entitiesView.text = entitiesView.text + "MESSAGE_SUBJECT: " + (value as! String ) + "\n";
                        message_subject = (value as! String);
                    }
                }
            }
            
        }
        if let confidence:NSString = firstOutcome.objectForKey("confidence") as? NSString{
            println(confidence);
            entitiesView.text = entitiesView.text + "CONFIDENCE: " + (confidence as! String) + "\n";
        }
        if let text:NSString = firstOutcome.objectForKey("_text") as? NSString{
            println(text);
            intentView.text = (text as! String);
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
        

        //var COLORDIC:NSDictionary = ["RED":65280, "YELLOW":12950, "WHITE":36210, "BLUE":46920, "PURPLE":56100, "PINK":53505, "ORANGE":10000, "GREEN": 25500];
        if(cache != nil) {
        for light in cache!.lights.values{
            if(light.reachable != 0){
            //println("Light \(light.identifier)  \(light.lightState.description)");
            var lightState = PHLightState();
            if onOffVal == "off"{
                lightState.on = false;
            } else if(onOffVal == "on"){
                lightState.on = true;
            }
                lightState.hue = hueVal.toInt();
                lightState.on = true;
                lightState.saturation = 254;
            
            lightState.brightness = 254;
            

            var bridgeSendAPI = PHBridgeSendAPI();
            bridgeSendAPI.updateLightStateForId(light.identifier, withLightState: lightState, completionHandler: nil);
            }
        }
        }
    
    }

    
   
}

}

