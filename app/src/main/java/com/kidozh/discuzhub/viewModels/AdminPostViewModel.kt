package com.kidozh.discuzhub.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.Post
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.results.ApiMessageActionResult
import com.kidozh.discuzhub.results.MessageResult
import com.kidozh.discuzhub.services.DiscuzApiService
import com.kidozh.discuzhub.utilities.NetworkUtils
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

val POST_HIDDEN = 1
val POST_WARNED = 2
val POST_REVISED = 4
val POST_MOBILE = 8

class AdminPostViewModel(application: Application) : AndroidViewModel(application) {
    val blockState: MutableLiveData<Boolean> = MutableLiveData(false)
    val warnState: MutableLiveData<Boolean> = MutableLiveData(false)
    val stickState: MutableLiveData<Boolean> = MutableLiveData(false)
    val sendingBlockRequest: MutableLiveData<Boolean> = MutableLiveData(false)
    val sendingWarnRequest: MutableLiveData<Boolean> = MutableLiveData(false)
    val sendingStickRequest: MutableLiveData<Boolean> = MutableLiveData(false)
    val sendingDeleteRequest: MutableLiveData<Boolean> = MutableLiveData(false)
    val returnedMessage = MutableLiveData<MessageResult?>(null)
    val networkError = MutableLiveData<Boolean>(false)
    val reasonMutableLiveData: MutableLiveData<String> = MutableLiveData("")
    lateinit var newPostMutableLiveData: MutableLiveData<Post>
    var fid = 0
    var tid = 0
    var formHash = ""
    lateinit var post: Post
    lateinit var client: OkHttpClient
    lateinit var retrofit: Retrofit
    lateinit var service: DiscuzApiService

    fun initPostStatus(discuz: Discuz, user: User, post: Post, fid: Int, tid: Int, formHash: String){
        blockState.postValue(post.status and POST_HIDDEN != 0 )
        warnState.postValue(post.status and POST_WARNED != 0 )
        this.fid = fid
        this.tid = tid
        this.formHash = formHash
        this.post = post
        newPostMutableLiveData = MutableLiveData(post)
        client = NetworkUtils.getPreferredClientWithCookieJarByUser(getApplication(), user)
        retrofit = NetworkUtils.getRetrofitInstance(discuz.base_url, client)
        service = retrofit.create(DiscuzApiService::class.java)
    }

    fun sendWarnRequest(){
        val warn: Boolean = warnState.value!!
        val warnParameter: Int = if(warn){
            0
        } else{
            1
        }
        sendingWarnRequest.postValue(true)
        service.warnPost(formHash,fid,tid, listOf(post.pid),warnParameter,reasonMutableLiveData.value!!).enqueue(object :Callback<ApiMessageActionResult>{
            override fun onResponse(
                call: Call<ApiMessageActionResult>,
                response: Response<ApiMessageActionResult>
            ) {
                val res = response.body()
                if(response.isSuccessful && res!=null){
                    val newPost: Post = newPostMutableLiveData.value!!
                    returnedMessage.postValue(res.message)
                    if(res.message?.key  == "admin_succeed"){
                        newPost.status = newPost.status xor POST_WARNED
                        newPostMutableLiveData.postValue(newPost)

                        warnState.postValue(!warn)
                    }
                }
                else{
                    networkError.postValue(true);
                }
                sendingWarnRequest.postValue(false)
            }

            override fun onFailure(call: Call<ApiMessageActionResult>, t: Throwable) {
                sendingWarnRequest.postValue(true)
            }

        })
    }

    fun sendBlockRequest(){
        val blocked: Boolean = blockState.value!!
        val blockParameter: Int = if(blocked){
            0
        } else{
            1
        }
        sendingWarnRequest.postValue(true)
        service.banPost(formHash,fid,tid, listOf(post.pid),blockParameter,reasonMutableLiveData.value!!).enqueue(object :Callback<ApiMessageActionResult>{
            override fun onResponse(
                call: Call<ApiMessageActionResult>,
                response: Response<ApiMessageActionResult>
            ) {
                val res = response.body()
                if(response.isSuccessful && res!=null){
                    returnedMessage.postValue(res.message)
                    if(res.message?.key  == "admin_succeed"){
                        val newPost: Post = newPostMutableLiveData.value!!
                        newPost.status = newPost.status xor POST_HIDDEN
                        newPostMutableLiveData.postValue(newPost)
                        blockState.postValue(!blocked)



                    }
                }
                else{
                    networkError.postValue(true);
                }
                sendingWarnRequest.postValue(false)
            }

            override fun onFailure(call: Call<ApiMessageActionResult>, t: Throwable) {
                sendingWarnRequest.postValue(true)
            }

        })
    }

    fun sendStickRequest(){
        val stick: Boolean = stickState.value!!
        val stickParameter: Int = if(stick){
            0
        } else{
            1
        }
        sendingStickRequest.postValue(true)
        service.stickPost(formHash,fid,tid, listOf(post.pid),stickParameter,reasonMutableLiveData.value!!).enqueue(object :Callback<ApiMessageActionResult>{
            override fun onResponse(
                call: Call<ApiMessageActionResult>,
                response: Response<ApiMessageActionResult>
            ) {
                val res = response.body()
                if(response.isSuccessful && res!=null){
                    returnedMessage.postValue(res.message)
                    if(res.message?.key  == "admin_succeed"){
                        stickState.postValue(!stick)
                    }
                }
                else{
                    networkError.postValue(true);
                }
                sendingStickRequest.postValue(false)
            }

            override fun onFailure(call: Call<ApiMessageActionResult>, t: Throwable) {
                sendingStickRequest.postValue(true)
            }

        })
    }

    fun deleteStickRequest(){
        sendingDeleteRequest.postValue(true)
        service.deletePost(formHash,fid,tid, 1, listOf(post.pid),reasonMutableLiveData.value!!).enqueue(object :Callback<ApiMessageActionResult>{
            override fun onResponse(
                call: Call<ApiMessageActionResult>,
                response: Response<ApiMessageActionResult>
            ) {
                val res = response.body()
                if(response.isSuccessful && res!=null){
                    returnedMessage.postValue(res.message)
                }
                else{
                    networkError.postValue(true);
                }
                sendingDeleteRequest.postValue(false)
            }

            override fun onFailure(call: Call<ApiMessageActionResult>, t: Throwable) {
                sendingDeleteRequest.postValue(true)
            }

        })
    }

}

