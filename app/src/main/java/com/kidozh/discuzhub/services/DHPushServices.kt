package com.kidozh.discuzhub.services

import com.kidozh.discuzhub.results.PostTokenResult
import com.kidozh.discuzhub.results.TokenResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface DHPushServices {
    @GET("/plugin.php?id=dhpush:token")
    fun tokenList(): Call<TokenResult>

    @POST("/plugin.php?id=dhpush:token")
    fun sendToken(
        @Query("formhash") formHash: String,
        @Query("token") token: String,
        @Query("deviceName") deviceName:String
    ): Call<PostTokenResult>
}