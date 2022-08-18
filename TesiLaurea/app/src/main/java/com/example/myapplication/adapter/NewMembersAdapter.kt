package com.example.myapplication.adapter

import android.app.ProgressDialog
import android.content.Context
import android.media.Image
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.FirebaseStorageWrapper
import com.example.myapplication.models.User
import com.example.myapplication.models.getUserByNickname
import com.example.myapplication.models.getUserIdByNickname
import kotlinx.coroutines.*
import java.io.File

class NewMembersAdapter (val c: Context, val memberList:ArrayList<User>): RecyclerView.Adapter<NewMembersAdapter.UserViewHolder>() {
    inner class UserViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
        var username: TextView
        var name: TextView
        var remove : ImageView

        init {
            username = v.findViewById<TextView>(R.id.username)
            name = v.findViewById<TextView>(R.id.name)
            remove = v.findViewById<ImageView>(R.id.removeUser)

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

        holder.remove.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                memberList.remove(newList)
                notifyDataSetChanged()
            }
        })

    }

    override fun getItemCount(): Int {
        return memberList.size
    }
}