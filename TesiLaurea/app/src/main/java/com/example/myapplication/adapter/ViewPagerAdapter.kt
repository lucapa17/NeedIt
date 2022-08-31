package com.example.myapplication.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.myapplication.R

class ViewPagerAdapter(fm: FragmentManager?, private val list: ArrayList<Fragment>, c : Context) :
    FragmentPagerAdapter(fm!!) {
    private val TAB_TITLES = arrayOf(c.resources.getString(R.string.activeList), c.resources.getString(R.string.completedList))

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Fragment {
        return list[position]
    }

    override fun getPageTitle(position: Int): CharSequence {
        return TAB_TITLES[position]

    }

}