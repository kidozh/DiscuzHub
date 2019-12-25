package com.kidozh.discuzhub.utilities;

import android.content.Intent;
import android.util.Log;

import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.entities.forumCategorySection;
import com.kidozh.discuzhub.entities.forumInfo;
import com.kidozh.discuzhub.entities.threadCommentInfo;
import com.kidozh.discuzhub.entities.threadInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class bbsParseUtils {
    private static String TAG = bbsParseUtils.class.getSimpleName();
    public static bbsInformation parseInformationByJson(String base_url, String jsonString){
        try{
            JSONObject jsonObject = new JSONObject(jsonString);
            String discuzVersion = jsonObject.getString("discuzversion");
            String charset = jsonObject.getString("charset");
            String version = jsonObject.getString("version");
            String pluginVersion = jsonObject.getString("pluginversion");
            String registerName = jsonObject.getString("regname");
            String wsqQQConnect = jsonObject.optString("wsqqqconnect","0");
            String wsqHideregsiter = jsonObject.optString("wsqhideregister","1");
            String siteName = jsonObject.getString("sitename");
            String siteId = jsonObject.getString("mysiteid");
            String uCenterUrl = jsonObject.getString("ucenterurl");
            String defaultFid = jsonObject.optString("defaultfid",null);
            String totalPost = jsonObject.optString("totalposts","0");
            String totalMember = jsonObject.optString("totalmembers","0");
            Boolean qqConnect = wsqQQConnect.equals("1");
            Boolean hideRegister = wsqHideregsiter.equals("1");

            bbsInformation newForumInfo = new bbsInformation(
                    base_url,siteName,discuzVersion,
                    charset,version,pluginVersion,totalPost,
                    totalMember,siteId,defaultFid,uCenterUrl,
                    registerName,"",hideRegister,
                    qqConnect
                    );
            return newForumInfo;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static forumInfo getForumInfoByFid(String s, int fid){
        try{
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray catagoryList = variables.getJSONArray("forumlist");
            for(int i=0;i<catagoryList.length();i++){
                JSONObject catagory = catagoryList.getJSONObject(i);
                String fidString = catagory.getString("fid");
                if (! fidString.equals(String.valueOf(fid))){
                    continue;
                }
                String forumName = catagory.getString("name");
                String allPost = catagory.getString("posts");
                String thread = catagory.getString("threads");
                String todayPosts = catagory.getString("todayposts");
                String iconURLString = catagory.optString("icon","");
                String description = catagory.optString("description","");

                return new forumInfo(forumName,fid,iconURLString,description,todayPosts,allPost,thread);

            }


        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public static List<forumCategorySection> parseCategoryFids(String s){
        try{
            List<forumCategorySection> categorySectionFidList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray catagoryList = variables.getJSONArray("catlist");
            for(int i=0;i<catagoryList.length();i++){
                JSONObject catagory = catagoryList.getJSONObject(i);
                String cataName = catagory.getString("name");
                String fid = catagory.getString("fid");
                JSONArray forumFidList = catagory.getJSONArray("forums");
                List<Integer> fidList = new ArrayList<>();
                for(int j=0;j<forumFidList.length();j++){
                    String fidString = forumFidList.getString(j);
                    fidList.add(Integer.parseInt(fidString));
                }

                forumCategorySection categorySectionFid = new forumCategorySection(cataName,Integer.parseInt(fid),fidList);
                categorySectionFidList.add(categorySectionFid);
            }
            return categorySectionFidList;

        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String parseErrorInformation(String s){
        try{
            JSONObject jsonObject = new JSONObject(s);
            String errorText = jsonObject.getString("error");

            return errorText;

        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    public static Map<String,String> parseThreadType(String s){
        try{
            Map<String,String> threadTypeMap = new HashMap<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONObject threadTypes = variables.getJSONObject("threadtypes");
            JSONObject types = threadTypes.getJSONObject("types");
            Iterator<String> stringIterator = types.keys();
            while (stringIterator.hasNext()){
                String key = stringIterator.next();
                String value = types.getString(key);
                threadTypeMap.put(key,value);
            }
            return threadTypeMap;


        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static List<threadInfo> parseThreadListInfo(String s,Boolean isFirst){
        try{
            List<threadInfo> threadInfoList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray threadList = variables.getJSONArray("forum_threadlist");
            for(int i=0;i<threadList.length();i++){
                JSONObject threadObj = threadList.getJSONObject(i);
                String tid = threadObj.getString("tid");
                String typeid = threadObj.getString("typeid");
                String readPerm = threadObj.getString("readperm");
                String author = threadObj.getString("author");
                String authorId = threadObj.getString("authorid");
                String subject = threadObj.getString("subject");
                String updateAtString = threadObj.getString("lastpost");
                String publishAtStringTimestamp = threadObj.getString("dbdateline");
                String lastUpdator = threadObj.optString("lastposter","");
                String viewNum = threadObj.getString("views");
                String replyNum = threadObj.getString("replies");
                String displayOrder = threadObj.getString("displayorder");
                String digest = threadObj.optString("digest","0");
                String special = threadObj.optString("special","0");
                String recommendNum = threadObj.optString("recommend_add","0");
                String rushReply = threadObj.optString("rushreply","0");
                String price = threadObj.optString("price","0");
                String attachment = threadObj.optString("attachment","0");

                // parse short reply
                List<threadInfo.shortReplyInfo> shortReplyInfoList = new ArrayList<>();
                if(threadObj.has("reply")){
                    JSONArray shortReplies = threadObj.getJSONArray("reply");
                    for(int j = 0;j<shortReplies.length();j++){
                        JSONObject shortReply = shortReplies.getJSONObject(j);
                        shortReplyInfoList.add(new threadInfo.shortReplyInfo(
                                shortReply.getString("pid"),
                                shortReply.getString("author"),
                                shortReply.getString("authorid"),
                                shortReply.getString("message")
                        ));
                    }
                }

                threadInfo thread = new threadInfo();
                thread.tid = tid;
                thread.typeid = typeid;
                thread.readperm = readPerm;
                thread.author = author;
                thread.authorId = authorId;
                thread.subject = subject;
                thread.lastUpdator = lastUpdator;
                thread.viewNum = viewNum;
                thread.repliesNum = replyNum;
                thread.publishAt = new Timestamp(Long.parseLong(publishAtStringTimestamp)*1000);
                thread.lastUpdateTimeString = updateAtString;
                thread.digest = !digest.equals("0");
                thread.special = !special.equals("0");
                thread.rushReply = !rushReply.equals("0");
                thread.recommendNum = Integer.parseInt(recommendNum);
                thread.price = Integer.parseInt(price);
                thread.attachment = Integer.parseInt(attachment);
                thread.shortReplyInfoList = shortReplyInfoList;
                thread.displayOrder = displayOrder;

                if(!displayOrder.equals("0")){
                    thread.isTop = true;
                }
                if(isFirst==false&& thread.isTop == true){
                    continue;
                }
                threadInfoList.add(thread);
            }
            return threadInfoList;

        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String getThreadRuleString(String s){
        try{
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONObject forumInfo = variables.getJSONObject("forum");

            return forumInfo.optString("rules","");

        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String getThreadDescriptionString(String s){
        try{
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONObject forumInfo = variables.getJSONObject("forum");

            return forumInfo.optString("description","");

        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static List<threadCommentInfo.attachmentInfo> getAttachmentInfo(JSONObject jsonObject){
        try{

            List<threadCommentInfo.attachmentInfo> attachmentInfoList = new ArrayList<>();
            Iterator<String> stringIterator = jsonObject.keys();
            while (stringIterator.hasNext()){
                String key = stringIterator.next();
                JSONObject attachmentObj = jsonObject.getJSONObject(key);
                String aid = attachmentObj.getString("aid");
                String tid = attachmentObj.getString("tid");
                String pid = attachmentObj.getString("pid");
                String uid = attachmentObj.getString("uid");
                String filename = attachmentObj.getString("filename");
                if (!(filename.endsWith("png")||filename.endsWith("jpg")||filename.endsWith("gif"))){
                    continue;
                }
                String relativeUrl = attachmentObj.getString("attachment");
                String prefixUrl = attachmentObj.getString("url");
                String publishAtStr = attachmentObj.getString("dbdateline");
                Date publishAt = new Timestamp(Long.parseLong(publishAtStr)*1000);

                attachmentInfoList.add(new threadCommentInfo.attachmentInfo(aid,tid,pid,uid,relativeUrl,filename,publishAt,prefixUrl));
            }
            return attachmentInfoList;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String parseFormHash(String s){
        try{
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            return variables.getString("formhash");
        }
        catch (Exception e){
            return null;
        }
    }

    public static forumUserBriefInfo parseBreifUserInfo(String s){
        try{
            JSONObject jsonObject = new JSONObject(s);
            //Log.d(TAG,"Get ->"+jsonObject.toString());
            //List<bbsNotification> notifications = new ArrayList<>();
            JSONObject variables = jsonObject.getJSONObject("Variables");
            Log.d(TAG,"has auth " + variables.has("auth")+" null auth "+variables.isNull("auth"));
            if(variables.has("auth") && (!variables.isNull("auth"))){
                return new forumUserBriefInfo(
                        variables.getString("auth"),
                        variables.getString("saltkey"),
                        variables.getString("member_uid"),
                        variables.getString("member_username"),
                        variables.getString("member_avatar"),
                        Integer.parseInt(variables.getString("readaccess")),
                        variables.getString("groupid")
                );
            }
            else {

                return null;
            }



        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static Boolean isLoginSuccessful(JSONObject variables){
        try{
            if(variables.has("Message") && variables.getJSONObject("Message").getString("messageval").equals("login_succeed")){
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }

    public static forumUserBriefInfo parseLoginBreifUserInfo(String s){
        try{
            JSONObject jsonObject = new JSONObject(s);
            //Log.d(TAG,"Get ->"+jsonObject.toString());
            //List<bbsNotification> notifications = new ArrayList<>();
            JSONObject variables = jsonObject.getJSONObject("Variables");

            if(isLoginSuccessful(jsonObject) ){
                return new forumUserBriefInfo(
                        variables.getString("auth"),
                        variables.getString("saltkey"),
                        variables.getString("member_uid"),
                        variables.getString("member_username"),
                        variables.getString("member_avatar"),
                        Integer.parseInt(variables.getString("readaccess")),
                        variables.getString("groupid")
                );
            }
            else {
                return null;
            }



        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static List<threadCommentInfo> parseThreadCommentInfo(String s){
        try{
            List<threadCommentInfo> threadInfoList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray threadList = variables.getJSONArray("postlist");
            for(int i=0;i<threadList.length();i++){
                JSONObject threadObj = threadList.getJSONObject(i);
                String tid = threadObj.getString("tid");
                String pid = threadObj.getString("pid");
                String firstString = threadObj.getString("first");
                String author = threadObj.getString("author");
                String authorId = threadObj.getString("authorid");
                String message = threadObj.getString("message");
                String publishAtStringTimestamp = threadObj.getString("dbdateline");
                String lastPostTimeString = threadObj.getString("dateline");
                Date publishAt = new Timestamp(Long.parseLong(publishAtStringTimestamp)*1000);
                threadCommentInfo threadComment = new threadCommentInfo(tid,pid,author,authorId,message,publishAt,lastPostTimeString);
                if(firstString.equals("1")){
                    threadComment.first = true;
                }
                else {
                    threadComment.first = false;
                }
                // attachment
                if(threadObj.has("attachments")){
                    Log.d(TAG,"Find attachment!!!");
                    try{
                        JSONObject attachmentObj = threadObj.getJSONObject("attachments");
                        List<threadCommentInfo.attachmentInfo> attachmentInfoList = getAttachmentInfo(attachmentObj);
                        Log.d(TAG,"get attachmentInfo"+attachmentInfoList.size());
                        threadComment.attachmentInfoList = attachmentInfoList;
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                }

                threadInfoList.add(threadComment);


            }
            return threadInfoList;

        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String parseUploadHashString(String s){
        try{
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONObject allowperm = variables.getJSONObject("allowperm");
            return allowperm.getString("uploadhash");
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> getThreadTypeList(String s){
        try{
            List<String> numKeys = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONObject forumTypeInfo = variables.getJSONObject("threadtypes");
            JSONObject forumTypes = forumTypeInfo.getJSONObject("types");
            Iterator<String> iterator = forumTypes.keys();
            while (iterator.hasNext()){
                String numKey = iterator.next();
                String threadName = forumTypes.getString(numKey);
                numKeys.add(numKey);
            }
            return numKeys;

        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static Boolean isPostThreadSuccessful(String s){
        try{
            JSONObject jsonObject = new JSONObject(s);
            JSONObject message = jsonObject.getJSONObject("Message");
            return message.getString("messageval").equals("post_newthread_succeed");
        }
        catch (Exception e){
            return false;
        }
    }

    public static String parsePostThreadInfo(String s){
        try{
            JSONObject jsonObject = new JSONObject(s);
            JSONObject message = jsonObject.getJSONObject("Message");
            return message.getString("messagestr");
        }
        catch (Exception e){
            return null;
        }
    }

    public static List<threadInfo> parseHotThreadListInfo(String s){
        try{
            List<threadInfo> threadInfoList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray threadList = variables.getJSONArray("data");
            for(int i=0;i<threadList.length();i++){
                JSONObject threadObj = threadList.getJSONObject(i);
                String tid = threadObj.getString("tid");
                String typeid = threadObj.getString("typeid");
                String readPerm = threadObj.getString("readperm");
                String author = threadObj.getString("author");
                String authorId = threadObj.getString("authorid");
                String subject = threadObj.getString("subject");
                String updateAtString = threadObj.getString("lastpost");
                String publishAtStringTimestamp = threadObj.getString("dbdateline");
                String lastUpdator = threadObj.optString("lastposter","");
                String viewNum = threadObj.getString("views");
                String replyNum = threadObj.getString("replies");
                String displayOrder = threadObj.getString("displayorder");
                String digest = threadObj.optString("digest","0");
                String special = threadObj.optString("special","0");
                String recommendNum = threadObj.optString("recommend_add","0");
                String rushReply = threadObj.optString("rushreply","0");
                String price = threadObj.optString("price","0");
                String attachment = threadObj.optString("attachment","0");

                // parse short reply
                List<threadInfo.shortReplyInfo> shortReplyInfoList = new ArrayList<>();
                if(threadObj.has("reply")){
                    JSONArray shortReplies = threadObj.getJSONArray("reply");
                    for(int j = 0;j<shortReplies.length();j++){
                        JSONObject shortReply = shortReplies.getJSONObject(j);
                        shortReplyInfoList.add(new threadInfo.shortReplyInfo(
                                shortReply.getString("pid"),
                                shortReply.getString("author"),
                                shortReply.getString("authorid"),
                                shortReply.getString("message")
                        ));
                    }
                }

                threadInfo thread = new threadInfo();
                thread.tid = tid;
                thread.typeid = typeid;
                thread.readperm = readPerm;
                thread.author = author;
                thread.authorId = authorId;
                thread.subject = subject;
                thread.lastUpdator = lastUpdator;
                thread.viewNum = viewNum;
                thread.repliesNum = replyNum;
                thread.publishAt = new Timestamp(Long.parseLong(publishAtStringTimestamp)*1000);
                thread.lastUpdateTimeString = updateAtString;
                thread.digest = !digest.equals("0");
                thread.special = !special.equals("0");
                thread.rushReply = !rushReply.equals("0");
                thread.recommendNum = Integer.parseInt(recommendNum);
                thread.price = Integer.parseInt(price);
                thread.attachment = Integer.parseInt(attachment);
                thread.shortReplyInfoList = shortReplyInfoList;
                thread.displayOrder = displayOrder;

                if(!displayOrder.equals("0")){
                    thread.isTop = true;
                }
                threadInfoList.add(thread);
            }
            return threadInfoList;

        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static class publicMessage{
        public int id,authorId;
        public Date publishAt;
        public String message;
        public publicMessage(int id, int authorId, Date publishAt, String message){
            this.id = id;
            this.authorId = authorId;
            this.publishAt = publishAt;
            this.message = message;
        }
    }

    public static List<publicMessage> parsePublicMessage(String s){
        try{
            List<publicMessage> publicMessageList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray pmList = variables.getJSONArray("list");
            for(int i=0;i<pmList.length();i++){
                JSONObject pm = (JSONObject) pmList.get(i);
                int id = Integer.parseInt(pm.getString("id"));
                int authorId = Integer.parseInt(pm.getString("authorid"));
                String message = pm.getString("message");
                String publishAtStringTimestamp = pm.getString("dateline");
                Date publishAt = new Timestamp(Long.parseLong(publishAtStringTimestamp)*1000);
                publicMessageList.add(new publicMessage(id,authorId,publishAt,message));
            }
            return publicMessageList;

        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static class privateMessage implements Serializable {
        public int plid;
        public Boolean isNew;
        public String subject;
        public int toUid;
        public int pmid;
        public int msgFromId;
        public String msgFrom;
        public String message;
        public String toUsername;
        public String vdateLine;

        public privateMessage(int plid, Boolean isNew, String subject, int toUid, int pmid, int msgFromId, String msgFrom, String message, String toUsername, String vdateLine) {
            this.plid = plid;
            this.isNew = isNew;
            this.subject = subject;
            this.toUid = toUid;
            this.pmid = pmid;
            this.msgFromId = msgFromId;
            this.msgFrom = msgFrom;
            this.message = message;
            this.toUsername = toUsername;
            this.vdateLine = vdateLine;
        }
    }

    public static List<privateMessage> parsePrivateMessage(String s){
        try{
            List<privateMessage> privateMessageList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray pmList = variables.getJSONArray("list");
            for(int i=0;i<pmList.length();i++){
                JSONObject pm = (JSONObject) pmList.get(i);
                privateMessage privateMessageInstance = new privateMessage(
                        Integer.parseInt(pm.getString("plid")),
                        !pm.getString("isnew").equals("0"),
                        pm.getString("subject"),
                        Integer.parseInt(pm.getString("touid")),
                        Integer.parseInt(pm.getString("pmid")),
                        Integer.parseInt(pm.optString("msgfromid","0")),
                        pm.optString("msgfrom",""),
                        pm.optString("message",""),
                        pm.getString("tousername"),
                        pm.getString("vdateline")
                );
                privateMessageList.add(privateMessageInstance);

            }
            return privateMessageList;

        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static List<threadInfo> parseMyThreadListInfo(String s){
        try{
            List<threadInfo> threadInfoList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray threadList = variables.getJSONArray("data");
            for(int i=0;i<threadList.length();i++){
                JSONObject threadObj = threadList.getJSONObject(i);
                String tid = threadObj.getString("tid");
                String typeid = threadObj.getString("typeid");
                String readPerm = threadObj.getString("readperm");
                String author = threadObj.getString("author");
                String authorId = threadObj.getString("authorid");
                String subject = threadObj.getString("subject");
                String updateAtString = threadObj.getString("lastpost");
                String publishAtStringTimestamp = threadObj.getString("dbdateline");
                String lastUpdator = threadObj.getString("lastposter");
                String viewNum = threadObj.getString("views");
                String replyNum = threadObj.getString("replies");
                String displayOrder = threadObj.getString("displayorder");
                String fid = threadObj.getString("fid");

                threadInfo thread = new threadInfo();
                thread.tid = tid;
                thread.typeid = typeid;
                thread.readperm = readPerm;
                thread.author = author;
                thread.authorId = authorId;
                thread.subject = subject;
                thread.lastUpdator = lastUpdator;
                thread.viewNum = viewNum;
                thread.repliesNum = replyNum;
                thread.publishAt = new Timestamp(Long.parseLong(publishAtStringTimestamp)*1000);
                thread.fid = fid;
                if(!displayOrder.equals("0")){
                    thread.isTop = true;
                }
                threadInfoList.add(thread);
            }
            return threadInfoList;

        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static class privateDetailMessage implements Serializable{
        public int plid;
        public String subject;
        public int pmid;
        public String message;
        public int touid,msgFromId;
        public String msgFrom;
        public String vDateline;
        public Boolean isMyself;


        public privateDetailMessage(int plid, String subject, int pmid, String message, int touid, int msgFromId, String msgFrom, String vDateline, Boolean isMyself) {
            this.plid = plid;
            this.subject = subject;
            this.pmid = pmid;
            this.message = message;
            this.touid = touid;
            this.msgFromId = msgFromId;
            this.msgFrom = msgFrom;
            this.vDateline = vDateline;
            this.isMyself = isMyself;
        }
    }

    public static List<privateDetailMessage> parsePrivateDetailMessage(String s, int recvId){
        try{
            List<privateDetailMessage> privateDetailMessageList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray pmList = variables.getJSONArray("list");
            for(int i=0;i<pmList.length();i++){
                JSONObject pm = (JSONObject) pmList.get(i);
                privateDetailMessage privateDetailMessage = new privateDetailMessage(
                        Integer.parseInt(pm.getString("plid")),
                        pm.getString("subject"),
                        Integer.parseInt(pm.getString("pmid")),
                        pm.optString("message",""),
                        Integer.parseInt(pm.getString("touid")),
                        Integer.parseInt(pm.getString("msgfromid")),
                        pm.getString("msgfrom"),
                        pm.getString("vdateline"),
                        true
                );

                if(privateDetailMessage.msgFromId == recvId){
                    privateDetailMessage.isMyself = true;
                }
                else {
                    privateDetailMessage.isMyself = false;
                }
                if(privateDetailMessage.message.length()!=0){
                    privateDetailMessageList.add(privateDetailMessage);
                }




            }
            return privateDetailMessageList;

        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static int parsePrivateDetailMessagePerPage(String s){
        try{
            List<privateDetailMessage> privateDetailMessageList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");

            return Integer.parseInt(variables.getString("perpage"));

        }
        catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public static String parsePrivateDetailMessagePmid(String s){
        try{
            List<privateDetailMessage> privateDetailMessageList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");

            return variables.getString("pmid");

        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static int parsePrivateDetailMessagePage(String s){
        try{
            List<privateDetailMessage> privateDetailMessageList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");

            return Integer.parseInt(variables.getString("page"));

        }
        catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    public static Map<String,String> parseUserProfile(String s){

        try{
            Map<String,String> info = new HashMap<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");

            JSONObject spaceInfo = variables.getJSONObject("space");
            Iterator<String> sIterator = spaceInfo.keys();
            while(sIterator.hasNext()){
                String key = sIterator.next();
                if(spaceInfo.get(key) instanceof String){
                    String val = spaceInfo.getString(key);
                    info.put(key,val);
                }
            }
            return info;

        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static class userFriend{
        public int uid;
        public String username;

        public userFriend(int uid, String username) {
            this.uid = uid;
            this.username = username;
        }
    }

    public static List<userFriend> parseUserFriendInfo(String s){
        try{
            List<userFriend> userFriendList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray pmList = variables.getJSONArray("list");
            for(int i=0;i<pmList.length();i++){
                JSONObject pm = (JSONObject) pmList.get(i);
                userFriend userFriend = new userFriend(
                        Integer.parseInt(pm.getString("uid")),
                        pm.getString("username")
                );
                userFriendList.add(userFriend);
            }
            return userFriendList;

        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

}
