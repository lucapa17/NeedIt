package com.example.myapplication.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.R
import com.example.myapplication.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NewGroupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_group)
        val uid = FirebaseAuthWrapper(this).getUid()

        val button : Button = findViewById(R.id.buttonCreateGroup)
        button.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v : View?) {
                val groupName : EditText = findViewById(R.id.groupName)
                if(groupName.text.toString().isEmpty()) {
                    Toast.makeText(v!!.context, "Insert the group name!", Toast.LENGTH_SHORT).show()
                }
                else{
                    GlobalScope.launch{
                        val groupId : Long = getGroupId(this@NewGroupActivity)
                        val group : Group = Group(groupId, groupName.text.toString(), mutableListOf(uid!!))
                        createGroup(group, this@NewGroupActivity)
                    }
                    val intent : Intent = Intent(this@NewGroupActivity, MainActivity::class.java)
                    this@NewGroupActivity.startActivity(intent)
                }

            }

        })

    }
}