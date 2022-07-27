package com.example.myapplication.models

import java.sql.Time
import java.util.*

class Request() {

    var Id : Long = -1
    var groupId : Long = -1
    var userId : String = ""
    var completedById : String = ""
    var nameRequest : String = ""
    var isCompleted : Boolean = false
    var comment : String? = null
    var date : Date? = null

    constructor(Id : Long, groupId : Long,  userId : String, nameRequest : String, isCompleted : Boolean, comment : String?, completedById : String, date : Date) : this() {
        this.groupId = groupId
        this.completedById = completedById
        this.Id = Id
        this.nameRequest = nameRequest
        this.userId = userId
        this.isCompleted = isCompleted
        this.comment = comment
        this.date = date

    }
}
