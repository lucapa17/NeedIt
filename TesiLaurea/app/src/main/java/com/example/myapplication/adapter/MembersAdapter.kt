package com.example.myapplication.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.activities.GroupActivity
import com.example.myapplication.models.Group
import com.example.myapplication.models.User

class MembersAdapter (val c: Context, val memberList:ArrayList<User>): RecyclerView.Adapter<MembersAdapter.UserViewHolder>() {
    inner class UserViewHolder(val v: View): RecyclerView.ViewHolder(v) {
        var username: TextView
        var photo: ImageView

        init {
            username = v.findViewById<TextView>(R.id.username)
            photo = v.findViewById(R.id.photo)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembersAdapter.UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v  = inflater.inflate(R.layout.list_members,parent,false)
        return UserViewHolder(v)
    }

    override fun onBindViewHolder(holder: MembersAdapter.UserViewHolder, position: Int) {
        val newList = memberList[position]
        holder.username.text = newList.nickname

    }

    override fun getItemCount(): Int {
        return  memberList.size
    }
}