package com.kidozh.discuzhub.services

import com.google.firebase.messaging.FirebaseMessagingService

class FirebasePushService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }
}