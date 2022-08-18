package com.example.myapplication.activities

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.GroupsAdapter
import com.example.myapplication.adapter.ListAdapter
import com.example.myapplication.models.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.core.view.View
import kotlinx.coroutines.*
import java.io.File

class MainActivity : BaseActivity() {
    private lateinit var recv: RecyclerView
    private lateinit var groupsAdapter: GroupsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        delete_cache()
        recv = this.findViewById(R.id.mRecycler)
        groupsAdapter = GroupsAdapter(this, ArrayList())
        recv.layoutManager = LinearLayoutManager(this)
        recv.adapter = groupsAdapter
        runInstantWorker(this)
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Fetching...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val layoutNoGroup = this.findViewById<LinearLayout>(R.id.noGroup)

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                var groupList : MutableList<Group> = getGroups(this@MainActivity)
                withContext(Dispatchers.Main) {
                    if(groupList.isEmpty()){
                        progressDialog.dismiss()
                        layoutNoGroup.setVisibility(android.view.View.VISIBLE)
                    }
                    else {
                        groupsAdapter = GroupsAdapter(this@MainActivity, ArrayList(groupList))
                        recv.layoutManager = LinearLayoutManager(this@MainActivity)
                        recv.adapter = groupsAdapter
                        progressDialog.dismiss()
                    }

                }
            }
        }
        val link = this.findViewById<TextView>(R.id.newGroup)
        link.setOnClickListener(object : android.view.View.OnClickListener{
            override fun onClick(v : android.view.View?) {
                val intent = Intent(v!!.context, NewGroupActivity::class.java)
                v.context.startActivity(intent)
            }
        })


    }
    override fun onBackPressed() {
        finishAffinity()
        //startActivity(Intent(this, MainActivity::class.java))
    }
    fun delete_cache(){
        val dir: File = File(this.getCacheDir().getAbsolutePath())
        for (f in dir.listFiles()) {
            val minutes : Long =  (java.util.Calendar.getInstance().timeInMillis - f.lastModified()) / (1000*60)
            if(minutes >= 5)
                f.delete()
        }
    }
}