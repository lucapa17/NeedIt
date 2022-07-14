package com.example.myapplication.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.example.myapplication.R
import com.example.myapplication.models.FirebaseAuthWrapper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonlogout : Button = findViewById(R.id.buttonLogOut)
        buttonlogout.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                val firebaseWrapper : FirebaseAuthWrapper = FirebaseAuthWrapper(v!!.context)
                firebaseWrapper.logOut()
            }

        })



        val buttonprofile : Button = findViewById(R.id.buttonProfile)
        buttonprofile.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                val intent = Intent(v!!.context, ProfileActivity::class.java)
                v.context.startActivity(intent)
            }

        })

        val buttongroup : Button = findViewById(R.id.buttonNewGroup)
        buttongroup.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                val intent = Intent(v!!.context, NewGroupActivity::class.java)
                v.context.startActivity(intent)
            }

        })

    }



}