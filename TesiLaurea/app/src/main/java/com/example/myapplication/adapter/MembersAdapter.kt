package com.example.myapplication.adapter

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.activities.ShowProfile
import com.example.myapplication.models.FirebaseStorageWrapper
import com.example.myapplication.models.User
import com.example.myapplication.models.getUserIdByNickname
import kotlinx.coroutines.*
import java.io.File

class MembersAdapter (private val c: Context, private val memberList:ArrayList<User>): RecyclerView.Adapter<MembersAdapter.UserViewHolder>() {
    inner class UserViewHolder(v: View): RecyclerView.ViewHolder(v) {
        var username: TextView = v.findViewById(R.id.username)
        var name: TextView = v.findViewById(R.id.name)
        var photo: ImageView = v.findViewById(R.id.photo)
        init {
            v.setOnClickListener {
                val position = memberList[adapterPosition]
                val intent = Intent(v.context, ShowProfile::class.java)
                intent.putExtra("id", position.id)
                v.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembersAdapter.UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v  = inflater.inflate(R.layout.list_members,parent,false)
        return UserViewHolder(v)
    }

    override fun onBindViewHolder(holder: MembersAdapter.UserViewHolder, position: Int) {
        val newList = memberList[position]
        var uri: Uri?
        holder.username.text = newList.nickname
        holder.name.text = "${newList.name} ${newList.surname}"
        val progressDialog = ProgressDialog(c)
        progressDialog.setMessage(c.resources.getString(R.string.wait))
        progressDialog.setCancelable(false)
        progressDialog.show()
        val dir = File(c.cacheDir.absolutePath)
        var found = false
        if (dir.exists()) {
            for (f in dir.listFiles()) {
                if(f.name.toString().contains("image_${newList.id}_")){
                    if(f.length() != 0L)
                        holder.photo.setImageURI(Uri.fromFile(f))
                    found = true
                    progressDialog.dismiss()
                    break
                }
            }
        }
        if(!found){
            CoroutineScope(Dispatchers.Main + Job()).launch {
                withContext(Dispatchers.IO) {
                    val id = getUserIdByNickname(c, newList.nickname)
                    uri = FirebaseStorageWrapper().download(id!!, c)
                    withContext(Dispatchers.Main) {
                        if(uri != null)
                            holder.photo.setImageURI(uri)
                        progressDialog.dismiss()
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return  memberList.size
    }
}