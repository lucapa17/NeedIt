package com.example.myapplication.models

import java.util.*

class Group() {
    var groupId : Long = -1
    var nameGroup : String = ""
    var users : MutableList<String>? = mutableListOf()
    var lastNotification : Date? = null

    constructor(groupId : Long, nameGroup : String, users : MutableList<String>?, lastNotification : Date) : this() {
        this.groupId = groupId
        this.users = users
        this.nameGroup = nameGroup
        this.lastNotification = lastNotification
    }
}