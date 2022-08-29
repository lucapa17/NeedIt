package com.example.myapplication.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class ItemsAdapter (val c: Context, val itemsList:ArrayList<String>, val isSent : Boolean): RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>() {
    inner class ItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var card : CardView
        var item: TextView
        var remove : ImageView
        init {
            item = v.findViewById(R.id.item)
            remove = v.findViewById(R.id.removeItem)
            card = v.findViewById(R.id.cardList)
            if(isSent){
                remove.visibility = View.GONE
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsAdapter.ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v  = inflater.inflate(R.layout.list_new_item,parent,false)
        return ItemViewHolder(v)
    }
    override fun onBindViewHolder(holder: ItemsAdapter.ItemViewHolder, position: Int) {
        val newList = itemsList[position]
        holder.item.text = newList
        holder.remove.setOnClickListener {
            itemsList.remove(newList)
            notifyDataSetChanged()
        }
    }
    override fun getItemCount(): Int {
        return itemsList.size
    }
}