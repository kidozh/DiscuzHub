package com.kidozh.discuzhub.services;

import com.kidozh.discuzhub.results.FavoriteThreadActionResult;
import com.kidozh.discuzhub.results.FavoriteThreadResult;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DiscuzApiService {
    public static String DISCUZ_API_PATH = "/api/mobile/index.php";

    @GET(DISCUZ_API_PATH+"?version=4&module=myfavthread")
    Call<FavoriteThreadResult> getFavoriteThreadResult(@Query("page") int page);

    @GET(DISCUZ_API_PATH+"?version=4&module=favthread&type=thread&ac=favorite")
    Call<FavoriteThreadActionResult> favoriteThreadActionResult(
            @Query("formhash") String formhash,
            @Query("id") int tid

    );

    @FormUrlEncoded
    @POST(DISCUZ_API_PATH+"?version=4&module=favthread&type=thread&ac=favorite&op=delete&type=all")
    Call<FavoriteThreadActionResult> unfavoriteThreadActionResult(
            @Field("formhash") String formhash,
            @Query("favid") int favid

    );
}
