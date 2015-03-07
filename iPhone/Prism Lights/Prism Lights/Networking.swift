//
//  Networking.swift
//  Prism Lights
//
//  Created by Cody Foltz on 1/31/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation

var session2 :NSURLSession?





func HttpGet(url: String, requestCompleted : (responseReceived: Bool, msg: String, data :NSData?) -> ()) {
    //Setup the request object
    var request = NSMutableURLRequest(URL: NSURL(string: url)!)
    request.HTTPMethod = "GET"
    request.addValue("application/json", forHTTPHeaderField: "Content-Type")
    request.addValue("application/json", forHTTPHeaderField: "Accept")
    //Check to see if we have a session object already.
    if(session2 == nil){
        var config = NSURLSessionConfiguration.defaultSessionConfiguration()
        config.timeoutIntervalForRequest = 2
        config.timeoutIntervalForResource = 5
        session2 = NSURLSession(configuration: config)
    }
    
    var session = session2!
    
    var task = session.dataTaskWithRequest(request){
        (data, response, error) -> Void in
        
        if(error != nil){
            println("Unable to connect to ip address.");
            requestCompleted(responseReceived: false, msg: "Error could not connect to ip address", data: nil)
        } else{
            requestCompleted(responseReceived: true, msg: "", data: data)
        }
        return
        
        //
        //        println("Response: \(response)")
        //        var strData = NSString(data: data, encoding: NSUTF8StringEncoding)
        //        println("Body: \(strData)")
        //        var err: NSError?
        //        var json = NSJSONSerialization.JSONObjectWithData(data, options: .MutableLeaves, error: &err) as? NSDictionary
        //
        //
        //        // Did the JSONObjectWithData constructor return an error? If so, log the error to the console
        //        if(err != nil) {
        //            println(err!.localizedDescription)
        //            let jsonStr = NSString(data: data, encoding: NSUTF8StringEncoding)
        //            requestCompleted(responseReceived: true, msg: "Error could not parse JSON: '\(jsonStr)'", data: nil)
        //        }else {
        //            // The JSONObjectWithData constructor didn't return an error. But, we should still
        //            // check and make sure that json has a value using optional binding.
        //            if let parseJSON = json {
        //                // Okay, the parsedJSON is here, let's get the value for 'success' out of it
        //                requestCompleted(responseReceived: true, msg: "", data: data)
        //                return
        //            }
        //            else {
        //                // Woa, okay the json object was nil, something went worng. Maybe the server isn't running?
        //                let jsonStr = NSString(data: data, encoding: NSUTF8StringEncoding)
        //                println("Error could not parse JSON: \(jsonStr)")
        //                requestCompleted(responseReceived: true, msg: "Parsed JSON but received nil", data: nil)
        //            }
        //        }
        
    }
    task.resume()
}



func HttpPost(params : NSData, url : String, postCompleted : (succeeded: Bool, msg: String, data :NSData?) -> ()) {
    
    //Setup the request object
    var request = NSMutableURLRequest(URL: NSURL(string: url)!)
    request.HTTPMethod = "POST"
    request.HTTPBody = params
    request.addValue("application/json", forHTTPHeaderField: "Content-Type")
    request.addValue("application/json", forHTTPHeaderField: "Accept")
    
    //get session singleton
    
    if(session2 == nil){
        var config = NSURLSessionConfiguration.defaultSessionConfiguration()
        config.timeoutIntervalForRequest = 2
        config.timeoutIntervalForResource = 5
        session2 = NSURLSession(configuration: config)
    }
    
    var session = session2!
    
    //Create the task that will do the network connection.
    var task = session.dataTaskWithRequest(request, completionHandler: {data, response, error -> Void in
        println("Response: \(response)")
        var strData = NSString(data: data, encoding: NSUTF8StringEncoding)
        println("Body: \(strData)")
        var err: NSError?
        var json = NSJSONSerialization.JSONObjectWithData(data, options: .MutableLeaves, error: &err) as? NSDictionary
        
        
        // Did the JSONObjectWithData constructor return an error? If so, log the error to the console
        if(error != nil) {
            if(DEBUG){
                println(err!.localizedDescription)
            }
            postCompleted(succeeded: false, msg: "Network timeout", data: nil)
        }
        else {
            postCompleted(succeeded: true, msg: "", data: data)
        }
        
    })
    task.resume()
}
