package com.example.myapplication.activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.models.FirebaseStorageWrapper
import com.example.myapplication.models.User
import com.example.myapplication.models.getUser
import com.example.myapplication.models.getUserById
import kotlinx.coroutines.*
import java.io.File

class ShowProfile : AppCompatActivity() {
    private var image: Uri? = null
    private var id : String? = null
    var user : User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

        val intent : Intent = intent
        id = intent.getStringExtra("id")

        var name: String
        var surname : String
        var email : String
        var nickname : String

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage(resources.getString(R.string.wait))
        progressDialog.setCancelable(false)
        progressDialog.show()

        val dir = File(this.cacheDir.absolutePath)
        var found = false
        if (dir.exists()) {
            for (f in dir.listFiles()) {
                if(f.name.toString().contains("image_${id}_")){
                    if(f.length() != 0L)
                        image = Uri.fromFile(f)
                    found = true
                    break
                }

            }
        }
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                user = getUserById(this@ShowProfile, id!!)
                if(!found)
                    image = FirebaseStorageWrapper().download(id!!, this@ShowProfile)
                withContext(Dispatchers.Main) {
                    name = user?.name.toString()
                    surname = user?.surname.toString()
                    email = user?.email.toString()
                    nickname = user?.nickname.toString()

                    findViewById<TextView>(R.id.name).text = name
                    findViewById<TextView>(R.id.surname).text = surname
                    findViewById<TextView>(R.id.email).text = email
                    findViewById<TextView>(R.id.nickname).text = nickname
                    if (image != null) {
                        findViewById<ImageView>(R.id.profile_image).setImageURI(image)
                    }
                    progressDialog.dismiss()
                }
            }
        }
    }
}