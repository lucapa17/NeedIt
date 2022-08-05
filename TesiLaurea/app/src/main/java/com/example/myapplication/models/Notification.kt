package com.example.myapplication.models

class Notification() {

    var request : Request? = Request()
    var sender : String = ""
    var completedBy : String? = ""
    var groupName : String = ""
    var userId : String = ""
    var notificationId : Long = -1
    lateinit var type : Type

    constructor(userId : String, request : Request?, sender : String, completedBy : String?, groupName : String, notificationId : Long, type : Type) : this() {
        this.userId = userId
        this.sender = sender
        this.completedBy = completedBy
        this.groupName = groupName
        this.request = request
        this.notificationId = notificationId
        this.type = type
    }
    enum class Type {
        NewRequest,
        CompletedRequest,
        NewGroup
    }

}