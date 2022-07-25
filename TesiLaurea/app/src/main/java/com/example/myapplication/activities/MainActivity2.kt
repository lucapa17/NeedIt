package com.example.myapplication.activities

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.adapter.ViewPagerAdapter
import com.example.myapplication.databinding.ActivityMain2Binding
import com.example.myapplication.fragments.ActiveListFragment
import com.example.myapplication.fragments.CompletedListFragment

class MainActivity2 : AppCompatActivity() {

    private var binding : ActivityMain2Binding? = null
    private var groupId : Long? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding!!.root)

        val intent : Intent = getIntent()
        groupId = intent.getLongExtra("groupId", 0L)
        val groupName : String? = intent.getStringExtra("groupName")
        Log.d(TAG, "wwwwei: "+ groupName)

        val titleGroup = findViewById<TextView>(R.id.titleGroup)
        titleGroup.setText(groupName)
        val fragmentArrayList = ArrayList<Fragment>()

        fragmentArrayList.add(ActiveListFragment.newInstance(groupId!!))
        fragmentArrayList.add(CompletedListFragment.newInstance(groupId!!))

        val adapter = ViewPagerAdapter(this, supportFragmentManager, fragmentArrayList)
        binding!!.viewPager.adapter = adapter
        binding!!.tabs.setupWithViewPager(binding!!.viewPager)

    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(com.example.myapplication.R.menu.nav_menu_group, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            com.example.myapplication.R.id.nav_home -> {
                val intent : Intent = Intent(this, MainActivity::class.java)
                this.startActivity(intent)
                true
            }
            com.example.myapplication.R.id.nav_add_member -> {
                val intent : Intent = Intent(this, AddMemberActivity::class.java)
                intent.putExtra("groupId", groupId)

                this.startActivity(intent)
                true
            }
            com.example.myapplication.R.id.nav_show_members -> {
                val intent : Intent = Intent(this, GroupActivity::class.java)
                intent.putExtra("groupId", groupId)
                this.startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}