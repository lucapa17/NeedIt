package com.example.myapplication.activities

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.adapter.ViewPagerAdapter
import com.example.myapplication.databinding.ActivityGroupBinding
import com.example.myapplication.fragments.ActiveListFragment
import com.example.myapplication.fragments.CompletedListFragment
import com.example.myapplication.models.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.io.File

class GroupActivity : AppCompatActivity() {
    private var binding : ActivityGroupBinding? = null
    private var groupId : Long? = null
    private var groupName : String? = null
    private var valueEventListener : ValueEventListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        val intent : Intent = intent
        groupId = intent.getLongExtra("groupId", 0L)
        groupName = intent.getStringExtra("groupName")
        supportActionBar?.title = groupName
        Firebase.database.getReference("unread").child(FirebaseAuthWrapper(this).getUid()!!).child(groupId.toString()).setValue(0)
        val fragmentArrayList = ArrayList<Fragment>()
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val group = getGroupById(this@GroupActivity, groupId!!)
                val photoList : ArrayList<String> = ArrayList()
                for(user in group!!.users!!){
                    var uri : Uri? = null
                    val dir = File(this@GroupActivity.cacheDir.absolutePath)
                    var found = false
                    if (dir.exists()) {
                        for (f in dir.listFiles()) {
                            if(f.name.toString().contains("image_${user}_")){
                                if(f.length() != 0L){
                                    uri = Uri.fromFile(f)
                                }
                                found = true
                                break
                            }
                        }
                    }
                    if(!found){
                        uri = FirebaseStorageWrapper().download(user, this@GroupActivity)
                    }
                    if(uri != null)
                        photoList.add(uri.toString())
                }
                fragmentArrayList.add(ActiveListFragment.newInstance(groupId!!, FirebaseAuthWrapper(this@GroupActivity).getUid()!!, groupName!!, photoList))
                fragmentArrayList.add(CompletedListFragment.newInstance(groupId!!,FirebaseAuthWrapper(this@GroupActivity).getUid()!!, groupName!!, photoList))
                withContext(Dispatchers.Main) {
                    val adapter = ViewPagerAdapter(supportFragmentManager, fragmentArrayList)
                    binding!!.viewPager.adapter = adapter
                    binding!!.tabs.setupWithViewPager(binding!!.viewPager)
                    progressDialog.dismiss()


                }
            }
        }
        var requests: ArrayList<Request>
        GlobalScope.launch {
            val requestList : MutableList<Request> = getRequestsList(this@GroupActivity, groupId!!)
            requests = ArrayList(requestList)
            valueEventListener = Firebase.database.getReference("requests").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list : ArrayList<Request> = ArrayList()
                    for(child in snapshot.children){
                        val request = child.getValue(Request::class.java)
                        if(request!!.groupId == groupId)
                            list.add(request)
                    }
                    var same = true
                    if(list.size != requests.size){
                        same = false
                    }
                    else {
                        for(i in list.indices){
                            if(list.get(i).equals(requests.get(i))){
                                same = false
                                break
                            }
                        }
                    }
                    if(!same){
                        //requireActivity().finish()
                        //requireActivity().overridePendingTransition(0,0)
                        finish()
                        val intent  = Intent(this@GroupActivity, GroupActivity::class.java)
                        intent.putExtra("groupId", groupId)
                        intent.putExtra("groupName", groupName)
                        this@GroupActivity.startActivity(intent)
                        //requireActivity().overridePendingTransition(0,0)
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
                    Log.d(ContentValues.TAG, "rrrr1")
                }

            })
        }

    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(com.example.myapplication.R.menu.nav_menu_group, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            com.example.myapplication.R.id.nav_add_member -> {
                val intent  = Intent(this, AddMemberActivity::class.java)
                intent.putExtra("groupId", groupId)

                this.startActivity(intent)
                true
            }
            com.example.myapplication.R.id.nav_show_members -> {
                val intent  = Intent(this, InfoGroupActivity::class.java)
                intent.putExtra("groupId", groupId)
                this.startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onRestart() {
        super.onRestart()
        val intent  = Intent(this, GroupActivity::class.java)
        intent.putExtra("groupId", groupId)
        intent.putExtra("groupName", groupName)
        this.startActivity(intent)

    }
    override fun onPause() {
        super.onPause()
        if(valueEventListener != null)
            Firebase.database.getReference("requests").removeEventListener(valueEventListener!!) //ref will be your node where you are setting Event Listener.
    }


}