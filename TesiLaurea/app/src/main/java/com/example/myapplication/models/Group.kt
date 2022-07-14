package com.example.myapplication.models

class Group() {
    var nameGroup : String = ""
    var users : MutableList<Long>? = mutableListOf()

    constructor(nameGroup : String, users : MutableList<Long>?) : this() {
        this.users = users
        this.nameGroup = nameGroup
    }
}