package com.example.myapplication.activities

import android.R.drawable
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.GroupsAdapter
import com.example.myapplication.adapter.ViewPagerAdapter
import com.example.myapplication.databinding.ActivityGroupBinding
import com.example.myapplication.fragments.ActiveListFragment
import com.example.myapplication.fragments.CompletedListFragment
import com.example.myapplication.models.FirebaseAuthWrapper
import com.example.myapplication.models.Group
import com.example.myapplication.models.getGroups
import kotlinx.coroutines.*


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
        supportActionBar?.setTitle(groupName)


        //supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_pageview_24)
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val fragmentArrayList = ArrayList<Fragment>()
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                fragmentArrayList.add(ActiveListFragment.newInstance(groupId!!, FirebaseAuthWrapper(this@GroupActivity).getUid()!!, groupName!!))
                fragmentArrayList.add(CompletedListFragment.newInstance(groupId!!,FirebaseAuthWrapper(this@GroupActivity).getUid()!!, groupName!!))
                withContext(Dispatchers.Main) {
                    val adapter = ViewPagerAdapter(this@GroupActivity, supportFragmentManager, fragmentArrayList)
                    binding!!.viewPager.adapter = adapter
                    binding!!.tabs.setupWithViewPager(binding!!.viewPager)
                }
            }
        }

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