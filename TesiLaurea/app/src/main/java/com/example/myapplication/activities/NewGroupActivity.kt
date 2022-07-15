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
import com.example.myapplication.models.FirebaseAuthWrapper
import com.example.myapplication.models.FirebaseDbWrapper
import com.example.myapplication.models.Group
import com.example.myapplication.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class NewGroupActivity : AppCompatActivity() {
    private val TAG = NewGroupActivity::class.simpleName.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_group)

        Log.d(TAG,"BBB New groupActivity created " )
         var user : User = User()
        FirebaseDbWrapper(this).readDbUser(object : FirebaseDbWrapper.Companion.FirebaseReadCallback{
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                user = snapshot.getValue(User::class.java)!!
                Log.d(TAG,"BBB : user: " + user.name)
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }

        })
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

                    val group : Group = Group(groupName.text.toString(), mutableListOf())
                    Log.d(TAG,"BBB groupName : " + group.nameGroup )

                    FirebaseDbWrapper(this@NewGroupActivity).createGroup(group, user)
                    val intent : Intent = Intent(this@NewGroupActivity, MainActivity::class.java)
                    this@NewGroupActivity.startActivity(intent)

                }

            }

        })

    }
}