package com.example.myapplication.models

class User() {
    //var id : Long = -1
    var name : String = ""
    var surname : String = ""
    var email : String = ""

    constructor(name : String, surname : String, email : String) : this() {
        //this.id = id
        this.email = email
        this.name = name
        this.surname = surname
    }
}