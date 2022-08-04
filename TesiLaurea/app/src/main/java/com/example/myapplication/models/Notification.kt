package com.example.myapplication.models

class Notification() {
    var request : Request = Request()
    var userId : String = ""
    var notificationId : Long = -1
    lateinit var type : Type

    constructor(request : Request,  userId : String, notificationId : Long, type : Type) : this() {
        this.userId = userId
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