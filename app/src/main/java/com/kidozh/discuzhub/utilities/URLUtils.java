package com.kidozh.discuzhub.utilities;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.kidozh.discuzhub.entities.PostInfo;
import com.kidozh.discuzhub.entities.bbsInformation;

public class URLUtils {
    public static String TAG = URLUtils.class.getSimpleName();
    public static String BASE_URL;
    public static String UC_SERVER_URL;
    public static bbsInformation bbsInfo;



    public URLUtils(@NonNull String baseUrl){
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

    public static String getBBSMedalImageURL(String image){
        return BASE_URL+String.format("/static/image/common/%s",image);
    }





    public static class ForumStatus{
        public int fid,page,perPage=10;
        public boolean hasLoadAll = false;
        // orderby:[dateline,replies,views]
        public String orderBy="";
        // filter:
        public String filter="",filterId="";

        public ForumStatus(int fid,int page){
            this.fid = fid;
            this.page = page;
        }



        public void clear(){
            this.page=1;
            this.perPage = 15;
            this.orderBy = "";
            this.filterId = "";
            this.filter = "";
        }

        public void setInitAuthorId(int authorId){
            this.page = 1;
            this.hasLoadAll = false;
        }

        public void setInitPage(int page){
            this.page = page;
            this.hasLoadAll = false;
        }
    }

    public static final String FILTER_TYPE_POLL = "FILTER_TYPE_POLL",
            FILTER_TYPE_NEWEST = "FILTER_TYPE_NEWEST",
            FILTER_TYPE_HEATS = "FILTER_TYPE_HEATS",
            FILTER_TYPE_HOTTEST = "FILTER_TYPE_HOTTEST",
            FILTER_TYPE_DIGEST = "FILTER_TYPE_DIGEST",
            FILTER_TYPE_ID = "FILTER_TYPE_ID";

    public static String getForumUrlByFid(int fid,int page){
        return BASE_URL+String.format("/api/mobile/index.php?version=4&module=forumdisplay&fid=%s&page=%s&ppp=15",fid,page);
    }

    public static String getForumUrlByStatus(ForumStatus status){

        Uri.Builder uriBuilder = Uri.parse(BASE_URL+"/api/mobile/index.php")
                .buildUpon()
                .appendQueryParameter("version","4")
                .appendQueryParameter("module","forumdisplay")
                .appendQueryParameter("fid",String.valueOf(status.fid))
                .appendQueryParameter("page",String.valueOf(status.page))
                .appendQueryParameter("ppp",String.valueOf(status.perPage));
        if(!status.orderBy.equals("")){
            uriBuilder.appendQueryParameter("orderby",status.orderBy);
        }

        if(!status.filter.equals("")){

            uriBuilder.appendQueryParameter("filter",status.filter);
            switch (status.filter){
                case ("specialtype"):{
                    uriBuilder.appendQueryParameter("specialtype","poll");
                    break;
                }
                case ("lastpost"):{
                    uriBuilder.appendQueryParameter("orderby","lastpost");
                    break;
                }
                case ("heat"):{
                    uriBuilder.appendQueryParameter("orderby","heats");
                    break;
                }
                case ("digest"):{
                    uriBuilder.appendQueryParameter("digest","1");
                    break;
                }
            }
        }
        Log.d(TAG,"Type id "+status.filterId);
        if(!status.filterId.equals("")){

            uriBuilder.appendQueryParameter("filter","typeid")
                    .appendQueryParameter("typeid",status.filterId);
        }


        Uri uri = uriBuilder.build();
        return uri.toString();
    }



    private static String getThreadCommentUrlByFid(int tid,int page){
        return BASE_URL+String.format("/api/mobile/index.php?version=4&module=viewthread&tid=%s&page=%s&ppp=15",tid,page);
    }

    public static class ThreadStatus{
        public int tid,page=1,perPage=15;
        public int authorId = -1;
        public boolean hasLoadAll = false;
        // ordertype:1 -> descend 2:-? ascend
        public boolean datelineAscend = true;

        public ThreadStatus(int tid, int page) {
            this.tid = tid;
            this.page = page;
        }

        public void clear(){
            this.page=1;
            this.perPage = 15;
        }

        public void setInitAuthorId(int authorId){
            this.page = 1;
            this.authorId = authorId;
            this.hasLoadAll = false;
        }

        public void setInitPage(int page){
            this.page = page;
            this.hasLoadAll = false;
        }
    }

    public static String getThreadCommentUrlByStatus(ThreadStatus status){

        Uri.Builder uriBuilder = Uri.parse(BASE_URL+"/api/mobile/index.php")
                .buildUpon()
                .appendQueryParameter("version","4")
                .appendQueryParameter("module","viewthread")
                .appendQueryParameter("tid",String.valueOf(status.tid))
                .appendQueryParameter("page",String.valueOf(status.page))
                .appendQueryParameter("ppp",String.valueOf(status.perPage))
                .appendQueryParameter("pollsubmit","1");
        if(status.authorId != -1){
            uriBuilder.appendQueryParameter("authorid",String.valueOf(status.authorId));
        }

        if(status.datelineAscend){
            uriBuilder.appendQueryParameter("ordertype","2");
        }
        else {
            uriBuilder.appendQueryParameter("ordertype","1");
        }

        Uri uri = uriBuilder.build();
        return uri.toString();
    }

    public static String getSmallAvatarUrlByUid(String uid){
        return UC_SERVER_URL+String.format("/avatar.php?uid=%s&size=small",uid);
    }

    public static String getSmallAvatarUrlByUid(int uid){
        return UC_SERVER_URL+String.format("/avatar.php?uid=%s&size=small",uid);
    }

    public static String getDefaultAvatarUrlByUid(String uid){
        return UC_SERVER_URL+String.format("/avatar.php?uid=%s",uid);
    }

    public static String getDefaultAvatarUrlByUid(int uid){
        return UC_SERVER_URL+String.format("/avatar.php?uid=%s",uid);
    }

    public static String getLoginUrl(){
        Uri uri = Uri.parse(BASE_URL+"/api/mobile/index.php").buildUpon()
                .appendQueryParameter("version","4")
                .appendQueryParameter("module","login")
                .appendQueryParameter("action","login")
                .appendQueryParameter("loginsubmit","yes")
                .appendQueryParameter("inajax","1")
                .build();
        return uri.toString();
    }



    public static String getLoginWebUrl(){
        return BASE_URL+"/member.php?mod=logging&action=login";
    }

    public static String getLoginSecondaryUrl(){
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

    public static String getArticleListApiUrl(int fid, int page) {
        return "api/mobile/index.php?version=4&module=forumdisplay&fid=" + fid + "&page=" + page;
    }

    public static String getArticleApiUrl(String tid, int page, int pageSize) {
        return "api/mobile/index.php?version=4&module=viewthread&tid=" + tid + "&page=" + page + "&ppp=" + pageSize;
    }

    public static String getAttachmentImageUrl(String s){
        return BASE_URL +"/data/attachment/forum/"+s;
    }

    public static String getAttachmentWithAlienCode(String alienCode){
        Uri uri = Uri.parse(BASE_URL+"/forum.php").buildUpon()
                .appendQueryParameter("mod","attachment")
                .appendQueryParameter("aid",alienCode)
                .build();
        return uri.toString();
    }

    public static String getAttachmentURL(PostInfo.Attachment attachmentInfo){
        String source;

        if(attachmentInfo.remote){
            // this is remote URL
            source = attachmentInfo.url + attachmentInfo.attachment;
        }
        else {
            // this is local URL
            // Log.d(TAG,"Get attachment aliencode "+attachmentInfo.aidEncode);
            if(attachmentInfo.aidEncode!=null && attachmentInfo.aidEncode.length()!=0) {
                source = URLUtils.getAttachmentWithAlienCode(attachmentInfo.aidEncode);
            }
            else if(attachmentInfo.url !=null && attachmentInfo.attachment !=null){
                source = URLUtils.getBaseUrl()+"/"+ attachmentInfo.url + attachmentInfo.attachment;
            }
            else {
                // have no way give blank
                source = "";
            }

        }
        return source;
    }

    public static String getUploadImageUrl() {
        return BASE_URL + "/misc.php?mod=swfupload&operation=upload&simple=1&type=image";
    }

    public static String getCheckPostUrl(String fid){
        Uri uri = Uri.parse(BASE_URL+"/api/mobile/index.php").buildUpon()
                .appendQueryParameter("version","4")
                .appendQueryParameter("module","checkpost")
                .appendQueryParameter("fid",fid)
                .build();
        return uri.toString();
    }

    public static String getSWFUploadAttachmentUrl(String fid) {
        Uri uri = Uri.parse(BASE_URL + "/misc.php").buildUpon()
                .appendQueryParameter("mod", "swfupload")
                .appendQueryParameter("action", "swfupload")
                .appendQueryParameter("operation", "upload")
                .appendQueryParameter("fid",fid)
                .build();
        return uri.toString();
    }


    public static String getPostThreadUrl(String fid){
        Uri uri = Uri.parse(BASE_URL+"/api/mobile/index.php").buildUpon()
                .appendQueryParameter("version","4")
                .appendQueryParameter("module","newthread")
                .appendQueryParameter("fid",fid)
                .build();
        return uri.toString();
    }

    public static String getReplyThreadUrl(int fid, int tid){
        Uri uri = Uri.parse(BASE_URL+"/api/mobile/index.php").buildUpon()
                .appendQueryParameter("version","4")
                .appendQueryParameter("module","sendreply")
                .appendQueryParameter("fid",String.valueOf(fid))
                .appendQueryParameter("tid",String.valueOf(tid))
                .appendQueryParameter("action","reply")
                //.appendQueryParameter("mod","post")
                // reply submit is neccessary
                .appendQueryParameter("replysubmit","yes")
                //.appendQueryParameter("handlekey","fastpost")
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

    public static String getSeccodeApiUrl(int idhash){
        return BASE_URL + "/api/mobile/index.php?version=4&module=seccodehtml&sechash="+idhash;
    }

    public static String getProfileApiUrlByUid(int uid){
        Uri builtUri = Uri.parse(BASE_URL+"/api/mobile/index.php")
                .buildUpon()
                .appendQueryParameter("version","4")
                .appendQueryParameter("module","profile")
                .appendQueryParameter("uid",String.valueOf(uid))
                .build();
        return builtUri.toString();
    }

    public static String getUserProfileUrl(int uid){
        Uri builtUri = Uri.parse(BASE_URL+"/api/mobile/index.php")
                .buildUpon()
                .appendQueryParameter("version","4")
                .appendQueryParameter("module","profile")
                .appendQueryParameter("uid",String.valueOf(uid))
                .build();
        return builtUri.toString();
    }

    public static String getVotePollApiUrl(int tid){
        Uri builtUri = Uri.parse(BASE_URL+"/api/mobile/index.php")
                .buildUpon()
                .appendQueryParameter("version","4")
                .appendQueryParameter("module","pollvote")
                .appendQueryParameter("tid",String.valueOf(tid))
                .appendQueryParameter("pollsubmit","1").build();
        return builtUri.toString();
    }

    public static String getNoteListApiUrl(String view, String type,int page){
        Uri builtUri = Uri.parse(BASE_URL+"/api/mobile/index.php")
                .buildUpon()
                .appendQueryParameter("version","4")
                .appendQueryParameter("module","mynotelist")
                .appendQueryParameter("view",view)
                .appendQueryParameter("type",type)
                .appendQueryParameter("page",String.valueOf(page))
                .build();
        return builtUri.toString();
    }

    public static String getForumDisplayUrl(String fid,String pageString){

        Uri uri = Uri.parse(BASE_URL+"/forum.php").buildUpon()
                .appendQueryParameter("mod","forumdisplay")
                .appendQueryParameter("fid",fid)
                .appendQueryParameter("page",pageString)
                .build();
        return uri.toString();
    }

    public static String getForumImageUrl(int fid){

        Uri uri = Uri.parse(BASE_URL+"/api/mobile/index.php").buildUpon()
                .appendQueryParameter("version","4")
                .appendQueryParameter("module","forumimage")
                .appendQueryParameter("fid",String.valueOf(fid))
                .build();
        return uri.toString();
    }

    public static String getViewThreadUrl(int tid,String pageString){

        Uri uri = Uri.parse(BASE_URL+"/forum.php").buildUpon()
                .appendQueryParameter("mod","viewthread")
                .appendQueryParameter("tid",String.valueOf(tid))
                .appendQueryParameter("page",pageString)
                .build();
        return uri.toString();
    }

    public static String getPortalPageUrl(){

        Uri uri = Uri.parse(BASE_URL+"/forum.php").buildUpon()
                .build();
        return uri.toString();
    }

    public static String getSecureParameterURL(String type){

        Uri uri = Uri.parse(BASE_URL+"/api/mobile/index.php").buildUpon()
                .appendQueryParameter("version","4")
                .appendQueryParameter("module","secure")
                .appendQueryParameter("type",type)
                .build();
        return uri.toString();
    }

    public static String getSecCodeImageURL(String secHash){

        Uri uri = Uri.parse(BASE_URL+"/api/mobile/index.php").buildUpon()
                .appendQueryParameter("version","4")
                .appendQueryParameter("module","seccode")
                .appendQueryParameter("sechash",secHash)
                .build();
        return uri.toString();
    }





}
