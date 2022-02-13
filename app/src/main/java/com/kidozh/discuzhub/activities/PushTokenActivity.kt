package com.kidozh.discuzhub.activities

import android.os.Bundle
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.utilities.ConstUtils

class PushTokenActivity : BaseStatusActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_push_token)
    }

    fun loadIntentData(){

        discuz = intent.getSerializableExtra(ConstUtils.PASS_BBS_ENTITY_KEY) as Discuz
        user = intent.getSerializableExtra(ConstUtils.PASS_BBS_USER_KEY) as User?
    }
}