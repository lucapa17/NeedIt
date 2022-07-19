package com.example.myapplication.activities

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.myapplication.R
import com.example.myapplication.models.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class AddMemberActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_member)
        val intent : Intent = getIntent()
        val groupId : Long = intent.getLongExtra("groupId", 0L)
        var myNickname: String? = null
        GlobalScope.launch {
            myNickname = getUser(this@AddMemberActivity).nickname
        }
        val nicknameEditText: EditText = findViewById(R.id.memberNickname)
        var userExists = false
        nicknameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                userExists = false
                CoroutineScope(Dispatchers.Main + Job()).launch {
                    withContext(Dispatchers.IO) {
                        userExists = nicknameIsAlreadyUsed(this@AddMemberActivity, nicknameEditText.text.toString().trim())
                        withContext(Dispatchers.Main) {
                            if (!userExists) {
                                nicknameEditText.error = "user with this nickname does not exist"
                            }
                            else if(nicknameEditText.text.toString().trim()==myNickname)
                                nicknameEditText.error = "this is your nickname"
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        val button : Button = findViewById(R.id.buttonAddNewMember)
        button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if(!userExists)
                    Toast.makeText(v!!.context, "user with this nickname does not existtttt", Toast.LENGTH_SHORT).show()
                else if(nicknameEditText.text.toString().trim()==myNickname)
                    nicknameEditText.error = "this is your nickname"
                else {
                    CoroutineScope(Dispatchers.Main + Job()).launch {
                        withContext(Dispatchers.IO) {
                            val group : Group = getGroupById(this@AddMemberActivity, groupId)
                            val id : String? = getUserIdByNickname(this@AddMemberActivity, nicknameEditText.text.toString().trim())
                            val user : User = getUserByNickname(this@AddMemberActivity, nicknameEditText.text.toString().trim())
                            withContext(Dispatchers.Main) {
                                if(id == null)
                                    Toast.makeText(v!!.context, "user with this nickname does not exist", Toast.LENGTH_SHORT).show()
                                else {
                                    Log.d(TAG, "AAA id : "+id)
                                    group.users!!.add(id)
                                    user.groups!!.add(groupId)
                                    //Log.d(TAG, "AAA user: "+Firebase.database.getReference("users").child(id).key)
                                    Firebase.database.getReference("users").child(id).setValue(user)
                                    Firebase.database.getReference("groups").child(groupId.toString()).setValue(group)
                                    val intent = Intent(this@AddMemberActivity, GroupActivity::class.java)
                                    this@AddMemberActivity.startActivity(intent)
                                }
                            }
                        }
                    }
                }
            }
        })
    }
}
