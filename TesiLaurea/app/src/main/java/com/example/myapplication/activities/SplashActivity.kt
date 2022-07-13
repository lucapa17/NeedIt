package com.example.myapplication.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication.R
import com.example.myapplication.models.FirebaseAuthWrapper

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // TODO: check if user logged or not
        val firebaseWrapper : FirebaseAuthWrapper = FirebaseAuthWrapper(this)
        if (!firebaseWrapper.isAuthenticated()) {
            //redirect to login/register activity
            val intent = Intent(this, LoginActivity::class.java)
            this.startActivity(intent)
        }
        else{
            //Start Main Activity
            val intent = Intent(this, MainActivity::class.java)
            this.startActivity(intent)
        }
        finish()
    }
}