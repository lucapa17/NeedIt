package com.example.myapplication.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.myapplication.R
import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.models.FirebaseAuthWrapper
import com.example.myapplication.models.FirebaseDbWrapper
import com.example.myapplication.models.User


class RegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        val button : Button = findViewById(R.id.buttonRegistration)
        button.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v : View?) {
                val name : EditText = findViewById(R.id.userName)
                val surname : EditText = findViewById(R.id.userSurname)
                val email : EditText = findViewById(R.id.userEmail)
                val password : EditText = findViewById(R.id.userPassword)
                val confirm : EditText = findViewById(R.id.userPassword2)

                if(email.text.toString().isEmpty() || password.text.toString().isEmpty() || name.text.toString().isEmpty() || surname.text.toString().isEmpty() || confirm.text.toString().isEmpty()) {
                    Toast.makeText(v!!.context, "Fill all the fields!", Toast.LENGTH_SHORT).show()
                }
                else{
                        if(password.text.toString() == confirm.text.toString()){
                            val firebaseWrapper : FirebaseAuthWrapper = FirebaseAuthWrapper(v!!.context)
                            firebaseWrapper.signUp(email.text.toString(), password.text.toString(), name.text.toString(), surname.text.toString())
                        }
                        else
                            Toast.makeText(v!!.context, "Passwords mismatched", Toast.LENGTH_SHORT).show()


                }
            }

        })

        val link : TextView = findViewById(R.id.switchToLogin)
        link.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v : View?) {
                val intent = Intent(v!!.context, LoginActivity::class.java)
                v.context.startActivity(intent)
            }
        })
    }

}