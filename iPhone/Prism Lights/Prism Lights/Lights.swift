//
//  Lights.swift
//  Prism Lights
//
//  Created by Cody Foltz on 1/27/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation



/**
Gets a list of all lights that have been discovered by the bridge.

:param: ipAddress   IP address of the bridge
:param: username    whitelisted user of the bridge
:param: dataHandler function that will handle the light objects
*/
func GetAllLights(ipAddress: String, username: String, dataHandler:(lights:[Light], error: String?)->()){
    //GET <IPAddress>/api/<username>/lights
    
    var address = "\(ipAddress)/api/\(username)/lights";
    
    // Get the JSON data from the address.
    // Completion block creates Light objects and returns them to the passed in completion
    HttpGet(address){
        (responseReceived, msg, dataNull) -> () in
        
        var lights :[Light] = []
        
        if(!responseReceived){
            return dataHandler(lights: [], error: msg);
        }
        var data = dataNull!
        
        //var strData = NSString(data: data, encoding: NSUTF8StringEncoding)
        //println("Body: \(strData)")
        var err: NSError?
        var JSONDict = NSJSONSerialization.JSONObjectWithData(data, options: .MutableLeaves, error: &err) as? NSDictionary
        
        
        //Checks each level for null and then gets the value
        if let lightDict = JSONDict{
            for light in lightDict{
                
                //Top level dict for lightbulb
                if let lightData = light.value as? Dictionary<String, AnyObject>{
                    if let name = lightData["name"] as? String{
                        if let type = lightData["type"] as? String{
                            if let modelid = lightData["modelid"] as? String{
                                if let swversion = lightData["swversion"] as? String{
                                    if let state = (light.value["state"] as? Dictionary<String,AnyObject>) {
                                        if let lightState :LightState = StateDictionaryToLightState(state){
                                            //Create light bulb
                                            var lightBulb = Light(type: type, name: name, modelID: modelid, swversion: swversion, state: lightState)
                                            lights.append(lightBulb);
                                        }
                                    }// End of state
                                }// End of version
                            }// End of modelid
                        }// End of type
                    }// End of name
                }// End Top level dict
            }// End for-in
        }
        
        return dataHandler(lights: lights, error: nil)
        
        
    }// End completion Handler
    
}// End function


/**
Gets a list of lights that were discovered the last time a search for new lights was performed. The list of new lights is always deleted when a new search is started.

:param: ipAddress IP address of the bridge
:param: username  Whitelisted user name
*/
func GetNewLights(ipAddress: String, username: String){
    //GET <IPAddress>/api/<username>/lights/new
    
    var address = "\(ipAddress)/api/\(username)/lights/new";
    
    
    HttpGet(address){
        (responseReceived, msg, dataNull) -> () in
        
        var lights :[Light] = []
        
        if(!responseReceived){
            return dataHandler(lights: [], error: msg);
        }
        var data = dataNull!
        
        //var strData = NSString(data: data, encoding: NSUTF8StringEncoding)
        //println("Body: \(strData)")
        var err: NSError?
        var JSONDict = NSJSONSerialization.JSONObjectWithData(data, options: .MutableLeaves, error: &err) as? NSDictionary
        
        
        //Checks each level for null and then gets the value
        if let lightDict = JSONDict{
            for light in lightDict{
                
                //Top level dict for lightbulb
                if let lightData = light.value as? Dictionary<String, AnyObject>{
                    if let name = lightData["name"] as? String{
                        if let type = lightData["type"] as? String{
                            if let modelid = lightData["modelid"] as? String{
                                if let swversion = lightData["swversion"] as? String{
                                    if let state = (light.value["state"] as? Dictionary<String,AnyObject>) {
                                        if let lightState :LightState = StateDictionaryToLightState(state){
                                            //Create light bulb
                                            var lightBulb = Light(type: type, name: name, modelID: modelid, swversion: swversion, state: lightState)
                                            lights.append(lightBulb);
                                        }
                                    }// End of state
                                }// End of version
                            }// End of modelid
                        }// End of type
                    }// End of name
                }// End Top level dict
            }// End for-in
        }
        
        return dataHandler(lights: lights, error: nil)
        
        
    }
    /**
    
    Response example
    {
    "7": {"name": "Hue Lamp 7"},
    "8": {"name": "Hue Lamp 8"},
    "lastscan": "2012-10-29T12:00:00"
    }
    **/
    

    
}



/**
Starts a search for new lights. As of 1.3 will also find switches (e.g. "tap")

The bridge will search for 1 minute and will add a maximum of 15 new lights. To add further lights, the command needs to be sent again after the search has completed. If a search is already active, it will be aborted and a new search will start.

When the search has finished, new lights will be available using the get new lights command. In addition, the new lights will now be available by calling get all lights or by calling get group attributes on group 0. Group 0 is a special group that cannot be deleted and will always contain all lights known by the bridge.

Body as of 1.1 (Optional)
{"deviceid":["45AF34","543636","34AFBE"]}

Response
Contains a list with a single item that details whether the search started successfully.

Response example
[ { "success": { "/lights": "Searching for new devices" } } ]

*/
func SearchForNewLights(ipAddress: String, username: String){
    //POST <IPAddress>/api/<username>/lights
    
    var address = "\(ipAddress)/api/\(username)/lights";
}






/**
Gets the attributes and state of a given light.

Response
Name            Type            Description

state           state           object	Details the state of the light, see the state table below for more details.
type            string          A fixed name describing the type of light e.g. “Extended color light”.
name            string 0, 32	A unique, editable name given to the light.
modelid         string 6, 6     The hardware model of the light.
uniqueid        string 6, 32	As of 1.4. Unique id of the device. The MAC address of the device with a unique endpoint id in the form: AA:BB:CC:DD:EE:FF:00:11-XX
swversion       string 8, 8     An identifier for the software version running on the light.
Pointsymbol     object          This parameter is reserved for future functionality.

The state object contains the following fields

Name        Type                    Description

on          bool                    On/Off state of the light. On=true, Off=false
bri         uint8                   Brightness of the light. This is a scale from the minimum brightness the light is capable of, 1, to the maximum capable brightness, 254.
hue         uint16                  Hue of the light. This is a wrapping value between 0 and 65535. Both 0 and 65535 are red, 25500 is green and 46920 is blue.
sat         uint8                   Saturation of the light. 255 is the most saturated (colored) and 0 is the least saturated (white).
xy          list 2..2 of float 4	The x and y coordinates of a color in CIE color space. The first entry is the x coordinate and the second entry is the y coordinate. Both x and y are between 0 and 1.
ct          uint16                  The Mired Color temperature of the light. 2012 connected lights are capable of 153 (6500K) to 500 (2000K).
alert       string                  The alert effect, which is a temporary change to the bulb’s state. This can take one of the following values:
“none” – The light is not performing an alert effect.
“select” – The light is performing one breathe cycle.
“lselect” – The light is performing breathe cycles for 30 seconds or until an "alert": "none" command is received.
Note that in 1.0 this contains the last alert sent to the light and not its current state.

effect      string                  The dynamic effect of the light, can either be “none” or “colorloop”.
If set to colorloop, the light will cycle through all hues using the current brightness and saturation settings.

colormode	string 2, 2             Indicates the color mode in which the light is working, this is the last command type it received.
Values are “hs” for Hue and Saturation, “xy” for XY and “ct” for Color Temperature. This parameter is only present when the light supports at least one of the values.
reachable	bool                    Indicates if a light can be reached by the bridge. Currently always returns true, functionality will be added in a future patch.
1.4.3. Response example
{
"state": {
"hue": 50000,
"on": true,
"effect": "none",
"alert": "none",
"bri": 200,
"sat": 200,
"ct": 500,
"xy": [0.5, 0.5],
"reachable": true,
"colormode": "hs"
},
"type": "Living Colors",
"name": "LC 1",
"modelid": "LC0015",
"swversion": "1.0.3",
"pointsymbol": {
"1": "none",
"2": "none",
"3": "none",
"4": "none",
"5": "none",
"6": "none",
"7": "none",
"8": "none"
}
}
Notes
Note the usage of the colormode parameter: There are 3 ways of setting the light color: xy, color temperature (ct) or hue and saturation (hs). A light may contain different settings for xy, ct and hs, but only the mode indicated by the colormode parameter will be certain to give the active light color.

Also note that some light state attributes are only present for specific light types. See supported lights for more information.
*/
func GetLightAttributesAndState(ipAddress: String, username: String, lightId: String){
    //POST <ipAddress>/api/<username>/lights/<lightId>
    
    var address = "\(ipAddress)/api/\(username)/lights/\(lightId)";
}



/**
Used to rename lights. A light can have its name changed when in any state, including when it is unreachable or off.


*/
func SetLightName(ipAddress: String, username: String, lightId: String, lightName :String){
    //PUT <ipAddress>/api/<username>/lights/<lightId>
    
    var address = "\(ipAddress)/api/\(username)/lights/\(lightId)";
    
}


/**
Allows the user to turn the light on and off, modify the hue and effects.


*/
func SetLightState(ipAddress: String, username: String, lightId: String, lightName :String){
    //PUT <ipAddress>/api/<username>/lights/<lightId>
    
    var address = "\(ipAddress)/api/\(username)/lights/\(lightId)";
    
}


























/*


DataManager.getTopAppsDataFromFileWithSuccess { (data) -> Void in
// Get the number 1 app using optional binding and NSJSONSerialization
//1
var parseError: NSError?
let parsedObject: AnyObject? = NSJSONSerialization.JSONObjectWithData(data,
options: NSJSONReadingOptions.AllowFragments,
error:&parseError)

//2
if let topApps = parsedObject as? NSDictionary {
if let feed = topApps["feed"] as? NSDictionary {
if let apps = feed["entry"] as? NSArray {
if let firstApp = apps[0] as? NSDictionary {
if let imname = firstApp["im:name"] as? NSDictionary {
if let appName = imname["label"] as? NSString {
//3
println("Optional Binding: \(appName)")
}
}
}
}
}
}
}


*/