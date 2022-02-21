package com.kidozh.discuzhub.services

import com.kidozh.discuzhub.results.PostTokenResult
import com.kidozh.discuzhub.results.TokenResult
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface DHPushServices {
    @GET("/plugin.php?id=dhpush:token")
    fun tokenList(): Call<TokenResult>

    @POST("/plugin.php?id=dhpush:token")
    @FormUrlEncoded
    fun sendToken(
        @Field("formhash") formHash: String,
        @Field("token") token: String,
        @Field("deviceName") deviceName:String,
        @Field("packageId") packageId:String,
        @Field("channel") pushChannel: String
    ): Call<PostTokenResult>
}