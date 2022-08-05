package com.example.myapplication.activities

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
import com.example.myapplication.databinding.ActivityGroupBinding
import com.example.myapplication.fragments.ActiveListFragment
import com.example.myapplication.fragments.CompletedListFragment
import com.example.myapplication.models.FirebaseAuthWrapper

class GroupActivity : AppCompatActivity() {

    private var binding : ActivityGroupBinding? = null
    private var groupId : Long? = null
    private var groupName : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        val intent : Intent = getIntent()
        groupId = intent.getLongExtra("groupId", 0L)
        groupName = intent.getStringExtra("groupName")

        val titleGroup = findViewById<TextView>(R.id.titleGroup)
        titleGroup.setText(groupName)
        val fragmentArrayList = ArrayList<Fragment>()

        fragmentArrayList.add(ActiveListFragment.newInstance(groupId!!, FirebaseAuthWrapper(this).getUid()!!, groupName!!))
        fragmentArrayList.add(CompletedListFragment.newInstance(groupId!!,FirebaseAuthWrapper(this).getUid()!!, groupName!!))

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
                val intent : Intent = Intent(this, InfoGroupActivity::class.java)
                intent.putExtra("groupId", groupId)
                this.startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
    }


}