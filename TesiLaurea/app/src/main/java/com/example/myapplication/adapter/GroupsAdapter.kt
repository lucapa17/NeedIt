package com.example.myapplication.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.activities.GroupActivity
import com.example.myapplication.models.*

class GroupsAdapter (private val c:Context, val groupList:ArrayList<Group>, private val photoList:ArrayList<String?>, private val unreadList:ArrayList<Int>):RecyclerView.Adapter<GroupsAdapter.UserViewHolder>() {
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
                (c as Activity).finish()
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
        val newList = groupList[position]
        holder.nameGroup.text = newList.nameGroup
        if(photoList[position] != null)
            holder.logoGroup.setImageURI(photoList[position]!!.toUri())
        if(unreadList[position] != 0){
            holder.unread.text = unreadList[position].toString()
            holder.unread.visibility = View.VISIBLE
        }
    }
    override fun getItemCount(): Int {
        return  groupList.size
    }
}