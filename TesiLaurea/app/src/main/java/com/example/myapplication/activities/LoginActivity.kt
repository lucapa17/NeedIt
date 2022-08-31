package com.example.myapplication.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.R
import com.example.myapplication.models.FirebaseAuthWrapper

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val button : Button = findViewById(R.id.buttonLogin)
        button.setOnClickListener { v ->
            val email: EditText = findViewById(R.id.userEmail)
            val password: EditText = findViewById(R.id.userPassword)
            if (email.text.toString().trim().isEmpty() || password.text.toString().trim()
                    .isEmpty()
            ) {
                Toast.makeText(v!!.context, R.string.fillAllTheFields.toString(), Toast.LENGTH_SHORT).show()
            } else {
                val firebaseWrapper = FirebaseAuthWrapper(v!!.context)
                firebaseWrapper.signIn(
                    email.text.toString().trim(),
                    password.text.toString().trim()
                )
            }
        }
        val link : TextView = findViewById(R.id.switchToRegistration)
        link.setOnClickListener { v ->
            val intent = Intent(v!!.context, RegistrationActivity::class.java)
            v.context.startActivity(intent)
        }
    }
    override fun onBackPressed() {
        finishAffinity()
    }
}

