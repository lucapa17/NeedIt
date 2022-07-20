package com.example.myapplication.models

class Request() {

    var Id : Long = -1
    var groupId : Long = -1
    var userId : String = ""
    var nameRequest : String = ""
    var isCompleted : Boolean = false
    var comment : String? = null

    constructor(Id : Long, groupId : Long,  userId : String, nameRequest : String, isCompleted : Boolean, comment : String?) : this() {
        this.groupId = groupId
        this.Id = Id
        this.nameRequest = nameRequest
        this.userId = userId
        this.isCompleted = isCompleted
        this.comment = comment
    }
}
