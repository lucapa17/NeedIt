package com.example.myapplication.adapter

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.net.toFile
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.activities.GroupActivity
import com.example.myapplication.models.*
import kotlinx.coroutines.*
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class GroupsAdapter (val c:Context,val groupList:ArrayList<Group>):RecyclerView.Adapter<GroupsAdapter.UserViewHolder>() {
    inner class UserViewHolder(val v:View):RecyclerView.ViewHolder(v) {
        var nameGroup: TextView
        var logoGroup: ImageView

        init {
            nameGroup = v.findViewById<TextView>(R.id.nameGroup)
            logoGroup = v.findViewById(R.id.logoGroup)
            v.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    val position = groupList[adapterPosition]
                    val intent = Intent(v!!.context, GroupActivity::class.java)
                    intent.putExtra("groupId", position.groupId)
                    intent.putExtra("groupName", position.nameGroup)

                    v!!.context.startActivity(intent)
                }
            })
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsAdapter.UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v  = inflater.inflate(R.layout.list_groups,parent,false)
        return UserViewHolder(v)
    }

    override fun onBindViewHolder(holder: GroupsAdapter.UserViewHolder, position: Int) {
        val progressDialog = ProgressDialog(c)
        progressDialog.setMessage("Fetching...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val newList = groupList[position]
        holder.nameGroup.text = newList.nameGroup
        var uri : Uri? = null
        val dir: File = File(c.getCacheDir().getAbsolutePath())
        var found = false
        if (dir.exists()) {
            for (f in dir.listFiles()) {
                if(f.name.toString().contains("image_${newList.groupId}_")){
                    Log.d(TAG, "dddddddd "+f)
                    if(!(f.length() == 0L))
                        holder.logoGroup.setImageURI(Uri.fromFile(f))
                    found = true
                    progressDialog.dismiss()
                    break
                }

            }

        }
        Log.d(TAG, "iiiiiiii"+ java.util.Calendar.getInstance().timeInMillis)
        for (f in dir.listFiles()) {
            val ciao : Long =  (java.util.Calendar.getInstance().timeInMillis - f.lastModified()) / (1000*60)
            Log.d(TAG, "iiii "+f.name)
            Log.d(TAG, "iiii "+f.lastModified())
            Log.d(TAG, "iiii "+ciao)
        }

            /*
            if(File(c.cacheDir, "images/${newList.groupId}_.jpg").exists())
                Log.d(TAG, "iiii exist")
            else
                Log.d(TAG, "iiii NO exist")
            */
        if(!found){
            CoroutineScope(Dispatchers.Main + Job()).launch {
                withContext(Dispatchers.IO) {
                    uri = FirebaseStorageWrapper().download(newList.groupId.toString(), c)
                    Log.d(TAG, "iiii "+uri)
                    withContext(Dispatchers.Main) {
                        if(uri != null){
                            holder.logoGroup.setImageURI(uri)
                            //uri!!.toFile().delete()
                        }
                        progressDialog.dismiss()
                    }
                }
            }
        }


    }

    override fun getItemCount(): Int {
        return  groupList.size
    }
}