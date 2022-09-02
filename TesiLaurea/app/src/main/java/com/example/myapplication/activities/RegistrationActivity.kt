package com.example.myapplication.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.myapplication.R
import android.content.Intent
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.models.*
import kotlinx.coroutines.*
import java.util.regex.Pattern

class RegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        val button : Button = findViewById(R.id.buttonRegistration)
        button.setOnClickListener { v ->
            val name: EditText = findViewById(R.id.userName)
            val surname: EditText = findViewById(R.id.userSurname)
            val nickname: EditText = findViewById(R.id.userNickname)
            val email: EditText = findViewById(R.id.userEmail)
            val password: EditText = findViewById(R.id.userPassword)
            val confirm: EditText = findViewById(R.id.userPassword2)

            if (email.text.toString().trim().isEmpty() || password.text.toString().trim().isEmpty() ||
                name.text.toString().trim().isEmpty() || nickname.text.toString().trim().isEmpty() ||
                surname.text.toString().trim().isEmpty() || confirm.text.toString().trim().isEmpty()) {
                Toast.makeText(v!!.context, resources.getString(R.string.fillAllTheFields), Toast.LENGTH_SHORT).show()
            }
            else if (!Pattern.matches("^[a-zA-Z][a-zA-Z0-9_.-]{5,20}$", nickname.text.toString().trim())){
                nickname.error = resources.getString(R.string.nicknameNotValid)
            }
            else if (!Pattern.matches("[\\p{Alpha}\\p{Digit}\\p{Punct}]{8,20}", password.text.toString().trim())){
                password.error = resources.getString(R.string.passwordNotValid)
            }
            else {
                CoroutineScope(Dispatchers.Main + Job()).launch {
                    withContext(Dispatchers.IO) {
                        val nicknameAlreadyUsed: Boolean =
                            nicknameIsAlreadyUsed(v!!.context, nickname.text.toString().trim())
                        withContext(Dispatchers.Main) {
                            if (nicknameAlreadyUsed)
                                nickname.error = resources.getString(R.string.nicknameAlreadyUsed)
                            else {
                                if (password.text.toString().trim() == confirm.text.toString().trim()) {
                                    val firebaseWrapper = FirebaseAuthWrapper(v.context)
                                    firebaseWrapper.signUp(
                                        email.text.toString().trim(),
                                        password.text.toString().trim(),
                                        name.text.toString().trim(),
                                        surname.text.toString().trim(),
                                        nickname.text.toString().trim())
                                } else
                                    confirm.error = resources.getString(R.string.passwordMismatched)


                            }
                        }
                    }
                }
            }
        }
        val link : TextView = findViewById(R.id.switchToLogin)
        link.setOnClickListener { v ->
            val intent = Intent(v!!.context, LoginActivity::class.java)
            v.context.startActivity(intent)
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}