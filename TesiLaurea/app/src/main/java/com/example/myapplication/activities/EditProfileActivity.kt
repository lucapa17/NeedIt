package com.example.myapplication.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.models.*
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import java.io.File
import java.util.regex.Pattern

class EditProfileActivity : AppCompatActivity() {
    private var image: Uri? = null
    private val id : String = FirebaseAuthWrapper(this@EditProfileActivity).getUid()!!
    private var user : User? = null
    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        var name: String
        var surname : String
        var email : String
        var nickname : String

        val progressDialog = ProgressDialog(this@EditProfileActivity)
        progressDialog.setMessage(resources.getString(R.string.wait))
        progressDialog.setCancelable(false)
        progressDialog.show()

        val dir = File(this@EditProfileActivity.cacheDir.absolutePath)
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
                user = getUser(this@EditProfileActivity)
                if(!found)
                    image = FirebaseStorageWrapper().download(id, this@EditProfileActivity)
                withContext(Dispatchers.Main) {
                    name = user?.name.toString()
                    surname = user?.surname.toString()
                    email = user?.email.toString()
                    nickname = user?.nickname.toString()

                    findViewById<TextView>(R.id.name).text = name
                    findViewById<TextView>(R.id.surname).text = surname
                    findViewById<TextView>(R.id.email).text = email
                    findViewById<TextView>(R.id.nickname).text = nickname
                    findViewById<EditText>(R.id.edit_nickname).setText(nickname)
                    findViewById<EditText>(R.id.edit_name).setText(name)
                    findViewById<EditText>(R.id.edit_surname).setText(surname)
                    if (image != null) {
                        findViewById<ImageView>(R.id.profile_image).setImageURI(image)
                    }
                    progressDialog.dismiss()
                }
            }
        }

        val modifyName : ImageView = findViewById(R.id.modify_name)
        modifyName.setOnClickListener {
            findViewById<TextView>(R.id.name).visibility = View.GONE
            findViewById<EditText>(R.id.edit_name).visibility = View.VISIBLE
            findViewById<Button>(R.id.edit_button).visibility = View.VISIBLE
        }
        val modifySurname : ImageView = findViewById(R.id.modify_surname)
        modifySurname.setOnClickListener {
            findViewById<TextView>(R.id.surname).visibility = View.GONE
            findViewById<EditText>(R.id.edit_surname).visibility = View.VISIBLE
            findViewById<Button>(R.id.edit_button).visibility = View.VISIBLE
        }
        val modifyNickname : ImageView = findViewById(R.id.modify_nickname)
        modifyNickname.setOnClickListener {
            findViewById<TextView>(R.id.nickname).visibility = View.GONE
            findViewById<EditText>(R.id.edit_nickname).visibility = View.VISIBLE
            findViewById<Button>(R.id.edit_button).visibility = View.VISIBLE
        }

        val editPhoto : ImageView = findViewById(R.id.edit_photo)
        editPhoto.setOnClickListener {
            if(image != null) {
                popupMenus(editPhoto)
            }
            else {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent, 100)
            }
        }

        val button : Button = findViewById(R.id.edit_button)
        button.setOnClickListener { v ->
            val tName: String = findViewById<EditText>(R.id.edit_name).text.toString().trim()
            val tSurname: String = findViewById<EditText>(R.id.edit_surname).text.toString().trim()
            val tNickname: String = findViewById<EditText>(R.id.edit_nickname).text.toString().trim()

            if (tName.isEmpty() || tSurname.isEmpty() || tNickname.isEmpty()) {
                Toast.makeText(v!!.context, resources.getString(R.string.fillAllTheFields), Toast.LENGTH_SHORT).show()
            }
            else if (!Pattern.matches("^[a-zA-Z][a-zA-Z0-9_.-]{5,20}$", tNickname)){
                findViewById<EditText>(R.id.edit_nickname).error = resources.getString(R.string.nicknameNotValid)
            }
            else {
                val progressDialog1 = ProgressDialog(this)
                progressDialog1.setMessage(resources.getString(R.string.wait))
                progressDialog1.setCancelable(false)
                progressDialog1.show()
                CoroutineScope(Dispatchers.Main + Job()).launch {
                    withContext(Dispatchers.IO) {
                        val nicknameAlreadyUsed: Boolean = nicknameIsAlreadyUsed(v!!.context, tNickname)
                        user = getUser(this@EditProfileActivity)
                        withContext(Dispatchers.Main) {
                            if (tNickname != findViewById<TextView>(R.id.nickname).text && nicknameAlreadyUsed){
                                progressDialog1.dismiss()
                                findViewById<EditText>(R.id.edit_nickname).error = resources.getString(R.string.nicknameAlreadyUsed)
                            }
                            else {
                                user!!.name = tName
                                user!!.surname = tSurname
                                user!!.nickname = tNickname
                                Firebase.database.getReference("users").child(id).setValue(user)
                                findViewById<TextView>(R.id.name).text = tName
                                findViewById<TextView>(R.id.name).visibility = View.VISIBLE
                                findViewById<EditText>(R.id.edit_name).visibility = View.GONE
                                findViewById<TextView>(R.id.surname).text = tSurname
                                findViewById<TextView>(R.id.surname).visibility = View.VISIBLE
                                findViewById<EditText>(R.id.edit_surname).visibility = View.GONE
                                findViewById<TextView>(R.id.nickname).text = tNickname
                                findViewById<TextView>(R.id.nickname).visibility = View.VISIBLE
                                findViewById<EditText>(R.id.edit_nickname).visibility = View.GONE
                                findViewById<Button>(R.id.edit_button).visibility = View.GONE
                                progressDialog1.dismiss()

                            }
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100 && resultCode == RESULT_OK){
            image = data?.data!!
            findViewById<ImageView>(R.id.profile_image).setImageURI(image)
            GlobalScope.launch{
                val dir = File(this@EditProfileActivity.cacheDir.absolutePath)
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
        inflater.inflate(R.menu.nav_menu_profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_delete -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(resources.getString(R.string.deleteProfileTitle))
                builder.setIcon(R.drawable.ic_baseline_cancel)
                builder.setMessage(resources.getString(R.string.deleteProfileMessage))
                builder.setPositiveButton(resources.getString(R.string.yes)){
                        dialog,_->
                    val inflter = LayoutInflater.from(this)
                    val v = inflter.inflate(R.layout.delete_login,null)
                    val password = v.findViewById<EditText>(R.id.password)
                    val addDialog = AlertDialog.Builder(this)

                    addDialog.setView(v)
                    addDialog.setPositiveButton("Ok"){
                            dialog1,_->
                        val progressDialog = ProgressDialog(this@EditProfileActivity)
                        progressDialog.setMessage(resources.getString(R.string.wait))
                        progressDialog.setCancelable(false)
                        progressDialog.show()
                        val password2 = password.text.toString().trim()
                        if(password2.isEmpty()) {
                            progressDialog.dismiss()
                            Toast.makeText(v!!.context, resources.getString(R.string.fillAllTheFields), Toast.LENGTH_SHORT).show()
                        }
                        else {
                            Firebase.auth.signInWithEmailAndPassword(user!!.email, password2).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    GlobalScope.launch {
                                        FirebaseAuthWrapper(this@EditProfileActivity).delete()
                                        progressDialog.dismiss()
                                    }
                                } else {
                                    val exception = task.exception as FirebaseException
                                    if(exception is FirebaseAuthInvalidCredentialsException)
                                        Toast.makeText(this, resources.getString(R.string.wrongPassword), Toast.LENGTH_SHORT).show()
                                    else if(exception is FirebaseNetworkException)
                                        Toast.makeText(this, resources.getString(R.string.networkError), Toast.LENGTH_SHORT).show()
                                    else
                                        Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
                                    progressDialog.dismiss()

                                }
                            }
                            dialog1.dismiss()
                        }
                    }
                    addDialog.setNegativeButton(resources.getString(R.string.cancel)){
                            dialog,_->
                        dialog.dismiss()
                    }
                    addDialog.create()
                    addDialog.show()
                }
                builder.setNegativeButton("No"){
                        dialog,_->
                    dialog.dismiss()
                }
                builder.create()
                builder.show()
                true
            }
            R.id.nav_change_password -> {
                val inflater = LayoutInflater.from(this@EditProfileActivity)
                val view = inflater.inflate(R.layout.change_password, null)
                val oldPassword = view.findViewById<EditText>(R.id.old_password)
                val newPassword = view.findViewById<EditText>(R.id.new_password)
                val confirmPassword = view.findViewById<EditText>(R.id.confirm_password)
                val addDialog = AlertDialog.Builder(this@EditProfileActivity)
                addDialog.setView(view)
                addDialog.setPositiveButton("Ok") {
                        dialog, _ ->

                    val oldPassword1 = oldPassword.text.toString().trim()
                    val newPassword1 = newPassword.text.toString().trim()
                    val confirmPassword1 = confirmPassword.text.toString().trim()

                    if (oldPassword1.isEmpty() || newPassword1.isEmpty() || confirmPassword1.isEmpty()) {
                        Toast.makeText(view!!.context, resources.getString(R.string.fillAllTheFields), Toast.LENGTH_SHORT).show()
                    }
                    else if (!Pattern.matches("[\\p{Alpha}\\p{Digit}\\p{Punct}]{8,20}", newPassword1)){
                        Toast.makeText(view!!.context, resources.getString(R.string.passwordNotValid), Toast.LENGTH_SHORT).show()
                    }
                    else if (newPassword1 != confirmPassword1) {
                        Toast.makeText(view!!.context, resources.getString(R.string.passwordMismatched), Toast.LENGTH_SHORT).show()
                    }
                    else {
                        val progressDialog1 = ProgressDialog(this@EditProfileActivity)
                        progressDialog1.setMessage(resources.getString(R.string.wait))
                        progressDialog1.setCancelable(false)
                        progressDialog1.show()
                        Firebase.auth.signInWithEmailAndPassword(user!!.email, oldPassword1).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                if (newPassword1 == oldPassword1) {
                                    progressDialog1.dismiss()
                                    Toast.makeText(this@EditProfileActivity, resources.getString(R.string.passwordEqualOld), Toast.LENGTH_SHORT).show()
                                }
                                else {
                                    Firebase.auth.currentUser!!.updatePassword(newPassword1).addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            progressDialog1.dismiss()
                                            Toast.makeText(view!!.context, resources.getString(R.string.passwordOk), Toast.LENGTH_SHORT).show()
                                        }
                                        else {
                                            progressDialog1.dismiss()
                                            Toast.makeText(this@EditProfileActivity, it.exception!!.message, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                            else {
                                progressDialog1.dismiss()
                                val exception = task.exception as FirebaseException
                                if(exception is FirebaseAuthInvalidCredentialsException)
                                    Toast.makeText(this, resources.getString(R.string.wrongPassword), Toast.LENGTH_SHORT).show()
                                else if(exception is FirebaseNetworkException)
                                    Toast.makeText(this, resources.getString(R.string.networkError), Toast.LENGTH_SHORT).show()
                                else
                                    Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                        dialog.dismiss()
                    }
                }
                addDialog.setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                addDialog.create()
                addDialog.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
    }
    fun popupMenus(v:View) {
        val popupMenus = PopupMenu(this,v)
        popupMenus.inflate(R.menu.options_image)
        popupMenus.setOnMenuItemClickListener {

            when(it.itemId){
                R.id.changeImage->{
                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(intent, 100)
                    true
                }
                R.id.deleteImage->{
                    findViewById<ImageView>(R.id.profile_image).setImageResource(R.drawable.ic_baseline_person_grey)
                    //FirebaseStorageWrapper().delete(id, this)
                    FirebaseStorage.getInstance().reference.child("images/${id}.jpg").delete()
                    val dir = File(this.cacheDir.absolutePath)
                    if (dir.exists()) {
                        for (f in dir.listFiles()) {
                            if(f.name.toString().contains("image_${id}_")){
                                f.delete()
                            }
                        }
                    }
                    val tmp = File.createTempFile("image_${id}_", null, this.cacheDir)
                    tmp.deleteOnExit()
                    image = null

                    true
                }
                else-> true
            }
        }
        popupMenus.show()
        val popup = PopupMenu::class.java.getDeclaredField("mPopup")
        popup.isAccessible = true
        val menu = popup.get(popupMenus)
        menu.javaClass.getDeclaredMethod("setForceShowIcon",Boolean::class.java)
            .invoke(menu,true)
    }
}