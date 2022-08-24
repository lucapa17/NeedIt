package com.example.myapplication.activities

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.GroupsAdapter
import com.example.myapplication.models.*
import kotlinx.coroutines.*
import java.io.File

class MainActivity : BaseActivity() {
    private lateinit var recv: RecyclerView
    private lateinit var groupsAdapter: GroupsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        deleteCache()
        val dir = File(this.cacheDir.absolutePath)
        if (dir.exists()) {
            for (f in dir.listFiles()) {
                Log.d(TAG, "fff "+f.name.toString())
            }
        }
        recv = this.findViewById(R.id.mRecycler)
        groupsAdapter = GroupsAdapter(this, ArrayList())
        recv.layoutManager = LinearLayoutManager(this)
        recv.adapter = groupsAdapter
        runInstantWorker(this)
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val layoutNoGroup = this.findViewById<LinearLayout>(R.id.noGroup)
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val groupList : MutableList<Group> = getGroups(this@MainActivity)
                withContext(Dispatchers.Main) {
                    if(groupList.isEmpty()){
                        progressDialog.dismiss()
                        layoutNoGroup.visibility = android.view.View.VISIBLE
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
        link.setOnClickListener { v ->
            val intent = Intent(v!!.context, NewGroupActivity::class.java)
            v.context.startActivity(intent)
        }
    }
    override fun onBackPressed() {
        finishAffinity()
    }
    private fun deleteCache(){
        val dir = File(this.cacheDir.absolutePath)
        for (f in dir.listFiles()) {
            val minutes : Long =  (java.util.Calendar.getInstance().timeInMillis - f.lastModified()) / (1000*60)
            if(minutes >= 5)
                f.delete()
        }
    }
}