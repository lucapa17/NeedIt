package com.example.myapplication.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.models.FirebaseDbWrapper
import com.example.myapplication.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        FirebaseDbWrapper(this).readDbUser(object : FirebaseDbWrapper.Companion.FirebaseReadCallback{
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val user : User? = snapshot.getValue(User ::class.java)
                val name : String = user?.name.toString()
                val surname : String = user?.surname.toString()
                val email : String = user?.email.toString()

                findViewById<TextView>(R.id.profile_name).setText(name)
                findViewById<TextView>(R.id.profile_surname).setText(surname)
                findViewById<TextView>(R.id.profile_email).setText(email)

            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })

    }
}