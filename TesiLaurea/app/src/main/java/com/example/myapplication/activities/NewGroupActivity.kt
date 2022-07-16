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
    private val TAG = NewGroupActivity::class.simpleName.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_group)
        Log.d(TAG,"BBB New groupActivity created " )

        val uid = FirebaseAuthWrapper(this).getUid()
        Log.d(TAG,"BBB uid : " + uid )

        val button : Button = findViewById(R.id.buttonCreateGroup)
        button.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v : View?) {
                Log.d(TAG,"BBB button clicked " )
                val groupName : EditText = findViewById(R.id.groupName)
                if(groupName.text.toString().isEmpty()) {
                    Toast.makeText(v!!.context, "Insert the group name!", Toast.LENGTH_SHORT).show()
                }
                else{
                    Log.d(TAG,"BBB groupName is not empty " )
                    GlobalScope.launch{
                        val group : Group = Group(groupName.text.toString(), mutableListOf(uid!!))
                        Log.d(TAG,"BBB groupList : " + group.users )
                        val groupId : Long = getGroupId(this@NewGroupActivity)
                        Log.d(TAG,"BBB Now we create the group : " + group.nameGroup + " " + groupId )
                        createGroup(group, groupId, this@NewGroupActivity)
                    }
                    Log.d(TAG,"BBB Now we go to the main activity" )
                    val intent : Intent = Intent(this@NewGroupActivity, MainActivity::class.java)
                    this@NewGroupActivity.startActivity(intent)
                }

            }

        })

    }
}