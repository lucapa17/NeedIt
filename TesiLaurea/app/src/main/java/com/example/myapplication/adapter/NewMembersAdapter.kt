package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.User

class NewMembersAdapter (private val memberList:ArrayList<User>): RecyclerView.Adapter<NewMembersAdapter.UserViewHolder>() {
    inner class UserViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var username: TextView
        var name: TextView
        var remove : ImageView
        init {
            username = v.findViewById(R.id.username)
            name = v.findViewById(R.id.name)
            remove = v.findViewById(R.id.removeUser)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewMembersAdapter.UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v  = inflater.inflate(R.layout.list_new_members,parent,false)
        return UserViewHolder(v)
    }
    override fun onBindViewHolder(holder: NewMembersAdapter.UserViewHolder, position: Int) {
        val newList = memberList[position]
        holder.username.text = newList.nickname
        holder.name.text = "${newList.name} ${newList.surname}"
        holder.remove.setOnClickListener {
            memberList.remove(newList)
            notifyDataSetChanged()
        }
    }
    override fun getItemCount(): Int {
        return memberList.size
    }
}