package com.example.myapplication.activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.MembersAdapter
import com.example.myapplication.models.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.io.File

class InfoGroupActivity: AppCompatActivity() {
    private lateinit var recv: RecyclerView
    private lateinit var membersAdapter: MembersAdapter
    private var image: Uri? = null
    var groupId : Long? = null
    var group : Group? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_group)
        val intent : Intent = intent
        groupId = intent.getLongExtra("groupId", 0L)

        recv = this.findViewById(R.id.mRecycler)
        membersAdapter = MembersAdapter(this, ArrayList())
        recv.layoutManager = LinearLayoutManager(this)
        recv.adapter = membersAdapter

        var uri : Uri? = null
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Fetching...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                group = getGroupById(this@InfoGroupActivity, groupId!!)
                val dir: File = File(this@InfoGroupActivity.cacheDir.absolutePath)
                var found = false
                if (dir.exists()) {
                    for (f in dir.listFiles()) {
                        if(f.name.toString().contains("image_${groupId}_")){
                            if(f.length() != 0L)
                                uri = Uri.fromFile(f)
                            found = true
                            break
                        }
                    }
                }
                if(!found)
                    uri = FirebaseStorageWrapper().download(groupId.toString(), this@InfoGroupActivity)
                val groupMembersList : MutableList<User> = mutableListOf()
                for (user in group!!.users!!){
                    val user : User? = getUserById(this@InfoGroupActivity, user)
                    groupMembersList.add(user!!)
                }
                withContext(Dispatchers.Main) {
                    if(uri != null)
                        findViewById<ImageView>(R.id.group_image).setImageURI(uri)
                    membersAdapter = MembersAdapter(this@InfoGroupActivity, ArrayList(groupMembersList))
                    recv.layoutManager = LinearLayoutManager(this@InfoGroupActivity)
                    recv.adapter = membersAdapter
                    progressDialog.dismiss()
                }
            }
        }

        val editPhoto : ImageView = findViewById(R.id.edit_group_photo)
        editPhoto.setOnClickListener {
            val i = Intent()
            i.type = "image/*"
            i.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(i, 100)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100 && resultCode == RESULT_OK){
            image = data?.data!!
            findViewById<ImageView>(R.id.group_image).setImageURI(image)
            GlobalScope.launch{
                //FirebaseStorageWrapper().delete(id)
                val dir = File(this@InfoGroupActivity.cacheDir.absolutePath)
                if (dir.exists()) {
                    for (f in dir.listFiles()) {
                        if(f.name.toString().contains("image_${groupId}_")){
                            f.delete()
                        }
                    }
                }
                FirebaseStorageWrapper().upload(image!!, groupId.toString(), this@InfoGroupActivity)
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.nav_menu_infogroup, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_leave -> {
                GlobalScope.launch {
                    val user : User = getUser(this@InfoGroupActivity)
                    val requestList : MutableList<Request> = getRequestsList(this@InfoGroupActivity, group!!.groupId)
                    for(request in requestList){
                        if(request.user.id == user.id){
                            Firebase.database.getReference("requests").child(request.id.toString()).removeValue()
                        }
                    }
                    user.groups!!.remove(groupId)
                    Firebase.database.getReference("users").child(user.id).setValue(user)
                    group!!.users!!.remove(user.id)
                    if(group!!.users!!.isEmpty()){
                        FirebaseStorageWrapper().delete(group!!.groupId.toString())
                        Firebase.database.getReference("groups").child(group!!.groupId.toString()).removeValue()
                    }
                    else{
                        Firebase.database.getReference("groups").child(group!!.groupId.toString()).setValue(group)
                    }
                    val intent = Intent(this@InfoGroupActivity, MainActivity::class.java)
                    this@InfoGroupActivity.startActivity(intent)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}