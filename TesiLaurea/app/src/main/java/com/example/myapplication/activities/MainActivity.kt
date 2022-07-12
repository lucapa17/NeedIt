package com.example.myapplication.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.example.myapplication.R
import com.example.myapplication.models.FireBaseWrapper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonlogout : Button = findViewById(R.id.buttonLogOut)
        buttonlogout.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                val firebaseWrapper : FireBaseWrapper = FireBaseWrapper(v!!.context)
                firebaseWrapper.logOut()
            }

        })
    }



}