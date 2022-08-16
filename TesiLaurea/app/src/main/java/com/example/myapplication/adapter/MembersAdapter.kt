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
import com.example.myapplication.activities.GroupActivity
import com.example.myapplication.models.FirebaseStorageWrapper
import com.example.myapplication.models.Group
import com.example.myapplication.models.User
import com.example.myapplication.models.getUserIdByNickname
import kotlinx.coroutines.*
import java.io.File

class MembersAdapter (val c: Context, val memberList:ArrayList<User>): RecyclerView.Adapter<MembersAdapter.UserViewHolder>() {
    inner class UserViewHolder(val v: View): RecyclerView.ViewHolder(v) {
        var username: TextView
        var name: TextView
        var photo: ImageView

        init {
            username = v.findViewById<TextView>(R.id.username)
            name = v.findViewById<TextView>(R.id.name)
            photo = v.findViewById<ImageView>(R.id.photo)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembersAdapter.UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v  = inflater.inflate(R.layout.list_members,parent,false)
        return UserViewHolder(v)
    }

    override fun onBindViewHolder(holder: MembersAdapter.UserViewHolder, position: Int) {
        val newList = memberList[position]
        var uri : Uri? = null
        holder.username.text = newList.nickname
        holder.name.text = "${newList.name} ${newList.surname}"
        val progressDialog = ProgressDialog(c)
        progressDialog.setMessage("Fetching...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val dir: File = File(c.getCacheDir().getAbsolutePath())
        var found = false
        if (dir.exists()) {
            for (f in dir.listFiles()) {
                if(f.name.toString().contains("image_${newList.id}_")){
                    if(!(f.length() == 0L))
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