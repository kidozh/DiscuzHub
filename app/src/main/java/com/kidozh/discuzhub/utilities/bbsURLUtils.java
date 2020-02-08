package com.kidozh.discuzhub.utilities;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumInfo;

public class bbsURLUtils {
    public static String TAG = bbsURLUtils.class.getSimpleName();
    public static String BASE_URL;
    public static String UC_SERVER_URL;
    public static bbsInformation bbsInfo;



    public bbsURLUtils(@NonNull String baseUrl){
        BASE_URL = baseUrl;
    }

    public static void setBaseUrl(String baseUrl){
        BASE_URL = baseUrl;
    }
    public static String getBaseUrl(){
        return BASE_URL;
    }
    public static void setBBS(bbsInformation bbs){
        BASE_URL = bbs.base_url;
        UC_SERVER_URL = bbs.ucenter_url;
        bbsInfo = bbs;
    }

    public static String getBBSForumInformationUrl(){
        return BASE_URL + "/api/mobile/index.php?version=4&module=check";
    }

    public static String getBBSRegisterUrl(String registerName){
        return BASE_URL + "/member.php?mod="+registerName;
    }

    public static String getBBSRegisterUrl(){
        return BASE_URL + "/member.php?mod="+bbsInfo.register_name;
    }

    public static String getBBSLogoUrl(){
        return BASE_URL + "/static/image/common/logo.png";
    }



    public static String getBBSForumInfoApi(){
        return BASE_URL + "/api/mobile/index.php?version=4&module=forumindex";
    }

    public static String getBBSForumIconURLByFid(int fid){
        return BASE_URL+String.format("/data/attachment/common/1f/common_%s_icon.png",fid);
    }

    public static String getForumUrlByFid(int fid,int page){
        return BASE_URL+String.format("/api/mobile/index.php?version=4&module=forumdisplay&fid=%s&page=%s&ppp=15",fid,page);
    }

    public static String getThreadCommentUrlByFid(int tid,int page){
        return BASE_URL+String.format("/api/mobile/index.php?version=4&module=viewthread&tid=%s&page=%s&ppp=15",tid,page);
    }

    public static String getSmallAvatarUrlByUid(String uid){
        return UC_SERVER_URL+String.format("/avatar.php?uid=%s&size=small",uid);
    }

    public static String getDefaultAvatarUrlByUid(String uid){
        return UC_SERVER_URL+String.format("/avatar.php?uid=%s",uid);
    }

    public static String getLoginUrl(){
        return BASE_URL + "/member.php?mod=logging&action=login&loginsubmit=yes&infloat=yes&lssubmit=yes&inajax=1";
        //return "https://bbs.nwpu.edu.cn/member.php?mod=logging&action=login&loginsubmit=yes&infloat=yes&lssubmit=yes&inajax=1";
    }

    public static String getLoginWebUrl(){
        return BASE_URL+"/member.php?mod=logging&action=login";
    }

    public static String getLoginApiUrl(){
        return BASE_URL + "/api/mobile/index.php?version=4&module=login&mod=logging&action=login";
    }

    public static String getUserProfileUrl(){
        return BASE_URL + "/api/mobile/index.php?version=4&module=profile";
    }

    // login

    public static String getUserThreadUrl(int page){
        return BASE_URL + String.format("/api/mobile/index.php?version=4&module=mythread&page=%s",page);
    }

    public static String getNewThreadUrl(int fid){
        return BASE_URL + String.format("/api/mobile/index.php?module=newthread&fid=%s&mobile=2&version=4",fid);
    }



    public static String getPostsUrl(int fid, int page, boolean isInner) {
        if (isInner) {
            return "forum.php?mod=forumdisplay&fid=" + fid + "&page=" + page;
        } else {
            return "forum.php?mod=forumdisplay&fid=" + fid + "&page=" + page + "&mobile=2";
        }
    }

    public static String getArticleListApiUrl(int fid, int page) {
        return "api/mobile/index.php?version=4&module=forumdisplay&fid=" + fid + "&page=" + page;
    }

    public static String getArticleApiUrl(String tid, int page, int pageSize) {
        return "api/mobile/index.php?version=4&module=viewthread&tid=" + tid + "&page=" + page + "&ppp=" + pageSize;
    }

    public static String getAttachmentImageUrl(String s){
        return BASE_URL +"/data/attachment/forum/"+s;
    }

    public static String getUploadImageUrl() {
        return BASE_URL + "/misc.php?mod=swfupload&operation=upload&simple=1&type=image";
    }

    public static String getCheckPostUrl(){
        return BASE_URL + "/api/mobile/index.php?version=4&module=checkpost";
    }



    public static String getPostThreadUrl(String fid){
        return BASE_URL+"/api/mobile/index.php?version=4&module=newthread&fid=" + fid ;
    }

    public static String ajaxGetReplyPostParametersUrl(String tid, String pid){
        return BASE_URL+String.format("/forum.php?mod=post&action=reply&tid=%s&repquote=%s&extra=&page=1&infloat=yes&handlekey=reply&inajax=1&ajaxtarget=fwin_content_reply",tid,pid);
    }

    public static String getReplyThreadUrl(String fid,String tid){
        Uri uri = Uri.parse(BASE_URL+"/forum.php").buildUpon()
                .appendQueryParameter("mod","post")
                .appendQueryParameter("action","reply")
                .appendQueryParameter("fid",fid)
                .appendQueryParameter("tid",tid)
                .appendQueryParameter("inajax","1")
                .appendQueryParameter("replysubmit","yes")
                .appendQueryParameter("infloat","yes")
                .appendQueryParameter("handlekey","fastpost")
                //.appendQueryParameter("extra","")
                .build();
        return uri.toString();
    }

    public static String getReplyToSomeoneThreadUrl(String fid,String tid){
        Uri uri = Uri.parse(BASE_URL+"/forum.php").buildUpon()
                .appendQueryParameter("mod","post")
                .appendQueryParameter("action","reply")
                .appendQueryParameter("fid",fid)
                .appendQueryParameter("tid",tid)
                .appendQueryParameter("inajax","1")
                .appendQueryParameter("replysubmit","yes")
                .appendQueryParameter("infloat","yes")
                //.appendQueryParameter("extra","")
                .build();
        return uri.toString();
    }



    public static String getHotThreadUrl(int page){
        return BASE_URL+"/api/mobile/index.php?version=4&module=hotthread&page="+page ;
    }

    public static String getPublicPMApiUrl(int page){
        return BASE_URL+"/api/mobile/index.php?version=4&module=publicpm&page="+page ;
    }

    public static String getPrivatePMApiUrl(int page){
        return BASE_URL+"/api/mobile/index.php?version=4&module=mypm&page="+page ;
    }

    public static String getPrivatePMDetailApiUrlByTouid(int toUid,int page){
        if(page==-1){
            return BASE_URL + "/api/mobile/index.php?version=4&module=mypm&subop=view&touid="+toUid;
        }
        else {
            return BASE_URL + "/api/mobile/index.php?version=4&module=mypm&subop=view&touid="+toUid+"&page="+page;
        }

    }

    public static String getSendPMApiUrl(int plid,int pmid){
        return BASE_URL+"/api/mobile/index.php?version=4&ac=pm&op=send&daterange=0&module=sendpm&plid="+plid+"&pmid="+pmid+"&handlekey=pmsend&pmsubmit=yes&inajax=1";
    }

    public static String getProfileApiUrlByUid(int uid){
        return BASE_URL+"/api/mobile/index.php?version=4&module=profile&uid="+uid;
    }

    public static String getFriendApiUrlByUid(int uid,int page){
        return BASE_URL+"/api/mobile/index.php?version=4&module=friend&uid="+uid+"&page="+page;
    }

    public static String getNotificationListApiUrl(int page){
        return BASE_URL+"/api/mobile/index.php?version=4&module=mynotelist&page="+page;
    }

    public static String getSmileyApiUrl() {
        return BASE_URL + "/api/mobile/index.php?version=4&module=smiley";
    }

    public static String getSmileyImageUrl(String path) {
        return BASE_URL + "/static/image/smiley/"+path;
    }

    public static String getPromptNotificationListApiUrl(int page){
        return BASE_URL + "/api/mobile/index.php?version=4&module=mynotelist&view=system&page="+page;
    }

    public static String getSeccodeApiUrl(int idhash){
        return BASE_URL + "http://192.168.0.119/api/mobile/index.php?version=4&module=seccodehtml&sechash="+idhash;
    }

}
