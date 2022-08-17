package com.example.myapplication.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.MembersAdapter
import com.example.myapplication.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.coroutines.*
import java.io.File

class NewGroupActivity : BaseActivity() {
    var image: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_group)
        val uid = FirebaseAuthWrapper(this).getUid()

        val edit_photo : ImageView = findViewById(R.id.edit_group_photo)
        edit_photo.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v : View?) {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent, 100)

            }
        })
        val button : Button = findViewById(R.id.buttonCreateGroup)
        button.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v : View?) {
                val groupName : EditText = findViewById(R.id.groupName)
                if(groupName.text.toString().isEmpty()) {
                    Toast.makeText(v!!.context, "Insert the group name!", Toast.LENGTH_SHORT).show()
                }
                else{
                    var groupId : Long = -1
                    CoroutineScope(Dispatchers.Main + Job()).launch {
                        withContext(Dispatchers.IO) {
                            groupId = getGroupId(this@NewGroupActivity)
                            val group : Group = Group(groupId, groupName.text.toString(), mutableListOf(uid!!))
                            createGroup(group, this@NewGroupActivity)
                            if(image != null)
                                FirebaseStorageWrapper().upload(image!!, groupId.toString(), this@NewGroupActivity)

                            }
                            withContext(Dispatchers.Main) {
                                Thread.sleep(1_000)
                                val dir: File = File(this@NewGroupActivity.getCacheDir().getAbsolutePath())
                                if (dir.exists()) {
                                    for (f in dir.listFiles()) {
                                        if(f.name.toString().contains("image_${groupId}_")){
                                            f.delete()
                                        }

                                    }
                                }
                                val intent : Intent = Intent(this@NewGroupActivity, MainActivity::class.java)
                                this@NewGroupActivity.startActivity(intent)
                            }
                    }
                }


            }

        })

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 100 && resultCode == RESULT_OK){
            image = data?.data!!
            findViewById<ImageView>(R.id.group_image).setImageURI(image)

        }
    }


}