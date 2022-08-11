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
import com.example.myapplication.models.Request
import com.example.myapplication.models.User
import com.example.myapplication.models.getUser
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        val id : String = FirebaseAuthWrapper(this@EditProfileActivity).getUid()!!
        var name : String = ""
        var surname : String = ""
        var email : String = ""
        var nickname : String = ""


        var user : User? =null
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                user= getUser(this@EditProfileActivity)
                withContext(Dispatchers.Main) {
                    name = user?.name.toString()
                    surname = user?.surname.toString()
                    email = user?.email.toString()
                    nickname = user?.nickname.toString()
                    findViewById<TextView>(R.id.edit_name).setText(name)
                    findViewById<TextView>(R.id.edit_surname).setText(surname)
                    findViewById<TextView>(R.id.edit_email).setText(email)
                    findViewById<TextView>(R.id.edit_nickname).setText(nickname)
                }
            }
        }

        val button : Button = findViewById(R.id.edit_button)
        button.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v : View?) {
                //funziona che mi restituisca tutte le richieste fatte e completate da un User
                GlobalScope.launch {
                    //user = User(name, surname, email, nickname,  )
                    val ref = Firebase.database.getReference("users").child(id)
                    ref.child("name").setValue(name)
                    ref.child("email").setValue(email)
                    ref.child("surname").setValue(surname)
                    ref.child("nickname").setValue(nickname)

                    val intent : Intent = Intent(this@EditProfileActivity, EditProfileActivity::class.java)
                    this@EditProfileActivity.startActivity(intent)

                    //val intent : Intent = Intent(this@EditProfileActivity, GroupActivity::class.java)
                    //this@EditProfileActivity.startActivity(intent)
                    //qui servono i valori da passare tramite intent
                }
            }
        })
    }
}