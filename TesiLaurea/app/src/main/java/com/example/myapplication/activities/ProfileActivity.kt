package com.example.myapplication.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.models.FirebaseDbWrapper
import com.example.myapplication.models.User
import com.example.myapplication.models.getUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.coroutines.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        var user : User? =null
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                user= getUser(this@ProfileActivity)
                withContext(Dispatchers.Main) {
                    val name : String = user?.name.toString()
                    val surname : String = user?.surname.toString()
                    val email : String = user?.email.toString()
                    val nickname : String = user?.nickname.toString()
                    findViewById<TextView>(R.id.profile_name).setText(name)
                    findViewById<TextView>(R.id.profile_surname).setText(surname)
                    findViewById<TextView>(R.id.profile_email).setText(email)
                    findViewById<TextView>(R.id.profile_nickname).setText(nickname)
                }
            }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}