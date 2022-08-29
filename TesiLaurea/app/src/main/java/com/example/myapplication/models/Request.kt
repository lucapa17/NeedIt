package com.example.myapplication.models

import java.util.*
import kotlin.collections.ArrayList

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
    var list : ArrayList<String>? = null
    var expiration : Date? = null
    lateinit var type : Type

    constructor(id : Long, groupId : Long,  user : User, nameRequest : String, isCompleted : Boolean, comment : String?, completedBy : User?, date : Date, price : String?, type : Type, list : ArrayList<String>?, expiration : Date?) : this() {
        this.groupId = groupId
        this.completedBy = completedBy
        this.id = id
        this.nameRequest = nameRequest
        this.user = user
        this.isCompleted = isCompleted
        this.comment = comment
        this.date = date
        this.price = price
        this.type = type
        this.list = list
        this.expiration = expiration
    }
    enum class Type {
        ToDo,
        ToBuy
    }
}
