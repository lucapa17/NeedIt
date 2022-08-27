package com.example.myapplication.activities

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.GroupsAdapter
import com.example.myapplication.models.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

import java.io.File


class MainActivity : BaseActivity() {
    private lateinit var recv: RecyclerView
    private lateinit var groupsAdapter: GroupsAdapter
    private val uid = FirebaseAuthWrapper(this@MainActivity).getUid()
    private var valueEventListener : ValueEventListener? = null


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
                groupList.reverse()
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

        GlobalScope.launch {
            val listUnread = getUnreadList(this@MainActivity, uid!!)
            Log.d(TAG, "rrrr0size "+listUnread.size)



            valueEventListener = Firebase.database.getReference("unread").child(uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = ArrayList<Int>()
                    for(child in snapshot.children){
                        list.add(child.getValue(Int::class.java)!!)
                    }
                    if(!listUnread.equals(list)){
                        finish()
                        val intent  = Intent(this@MainActivity, MainActivity::class.java)
                        this@MainActivity.startActivity(intent)
                    }




                /*
                    val listUnread1 = getUnreadList(this@MainActivity, uid)
                    for(i in listUnread1){
                        Log.d(TAG, "rrrr1 "+i.toString())
                    }
                    if(listUnread1.equals(listUnread))
                        Log.d(TAG, "rrrr2 uguali")
                    else
                        Log.d(TAG, "rrrr2 diversi")

                     */


                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "rrrr1")
                }

            })
        }







    }
    override fun onBackPressed() {
        finishAffinity()
    }


    override fun onRestart() {
        super.onRestart()
        val intent  = Intent(this, MainActivity::class.java)
        this.startActivity(intent)

    }

    override fun onPause() {
        super.onPause()
        if(valueEventListener != null)
            Firebase.database.getReference("unread").child(uid!!).removeEventListener(valueEventListener!!) //ref will be your node where you are setting Event Listener.
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