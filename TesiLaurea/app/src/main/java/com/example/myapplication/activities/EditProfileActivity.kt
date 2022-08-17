package com.example.myapplication.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.models.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.io.File
import java.util.*


class EditProfileActivity : AppCompatActivity() {
    private var profileImage: ImageView? = null
    var image: Uri? = null
    val id : String = FirebaseAuthWrapper(this@EditProfileActivity).getUid()!!
    //var new_image : Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        var name : String = ""
        var surname : String = ""
        var email : String = ""
        var nickname : String = ""
        var user : User? =null

        val progressDialog = ProgressDialog(this@EditProfileActivity)
        progressDialog.setMessage("Fetching...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val dir: File = File(this@EditProfileActivity.getCacheDir().getAbsolutePath())
        var found = false
        if (dir.exists()) {
            for (f in dir.listFiles()) {
                if(f.name.toString().contains("image_${id}_")){
                    if(!(f.length() == 0L))
                        image = Uri.fromFile(f)
                    found = true
                    break
                }

            }
        }
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                user = getUser(this@EditProfileActivity)
                if(!found)
                    image = FirebaseStorageWrapper().download(id, this@EditProfileActivity)
                Log.d(TAG, "aaa"+image)
                withContext(Dispatchers.Main) {
                    name = user?.name.toString()
                    surname = user?.surname.toString()
                    email = user?.email.toString()
                    nickname = user?.nickname.toString()

                    findViewById<TextView>(R.id.name).setText(name)
                    findViewById<TextView>(R.id.surname).setText(surname)
                    findViewById<TextView>(R.id.email).setText(email)
                    findViewById<TextView>(R.id.nickname).setText(nickname)
                    findViewById<EditText>(R.id.edit_nickname).setText(nickname)
                    findViewById<EditText>(R.id.edit_name).setText(name)
                    findViewById<EditText>(R.id.edit_surname).setText(surname)
                    if (image != null) {
                        findViewById<ImageView>(R.id.profile_image).setImageURI(image)
                    }
                    Log.d(TAG, "bello")
                    progressDialog.dismiss()

                }
            }
        }





        val modify_name : ImageView = findViewById(R.id.modify_name)
        modify_name.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v : View?) {
                findViewById<TextView>(R.id.name).setVisibility(View.GONE)
                findViewById<EditText>(R.id.edit_name).setVisibility(View.VISIBLE)
                findViewById<Button>(R.id.edit_button).setVisibility(View.VISIBLE)


            }
        })
        val modify_surname : ImageView = findViewById(R.id.modify_surname)
        modify_surname.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v : View?) {
                findViewById<TextView>(R.id.surname).setVisibility(View.GONE)
                findViewById<EditText>(R.id.edit_surname).setVisibility(View.VISIBLE)
                findViewById<Button>(R.id.edit_button).setVisibility(View.VISIBLE)


            }
        })
        val modify_nickname : ImageView = findViewById(R.id.modify_nickname)
        modify_nickname.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v : View?) {
                findViewById<TextView>(R.id.nickname).setVisibility(View.GONE)
                findViewById<EditText>(R.id.edit_nickname).setVisibility(View.VISIBLE)
                findViewById<Button>(R.id.edit_button).setVisibility(View.VISIBLE)


            }
        })

        val edit_photo : ImageView = findViewById(R.id.edit_photo)
        edit_photo.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v : View?) {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent, 100)



            }
        })

        val button : Button = findViewById(R.id.edit_button)
        button.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v : View?) {
                val t_name : String = findViewById<EditText>(R.id.edit_name).text.toString().trim()
                val t_surname : String = findViewById<EditText>(R.id.edit_surname).text.toString().trim()
                val t_nickname : String = findViewById<EditText>(R.id.edit_nickname).text.toString().trim()

                if(t_name.isEmpty() || t_surname.isEmpty() || t_nickname.isEmpty()) {
                    Toast.makeText(v!!.context, "Fill all the fields!", Toast.LENGTH_SHORT).show()
                }

                else {

                    CoroutineScope(Dispatchers.Main + Job()).launch {
                        withContext(Dispatchers.IO) {
                            val nicknameAlreadyUsed : Boolean = nicknameIsAlreadyUsed(v!!.context, t_nickname)
                            user= getUser(this@EditProfileActivity)
                            withContext(Dispatchers.Main) {
                                if(t_nickname != findViewById<TextView>(R.id.nickname).text && nicknameAlreadyUsed)
                                    Toast.makeText(v.context, "Nickname is already used", Toast.LENGTH_SHORT).show()
                                else{
                                    /*
                                    if(image != null) {
                                        FirebaseStorageWrapper().upload(image!!, id, this@EditProfileActivity)
                                    }

                                     */

                                    user!!.name = t_name
                                    user!!.surname = t_surname
                                    user!!.nickname = t_nickname
                                    Firebase.database.getReference("users").child(id).setValue(user)
                                    val intent : Intent = Intent(this@EditProfileActivity, EditProfileActivity::class.java)
                                    this@EditProfileActivity.startActivity(intent)
                                }
                            }
                        }
                    }


                }

            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 100 && resultCode == RESULT_OK){
            image = data?.data!!
            findViewById<ImageView>(R.id.profile_image).setImageURI(image)
            GlobalScope.launch{
                //FirebaseStorageWrapper().delete(id)
                val dir: File = File(this@EditProfileActivity.getCacheDir().getAbsolutePath())
                var found = false
                if (dir.exists()) {
                    for (f in dir.listFiles()) {
                        if(f.name.toString().contains("image_${id}_")){
                            f.delete()
                        }

                    }
                }
                FirebaseStorageWrapper().upload(image!!, id, this@EditProfileActivity)
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(com.example.myapplication.R.menu.nav_menu_profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            com.example.myapplication.R.id.nav_delete -> {


                val builder = AlertDialog.Builder(this)


                //builder.setView(v)
                builder.setTitle("Delete Profile")
                builder.setIcon(R.drawable.ic_baseline_cancel_24)
                builder.setMessage("Do you want to delete your profile? To do that, you have to re-authenticate")
                builder.setPositiveButton("Yes"){
                        dialog,_->

                    val inflter = LayoutInflater.from(this)
                    val v = inflter.inflate(R.layout.delete_login,null)
                    /**set view*/
                    val email = v.findViewById<EditText>(R.id.email)
                    val password = v.findViewById<EditText>(R.id.password)

                    val addDialog = AlertDialog.Builder(this)

                    addDialog.setView(v)
                    addDialog.setPositiveButton("Ok"){
                            dialog,_->
                        val email = email.text.toString().trim()
                        val password = password.text.toString().trim()

                        if(email.isEmpty() || password.isEmpty()) {
                            Toast.makeText(v!!.context, "Fill all the fields!", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            Firebase.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    GlobalScope.launch {
                                        FirebaseAuthWrapper(this@EditProfileActivity).delete()
                                    }
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        dialog.dismiss()

                    }
                    addDialog.setNegativeButton("Cancel"){
                            dialog,_->
                        dialog.dismiss()
                        Toast.makeText(this,"Cancel",Toast.LENGTH_SHORT).show()

                    }
                    addDialog.create()
                    addDialog.show()
                    /*


                     */
                }

                builder.setNegativeButton("No"){
                        dialog,_->
                    dialog.dismiss()
                }
                builder.create()
                builder.show()
                true

            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}