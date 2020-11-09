package com.kidozh.discuzhub.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kidozh.discuzhub.databinding.ActivitySingleDiscuzBinding

class SingleDiscuzActivity : BaseStatusActivity() {
    lateinit var binding: ActivitySingleDiscuzBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleDiscuzBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}