package com.kidozh.discuzhub.services;

import com.kidozh.discuzhub.results.BBSIndexResult;
import com.kidozh.discuzhub.results.BaseResult;
import com.kidozh.discuzhub.results.DisplayThreadsResult;
import com.kidozh.discuzhub.results.FavoriteForumResult;
import com.kidozh.discuzhub.results.ApiMessageActionResult;
import com.kidozh.discuzhub.results.FavoriteThreadResult;
import com.kidozh.discuzhub.results.ForumResult;
import com.kidozh.discuzhub.results.HotForumsResult;
import com.kidozh.discuzhub.results.LoginResult;
import com.kidozh.discuzhub.results.SecureInfoResult;
import com.kidozh.discuzhub.results.ThreadResult;
import com.kidozh.discuzhub.results.UserNoteListResult;
import com.kidozh.discuzhub.results.VariableResults;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface DiscuzApiService {
    public static String DISCUZ_API_PATH = "/api/mobile/index.php";

    @GET(DISCUZ_API_PATH+"?version=4&module=myfavthread")
    Call<FavoriteThreadResult> getFavoriteThreadResult(@Query("page") int page);

    @GET(DISCUZ_API_PATH+"?version=4&module=myfavforum")
    Call<FavoriteForumResult> getFavoriteForumResult(@Query("page") int page);

    @FormUrlEncoded
    @POST(DISCUZ_API_PATH+"?version=4&module=favthread&type=thread&ac=favorite")
    Call<ApiMessageActionResult> favoriteThreadActionResult(
            @Query("formhash") String formhash,
            @Query("id") int tid,
            @Field("description") String description


    );

    @FormUrlEncoded
    @POST(DISCUZ_API_PATH+"?version=4&module=favthread&type=all&ac=favorite&op=delete&inajax=1")
    Call<ApiMessageActionResult> unfavoriteThreadActionResult(
            @Field("formhash") String formhash,
            @Field("deletesubmit") String submit,
            @Field("handlekey") String handleKey,
            @Query("favid") int favid


    );

    @FormUrlEncoded
    @POST(DISCUZ_API_PATH+"?version=4&module=favforum&type=forum&ac=favorite")
    Call<ApiMessageActionResult> favoriteForumActionResult(
            @Query("formhash") String formhash,
            @Query("id") int tid,
            @Field("description") String description


    );

    @FormUrlEncoded
    @POST(DISCUZ_API_PATH+"?version=4&module=favforum&type=all&ac=favorite&op=delete&inajax=1")
    Call<ApiMessageActionResult> unfavoriteForumActionResult(
            @Field("formhash") String formhash,
            @Field("deletesubmit") String submit,
            @Field("handlekey") String handleKey,
            @Query("favid") int favid


    );


    @GET(DISCUZ_API_PATH+"?version=4&module=mobilesign")
    Call<ApiMessageActionResult> mobileSignActionResult(
            @Query("hash") String formHash
    );

    @GET(DISCUZ_API_PATH+"?version=4&module=mynotelist")
    Call<UserNoteListResult> userNotificationListResult(
            @Query("page") int page
    );

    @GET(DISCUZ_API_PATH+"?version=4&module=login&mod=logging&action=login")
    Call<LoginResult> getLoginResult();

    @GET(DISCUZ_API_PATH+"?version=4&module=hotforum")
    Call<HotForumsResult> hotForumResult();

    @GET(DISCUZ_API_PATH+"?version=4&module=hotthread")
    Call<DisplayThreadsResult> hotThreadResult(
            @Query("page") int page
    );

    @GET(DISCUZ_API_PATH+"?version=4&module=forumindex")
    Call<BBSIndexResult> indexResult();

    @GET(DISCUZ_API_PATH+"?version=4&module=forumdisplay")
    Call<ForumResult> forumDisplayResult(@QueryMap HashMap<String,String> options);

    @GET(DISCUZ_API_PATH+"?version=4&module=viewthread")
    Call<ThreadResult> viewThreadResult(@QueryMap HashMap<String,String> options);

    @GET(DISCUZ_API_PATH+"?version=4&module=secure")
    Call<SecureInfoResult> secureResult(@Query("type") String type);
}
