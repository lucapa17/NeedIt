package com.example.myapplication.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.adapter.ViewPagerAdapter
import com.example.myapplication.databinding.ActivityMain2Binding
import com.example.myapplication.fragments.BlankFragment
import com.example.myapplication.fragments.BlankFragment2

class MainActivity2 : AppCompatActivity() {

    private var binding : ActivityMain2Binding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding!!.root)

        val fragmentArrayList = ArrayList<Fragment>()

        fragmentArrayList.add(BlankFragment())
        fragmentArrayList.add(BlankFragment2())

        val adapter = ViewPagerAdapter(this, supportFragmentManager, fragmentArrayList)
        binding!!.viewPager.adapter = adapter
        binding!!.tabs.setupWithViewPager(binding!!.viewPager)

    }
}