//
//  PrismSDK.swift
//  Prism Lights
//
//  Created by Cody Foltz on 2/2/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

class PrismSDK {
    var lights :[Light] = []
    
}

enum ColorMode{
    case xy
    case ct
    case hs
    case NoColor
}

class LightState{
    var id :Int
    var reachable :Bool
    var hue :Int = 0
    var on :Bool
    var brightness :Int = 255
    var saturation :Int = 0
    var ct :Int = 5000
    var x :Double = 0.5
    var y :Double = 0.5
    var colortype :ColorMode = ColorMode.NoColor
    
    
    init(id:Int, reachable:Bool, on :Bool) {
        self.id = id
        self.reachable = reachable
        self.on = on
    }
    
    func SetColor(x:Double, y:Double){
        self.x = x
        self.y = y
        colortype = ColorMode.xy
    }
    
    func SetColor(hue:Int, saturation:Int){
        self.hue = hue
        self.saturation = saturation
        colortype = ColorMode.hs
    }
    
    func SetColor(ct :Int){
        self.ct = ct
        colortype = ColorMode.ct
    }
}



class Light{
    
    var type :String
    var name :String
    var modelID :String
    var swversion :String
    var state :LightState
    
    init( type :String, name :String, modelID :String, swversion :String, state :LightState) {
        self.type = type
        self.name = name
        self.modelID = modelID
        self.swversion = swversion
        self.state = state
    }
}


/**
Creates a LightState of a single bulb from a JSON dictionary containing light state infromation

:param: JSONDict JSON Dictionary that contains the state information of a bulb from the bridge

:returns: The light state for a bulb
*/
func StateDictionaryToLightState(bulbId :Int, JSONDict :Dictionary<String,AnyObject>) -> LightState? {
    
    var lightState :LightState?
    
    if let on = JSONDict["on"] as? Bool{
        if let reachable = JSONDict["reachable"] as? Bool{
            
            if let alert = JSONDict["alert"] as? String{
                if let effect = JSONDict["effect"] as? String{
                    if let brightness = JSONDict["bri"] as? Int{
                        if let colormode = JSONDict["colormode"] as? String{
                            lightState = LightState(id: bulbId, reachable: reachable, on: on)
                            switch(colormode){
                            case "xy":
                                if let xy = JSONDict["xy"] as? [Double]{
                                    lightState?.SetColor(xy[0], y: xy[1])
                                }
                            case "ct":
                                if let ct = JSONDict["ct"] as? Int{
                                    lightState?.SetColor(ct)
                                }
                            case "hs":
                                if let hue = JSONDict["hue"] as? Int{
                                    if let sat = JSONDict["sat"] as? Int{
                                        lightState?.SetColor(hue, saturation: sat)
                                    }
                                }
                            default:
                                println("No color mode");
                                return nil
                            }
                            lightState?.brightness = brightness
                            
                            /*
                            "bri": 144,
                            "hue": 13088,
                            "sat": 212,
                            "xy": [0.5128,0.4147],
                            "ct": 467,
                            */
                            
                        }
                    }// End of color
                }// End of effect
            }// End of alert
        }// End of reachable
    }// End of on
    
    
    
    return lightState
}



















