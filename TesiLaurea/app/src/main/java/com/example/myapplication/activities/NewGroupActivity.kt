package com.example.myapplication.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.R
import com.example.myapplication.models.FirebaseAuthWrapper
import com.example.myapplication.models.FirebaseDbWrapper
import com.example.myapplication.models.Group
import com.example.myapplication.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class NewGroupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_group)

        val button : Button = findViewById(R.id.buttonCreateGroup)
        button.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v : View?) {
                val groupName : EditText = findViewById(R.id.groupName)

                if(groupName.text.toString().isEmpty()) {
                    Toast.makeText(v!!.context, "Insert the group name!", Toast.LENGTH_SHORT).show()
                }
                else{

                    FirebaseDbWrapper(v!!.context).readDbUser(object : FirebaseDbWrapper.Companion.FirebaseReadCallback{
                        override fun onDataChangeCallback(snapshot: DataSnapshot) {
                            val user : User? = snapshot.getValue(User ::class.java)
                            val group : Group = Group(groupName.text.toString(), mutableListOf<Long>())
                            FirebaseDbWrapper(v!!.context).createGroup(group, user!!)

                        }

                        override fun onCancelledCallback(error: DatabaseError) {
                        }

                    })


                }
            }

        })
    }
}