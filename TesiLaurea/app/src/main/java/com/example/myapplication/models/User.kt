package com.example.myapplication.models

class User() {
    //var id : Long = -1
    var name : String = ""
    var surname : String = ""
    var email : String = ""
    var groups : MutableList<Long>? = mutableListOf()

    constructor(name : String, surname : String, email : String, groups : MutableList<Long>?) : this() {
        //this.id = id
        this.groups = groups
        this.email = email
        this.name = name
        this.surname = surname
    }
}