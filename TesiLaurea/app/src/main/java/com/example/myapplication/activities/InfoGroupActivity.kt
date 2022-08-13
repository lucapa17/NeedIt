package com.example.myapplication.activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.GroupsAdapter
import com.example.myapplication.adapter.MembersAdapter
import com.example.myapplication.models.*
import kotlinx.coroutines.*

class InfoGroupActivity: AppCompatActivity() {
    private lateinit var recv: RecyclerView
    private lateinit var membersAdapter: MembersAdapter
    var image: Uri? = null
    var groupId : Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_group)

        val intent : Intent = getIntent()
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
                val group : Group = getGroupById(this@InfoGroupActivity, groupId!!)
                uri = FirebaseStorageWrapper().download(groupId.toString())
                val groupMembersList : MutableList<User> = mutableListOf()
                for (user in group.users!!){
                    val user : User = getUserById(this@InfoGroupActivity, user)
                    groupMembersList.add(user)
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

        val edit_photo : ImageView = findViewById(R.id.edit_group_photo)
        edit_photo.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v : View?) {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent, 100)

            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 100 && resultCode == RESULT_OK){
            image = data?.data!!
            findViewById<ImageView>(R.id.group_image).setImageURI(image)
            GlobalScope.launch{
                //FirebaseStorageWrapper().delete(id)
                FirebaseStorageWrapper().upload(image!!, groupId.toString(), this@InfoGroupActivity)
            }

        }
    }

}