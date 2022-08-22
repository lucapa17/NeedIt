package com.example.myapplication.adapter

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.activities.GroupActivity
import com.example.myapplication.models.*
import kotlinx.coroutines.*
import java.io.File

class GroupsAdapter (private val c:Context, val groupList:ArrayList<Group>):RecyclerView.Adapter<GroupsAdapter.UserViewHolder>() {
    inner class UserViewHolder(v:View):RecyclerView.ViewHolder(v) {
        var nameGroup: TextView
        var logoGroup: ImageView
        var unread: TextView
        init {
            nameGroup = v.findViewById(R.id.nameGroup)
            logoGroup = v.findViewById(R.id.logoGroup)
            unread = v.findViewById(R.id.unread)
            v.setOnClickListener { v ->
                val position = groupList[adapterPosition]
                val intent = Intent(v!!.context, GroupActivity::class.java)
                intent.putExtra("groupId", position.groupId)
                intent.putExtra("groupName", position.nameGroup)
                v.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsAdapter.UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v  = inflater.inflate(R.layout.list_groups,parent,false)
        return UserViewHolder(v)
    }

    override fun onBindViewHolder(holder: GroupsAdapter.UserViewHolder, position: Int) {
        val progressDialog = ProgressDialog(c)
        progressDialog.setMessage("Wait...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val newList = groupList[position]
        holder.nameGroup.text = newList.nameGroup
        var uri: Uri? = null
        val dir = File(c.cacheDir.absolutePath)
        var found = false
        if (dir.exists()) {
            for (f in dir.listFiles()) {
                if(f.name.toString().contains("image_${newList.groupId}_")){
                    if(f.length() != 0L)
                        holder.logoGroup.setImageURI(Uri.fromFile(f))
                    found = true
                    progressDialog.dismiss()
                    break
                }
            }
        }
        var unreadMessages : Int
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                if(!found) {
                    uri = FirebaseStorageWrapper().download(newList.groupId.toString(), c)
                }
                unreadMessages = getUnread(c, newList.groupId, FirebaseAuthWrapper(c).getUid()!!)
                withContext(Dispatchers.Main) {
                    if(unreadMessages != 0){
                        holder.unread.text = unreadMessages.toString()
                        holder.unread.visibility = View.VISIBLE
                    }
                    if(uri != null){
                        holder.logoGroup.setImageURI(uri)
                    }
                    progressDialog.dismiss()
                }
            }
        }

    }
    override fun getItemCount(): Int {
        return  groupList.size
    }
}