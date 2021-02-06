package com.kidozh.discuzhub.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.kidozh.discuzhub.activities.ui.smiley.MarkedSmileyFragment
import com.kidozh.discuzhub.activities.ui.smiley.SmileyFragment
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.Smiley

class SmileyViewPagerAdapter(fm: FragmentManager, behavior: Int, val discuz: Discuz) : FragmentStatePagerAdapter(fm, behavior) {
    var smileyList: List<List<Smiley>> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()

        }

    var latestSmileyList : List<Smiley> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getPageTitle(position: Int): CharSequence {
        if(position == 0){
            return "HISTRO"
        }
        else{
            return position.toString()
        }

    }

    override fun getItem(position: Int): Fragment {
        if(position == 0){
            return MarkedSmileyFragment.newInstance(discuz)
        }
        else{
            val cateSmileyInfo = smileyList[position-1] as ArrayList<Smiley>

            return SmileyFragment.newInstance(discuz,cateSmileyInfo)
        }

    }

    override fun getCount(): Int {
        return smileyList.size + 1
    }
}