package com.example.myapplication.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import com.example.myapplication.R
import com.example.myapplication.models.FirebaseAuthWrapper

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonlogout : Button = findViewById(R.id.buttonLogOut)
        buttonlogout.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                val firebaseWrapper : FirebaseAuthWrapper = FirebaseAuthWrapper(v!!.context)
                firebaseWrapper.logOut()
            }

        })



        val buttonprofile : Button = findViewById(R.id.buttonProfile)
        buttonprofile.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                val intent = Intent(v!!.context, ProfileActivity::class.java)
                v.context.startActivity(intent)
            }

        })

        val buttongroup : Button = findViewById(R.id.buttonNewGroup)
        buttongroup.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                val intent = Intent(v!!.context, NewGroupActivity::class.java)
                v.context.startActivity(intent)
            }

        })

        val buttonshowgroups : Button = findViewById(R.id.buttonShowGroups)
        buttonshowgroups.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                val intent = Intent(v!!.context, ShowGroupsActivity::class.java)
                v.context.startActivity(intent)
            }

        })

        val buttonmain2 : Button = findViewById(R.id.buttonMain2)
        buttonmain2.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                val intent = Intent(v!!.context, MainActivity2::class.java)
                v.context.startActivity(intent)
            }

        })


    }

    override fun onBackPressed() {
        finishAffinity()
        //startActivity(Intent(this, MainActivity::class.java))
    }




}