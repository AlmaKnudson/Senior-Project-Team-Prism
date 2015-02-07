//
//  Groups.swift
//  Prism Lights
//
//  Created by Cody Foltz on 1/27/15.
//  Copyright (c) 2015 Prism. All rights reserved.
//

import Foundation


func GetAllGroups(ipAddress: String, username: String, lightId: String){
    //GET <ipAddress>/api/<username>/groups
    
    var address = "\(ipAddress)/api/\(username)/groups";
}

func CreateGroup(ipAddress: String, username: String, lightId: String){
    //POST <ipAddress>/api/<username>/groups
    
    var address = "\(ipAddress)/api/\(username)/groups";
}

func GetGroupAttributes(ipAddress: String, username: String, groupId: String){
    //GET <ipAddress>/api/<username>/groups/<groupId>
    
    var address = "\(ipAddress)/api/\(username)/groups/\(groupId)";
}

/**
This is the description

:param: ipAddress address of bridge
:param: username  authenticated user
:param: groupId   id of the light group

*/
func SetGroupAttributes(ipAddress: String, username: String, groupId: String){
    //PUT <ipAddress>/api/<username>/groups/<groupId>
    
    var address = "\(ipAddress)/api/\(username)/groups/\(groupId)";
}


func SetGroupState(ipAddress: String, username: String, groupId: String){
    //PUT <ipAddress>/api/<username>/groups/<groupId>/action
    
    var address = "\(ipAddress)/api/\(username)/groups/\(groupId)/action";
}

func DeleteGroup(ipAddress: String, username: String, groupId: String){
    //DELETE <ipAddress>/api/<username>/groups/<groupId>
    
    var address = "\(ipAddress)/api/\(username)/groups/\(groupId)";
}


