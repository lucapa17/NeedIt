package com.example.myapplication.models

class Group() {
    var nameGroup : String = ""
    var users : MutableList<String>? = mutableListOf()

    constructor(nameGroup : String, users : MutableList<String>?) : this() {
        this.users = users
        this.nameGroup = nameGroup
    }
}