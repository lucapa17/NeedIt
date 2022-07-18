package com.example.myapplication.models

class Group() {
    var groupId : Long = -1
    var nameGroup : String = ""
    var users : MutableList<String>? = mutableListOf()

    constructor(groupId : Long, nameGroup : String, users : MutableList<String>?) : this() {
        this.groupId = groupId
        this.users = users
        this.nameGroup = nameGroup
    }
}