package com.example.myapplication.activities

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.GroupsAdapter
import com.example.myapplication.adapter.ListAdapter
import com.example.myapplication.models.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
        CoroutineScope(Dispatchers.Main + Job()).launch {

            withContext(Dispatchers.IO) {
                var groupList : MutableList<Group> = getGroups(this@MainActivity)
                withContext(Dispatchers.Main) {
                    groupsAdapter = GroupsAdapter(this@MainActivity, ArrayList(groupList))
                    recv.layoutManager = LinearLayoutManager(this@MainActivity)
                    recv.adapter = groupsAdapter
                    progressDialog.dismiss()
                }
            }
        }


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