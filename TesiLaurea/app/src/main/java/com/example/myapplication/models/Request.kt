package com.example.myapplication.models

import java.util.*

class Request() {

    var id : Long = -1
    var groupId : Long = -1
    var user : User = User()
    var completedBy : User? = null
    var nameRequest : String = ""
    var isCompleted : Boolean = false
    var comment : String? = null
    var date : Date? = null
    var price : String? = ""

    constructor(id : Long, groupId : Long,  user : User, nameRequest : String, isCompleted : Boolean, comment : String?, completedBy : User?, date : Date, price : String?) : this() {
        this.groupId = groupId
        this.completedBy = completedBy
        this.id = id
        this.nameRequest = nameRequest
        this.user = user
        this.isCompleted = isCompleted
        this.comment = comment
        this.date = date
        this.price = price

    }
}
