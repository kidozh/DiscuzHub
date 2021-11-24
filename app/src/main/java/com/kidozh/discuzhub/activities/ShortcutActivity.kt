package com.kidozh.discuzhub.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kidozh.discuzhub.databinding.ActivityShortcutBinding

class ShortcutActivity : AppCompatActivity() {
    lateinit var binding: ActivityShortcutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShortcutBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}