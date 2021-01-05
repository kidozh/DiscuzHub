package com.kidozh.discuzhub.services;


import com.kidozh.discuzhub.results.PushCheckResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DHPushServices {
    @GET("/plugin.php?id=dhpush:check")
    Call<PushCheckResult> getPushInformation();
}
