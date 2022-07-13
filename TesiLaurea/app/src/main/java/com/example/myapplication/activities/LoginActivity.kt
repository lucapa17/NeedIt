package com.example.myapplication.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
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
        button.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v : View?) {
                val email : EditText = findViewById(R.id.userEmail)
                val password : EditText = findViewById(R.id.userPassword)

                if(email.text.toString().isEmpty() || password.text.toString().isEmpty()) {
                    Toast.makeText(v!!.context, "Fill all the fields!", Toast.LENGTH_SHORT).show()
                }
                else {
                    val firebaseWrapper : FirebaseAuthWrapper = FirebaseAuthWrapper(v!!.context)
                    firebaseWrapper.signIn(email.text.toString(), password.text.toString())
                }
            }
        })

        val link : TextView = findViewById(R.id.switchToRegistration)
        link.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v : View?) {
                val intent = Intent(v!!.context, RegistrationActivity::class.java)
                v.context.startActivity(intent)
            }
        })
    }



}

