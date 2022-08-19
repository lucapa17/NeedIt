package com.example.myapplication.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(fm: FragmentManager?, private val list: ArrayList<Fragment>) :
    FragmentPagerAdapter(fm!!) {
    override fun getCount(): Int {
        //return 2
        return list.size
    }

    override fun getItem(position: Int): Fragment {
        return list[position]
    }

    override fun getPageTitle(position: Int): CharSequence {
        return TAB_TITLES[position]
    }

    companion object{
        val TAB_TITLES = arrayOf("Active List", "Completed List")
    }

}