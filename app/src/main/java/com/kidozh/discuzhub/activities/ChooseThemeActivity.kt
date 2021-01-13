package com.kidozh.discuzhub.activities

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.adapter.AppThemeAdapter
import com.kidozh.discuzhub.databinding.ActivityChooseThemeBinding
import com.kidozh.discuzhub.entities.AppTheme
import com.kidozh.discuzhub.utilities.ThemeUtils
import com.kidozh.discuzhub.utilities.UserPreferenceUtils
import es.dmoral.toasty.Toasty

class ChooseThemeActivity : AppCompatActivity(), AppThemeAdapter.OnThemeCardClicked {
    lateinit var binding: ActivityChooseThemeBinding
    val TAG = ChooseThemeActivity::class.simpleName
    val styleList = intArrayOf(
            R.style.AppTheme_Default, R.style.AppTheme_Red, R.style.AppTheme_Purple,
            R.style.AppTheme_Indigo, R.style.AppTheme_Cyan, R.style.AppTheme_Green,
            R.style.AppTheme_Lime, R.style.AppTheme_Yellow, R.style.AppTheme_Orange,
            R.style.AppTheme_Gray, R.style.AppTheme_Black,
    )

    lateinit var themeList : ArrayList<AppTheme>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureTheme()
        binding = ActivityChooseThemeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureActionBar()
        configureRecyclerview()
    }

    fun configureTheme(){
        val position = UserPreferenceUtils.getThemeIndex(this)
        if(position < styleList.size){

            setTheme(styleList[position])
            //theme.applyStyle(styleList[position],true)

        }
    }

    fun configureActionBar(){
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun configureRecyclerview(){
        themeList = ThemeUtils(this).themeList
        val position = UserPreferenceUtils.getThemeIndex(this)
        binding.themeRecyclerview.layoutManager = GridLayoutManager(this, 4)
        Log.d(TAG, "Configure recyclerview "+themeList.size)

        val adapter = AppThemeAdapter()
        binding.themeRecyclerview.adapter = adapter
        adapter.addAppTheme(themeList,position)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                finishAfterTransition()
                return true
            }

        }
        return false
    }

    override fun onThemeCardSelected(position: Int) {
        // Change theme
        if(position < styleList.size){
            Toasty.info(this,getString(themeList[position].nameResource),Toast.LENGTH_SHORT).show()
            setTheme(styleList[position])
            theme.applyStyle(styleList[position],true)
            UserPreferenceUtils.setThemeIndex(this, position)
            recreate()
        }

    }
}