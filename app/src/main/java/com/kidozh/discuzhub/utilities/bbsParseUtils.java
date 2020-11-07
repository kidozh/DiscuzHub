package com.kidozh.discuzhub.utilities;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kidozh.discuzhub.entities.FavoriteThread;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.bbsPollInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.entities.threadCommentInfo;
import com.kidozh.discuzhub.results.AddCheckResult;
import com.kidozh.discuzhub.results.BBSIndexResult;
import com.kidozh.discuzhub.results.ForumResult;
import com.kidozh.discuzhub.results.DisplayThreadsResult;
import com.kidozh.discuzhub.results.FavoriteThreadResult;
import com.kidozh.discuzhub.results.HotForumsResult;
import com.kidozh.discuzhub.results.SecureInfoResult;
import com.kidozh.discuzhub.results.PostParameterResult;
import com.kidozh.discuzhub.results.ThreadResult;
import com.kidozh.discuzhub.results.UserFriendResult;
import com.kidozh.discuzhub.results.UserNoteListResult;
import com.kidozh.discuzhub.results.UserProfileResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
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

    public static AddCheckResult parseCheckInfoResult(String s) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(s, AddCheckResult.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, String> parseThreadType(String s) {
        try {
            Map<String, String> threadTypeMap = new HashMap<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONObject threadTypes = variables.getJSONObject("threadtypes");
            JSONObject types = threadTypes.getJSONObject("types");
            Iterator<String> stringIterator = types.keys();
            while (stringIterator.hasNext()) {
                String key = stringIterator.next();
                String value = types.getString(key);
                threadTypeMap.put(key, value);
            }
            return threadTypeMap;


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String parseFormHash(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            return variables.getString("formhash");
        } catch (Exception e) {
            return null;
        }
    }

    public static forumUserBriefInfo parseBreifUserInfo(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            //Log.d(TAG,"Get ->"+jsonObject.toString());
            //List<bbsNotification> notifications = new ArrayList<>();
            JSONObject variables = jsonObject.getJSONObject("Variables");
            Log.d(TAG, "has auth " + variables.has("auth") + " auth " + variables.isNull("auth"));
            if (variables.has("auth") && (!variables.isNull("auth"))) {
                return new forumUserBriefInfo(
                        variables.getString("auth"),
                        variables.getString("saltkey"),
                        variables.getString("member_uid"),
                        variables.getString("member_username"),
                        variables.getString("member_avatar"),
                        Integer.parseInt(variables.getString("readaccess")),
                        variables.getString("groupid")
                );
            } else {

                return null;
            }


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Boolean isLoginSuccessful(JSONObject variables) {
        try {
            if (variables.has("Message") && variables.getJSONObject("Message").getString("messageval").equals("login_succeed")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public static UserProfileResult parseUserProfileResult(String s){
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(s, UserProfileResult.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> getThreadTypeList(String s) {
        try {
            List<String> numKeys = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONObject forumTypeInfo = variables.getJSONObject("threadtypes");
            JSONObject forumTypes = forumTypeInfo.getJSONObject("types");
            Iterator<String> iterator = forumTypes.keys();
            while (iterator.hasNext()) {
                String numKey = iterator.next();
                String threadName = forumTypes.getString(numKey);
                numKeys.add(numKey);
            }
            return numKeys;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Boolean isPostThreadSuccessful(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONObject message = jsonObject.getJSONObject("Message");
            return message.getString("messageval").equals("post_newthread_succeed");
        } catch (Exception e) {
            return false;
        }
    }

    public static Boolean isPostReplySuccessful(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONObject message = jsonObject.getJSONObject("Message");
            return message.getString("messageval").equals("post_reply_succeed");
        } catch (Exception e) {
            return false;
        }
    }

    public static String parsePostThreadInfo(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONObject message = jsonObject.getJSONObject("Message");
            return message.getString("messagestr");
        } catch (Exception e) {
            return null;
        }
    }

    public static DisplayThreadsResult getThreadListInfo(String s) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(s, DisplayThreadsResult.class);


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class publicMessage {
        public int id, authorId;
        public Date publishAt;
        public String message;

        public publicMessage(int id, int authorId, Date publishAt, String message) {
            this.id = id;
            this.authorId = authorId;
            this.publishAt = publishAt;
            this.message = message;
        }
    }

    public static List<publicMessage> parsePublicMessage(String s) {
        try {
            List<publicMessage> publicMessageList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray pmList = variables.getJSONArray("list");
            for (int i = 0; i < pmList.length(); i++) {
                JSONObject pm = (JSONObject) pmList.get(i);
                int id = Integer.parseInt(pm.getString("id"));
                int authorId = Integer.parseInt(pm.getString("authorid"));
                String message = pm.getString("message");
                String publishAtStringTimestamp = pm.getString("dateline");
                Date publishAt = new Timestamp(Long.parseLong(publishAtStringTimestamp) * 1000);
                publicMessageList.add(new publicMessage(id, authorId, publishAt, message));
            }
            return publicMessageList;

        } catch (Exception e) {
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

    public static List<privateMessage> parsePrivateMessage(String s) {
        try {
            List<privateMessage> privateMessageList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray pmList = variables.getJSONArray("list");
            for (int i = 0; i < pmList.length(); i++) {
                JSONObject pm = (JSONObject) pmList.get(i);
                privateMessage privateMessageInstance = new privateMessage(
                        Integer.parseInt(pm.getString("plid")),
                        !pm.getString("isnew").equals("0"),
                        pm.getString("subject"),
                        Integer.parseInt(pm.getString("touid")),
                        Integer.parseInt(pm.getString("pmid")),
                        Integer.parseInt(pm.optString("msgfromid", "0")),
                        pm.optString("msgfrom", ""),
                        pm.optString("message", ""),
                        pm.getString("tousername"),
                        pm.getString("vdateline")
                );
                privateMessageList.add(privateMessageInstance);

            }
            return privateMessageList;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class privateDetailMessage implements Serializable {
        public int plid;
        public String subject;
        public int pmid;
        public String message;
        public int touid, msgFromId;
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

    public static List<privateDetailMessage> parsePrivateDetailMessage(String s, int recvId) {
        try {
            List<privateDetailMessage> privateDetailMessageList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray pmList = variables.getJSONArray("list");
            for (int i = 0; i < pmList.length(); i++) {
                JSONObject pm = (JSONObject) pmList.get(i);
                privateDetailMessage privateDetailMessage = new privateDetailMessage(
                        Integer.parseInt(pm.getString("plid")),
                        pm.getString("subject"),
                        Integer.parseInt(pm.getString("pmid")),
                        pm.optString("message", ""),
                        Integer.parseInt(pm.getString("touid")),
                        Integer.parseInt(pm.getString("msgfromid")),
                        pm.getString("msgfrom"),
                        pm.getString("vdateline"),
                        true
                );

                if (privateDetailMessage.msgFromId == recvId) {
                    privateDetailMessage.isMyself = true;
                } else {
                    privateDetailMessage.isMyself = false;
                }
                if (privateDetailMessage.message.length() != 0) {
                    privateDetailMessageList.add(privateDetailMessage);
                }


            }
            return privateDetailMessageList;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int parsePrivateDetailMessagePerPage(String s) {
        try {
            List<privateDetailMessage> privateDetailMessageList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");

            return Integer.parseInt(variables.getString("perpage"));

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String parsePrivateDetailMessagePmid(String s) {
        try {
            List<privateDetailMessage> privateDetailMessageList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");

            return variables.getString("pmid");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int parsePrivateDetailMessagePage(String s) {
        try {
            List<privateDetailMessage> privateDetailMessageList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");

            return Integer.parseInt(variables.getString("page"));

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }




    public static class userFriend {
        public int uid;
        public String username;

        public userFriend(int uid, String username) {
            this.uid = uid;
            this.username = username;
        }
    }


    public static UserFriendResult parseUserFriendsResult(String s) {
        try {

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(s, UserFriendResult.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class noticeNumInfo {
        public int push, pm, prompt, mypost;

        public noticeNumInfo(String push, String pm, String prompt, String mypost) {
            this.push = Integer.parseInt(push);
            this.pm = Integer.parseInt(pm);
            this.prompt = Integer.parseInt(prompt);
            this.mypost = Integer.parseInt(mypost);
        }

        public int getAllNoticeInfo() {
            return push + pm + prompt + mypost;
        }
    }

    public static noticeNumInfo parseNoticeInfo(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONObject notice = variables.getJSONObject("notice");
            String newpush = notice.getString("newpush");
            String newpm = notice.getString("newpm");
            String newprompt = notice.getString("newprompt");
            String newmypost = notice.getString("newmypost");
            return new noticeNumInfo(newpush, newpm, newprompt, newmypost);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class notificationDetailInfo {
        public int id, uid;
        public String type;
        public Boolean isNew;
        public int authorId;
        public String author, note;
        public Date date;
        public int fromId;
        public String fromIdType;
        public int from_num;
        public Map<String, String> noteVariables;

        public notificationDetailInfo(int id, int uid, String type, Boolean isNew, int authorId, String author, String note, Date date, int fromId, String fromIdType, int from_num, Map<String, String> noteVariables) {
            this.id = id;
            this.uid = uid;
            this.type = type;
            this.isNew = isNew;
            this.authorId = authorId;
            this.author = author;
            this.note = note;
            this.date = date;
            this.fromId = fromId;
            this.fromIdType = fromIdType;
            this.from_num = from_num;
            this.noteVariables = noteVariables;
        }
    }

    public static UserNoteListResult getUserNoteListResult(String s) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(s, UserNoteListResult.class);


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, String> parseNotificationVariable(JSONObject noteVar) {

        try {
            Map<String, String> info = new HashMap<>();

            JSONObject spaceInfo = noteVar.getJSONObject("notevar");
            Iterator<String> sIterator = spaceInfo.keys();
            while (sIterator.hasNext()) {
                String key = sIterator.next();
                if (spaceInfo.get(key) instanceof String) {
                    String val = spaceInfo.getString(key);
                    info.put(key, val);
                }
            }
            return info;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class smileyInfo implements Parcelable {
        public String code, imageRelativePath;
        public int category;

        public smileyInfo(String code, String imageRelativePath, int category) {
            this.code = code;
            this.imageRelativePath = imageRelativePath;
            this.category = category;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(code);
            dest.writeString(imageRelativePath);
            dest.writeInt(category);
        }

        public static final Creator<smileyInfo> CREATOR = new Creator<smileyInfo>() {
            @Override
            public smileyInfo createFromParcel(Parcel source) {
                String code = source.readString();
                String imageRelativePath = source.readString();
                int category = source.readInt();

                return new smileyInfo(code, imageRelativePath, category);
            }

            @Override
            public smileyInfo[] newArray(int size) {
                return new smileyInfo[size];
            }
        };
    }

    public static List<smileyInfo> parseSmileyInfo(String s) {
        try {
            List<smileyInfo> smileyInfoList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray smileyCateList = variables.getJSONArray("smilies");
            for (int i = 0; i < smileyCateList.length(); i++) {
                JSONArray smileyCates = smileyCateList.getJSONArray(i);
                for (int j = 0; j < smileyCates.length(); j++) {
                    JSONObject smiley = smileyCates.getJSONObject(j);
                    smileyInfoList.add(new smileyInfo(
                            smiley.getString("code"),
                            smiley.getString("image"),
                            i
                    ));
                }
            }
            return smileyInfoList;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int parseSmileyCateNum(String s) {
        try {
            List<smileyInfo> smileyInfoList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray smileyCateList = variables.getJSONArray("smilies");
            return smileyCateList.length();


        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static class returnMessage {
        @JsonProperty("messageval")
        public String value;
        @JsonProperty("messagestr")
        public String string;
    }

    public static returnMessage parseReturnMessage(String s) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            JSONObject jsonObject = new JSONObject(s);
            JSONObject messageObj = jsonObject.getJSONObject("Message");
            return mapper.readValue(messageObj.toString(), returnMessage.class);


        } catch (Exception e) {
            // e.printStackTrace();
            return null;
        }
    }

    public static class OneZeroDeserializer extends JsonDeserializer<Boolean> {

        @Override
        public Boolean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonToken currentToken = p.getCurrentToken();
            if (currentToken.equals(JsonToken.VALUE_STRING)) {
                String text = p.getText();
                if (text.equals("0") || text.equals("")) {
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }

            } else if (currentToken.equals(JsonToken.VALUE_TRUE)) {
                return Boolean.TRUE;
            } else if (currentToken.equals(JsonToken.VALUE_FALSE)) {
                return Boolean.FALSE;
            } else if (currentToken.equals(JsonToken.VALUE_NULL)) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class DetailedThreadInfo {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int tid;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int fid;
        @JsonProperty("posttableid")
        public String postableId= "";
        @JsonProperty("typeid")
        public String typeId= "";
        public String author= "", subject= "";
        @JsonProperty("authorid")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int authorId;
        @JsonProperty("sortid")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int sortId;
        @JsonProperty("dateline")
        @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="s")
        public Date lastPostTime = new Date();
        @JsonProperty("lastpost")
        public String lastPostTimeString;
        public String lastposter= "";
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("displayorder")
        public int displayOrder;
        @JsonFormat(shape=JsonFormat.Shape.STRING)
        public int views, replies;
        public String highlight;
        @JsonDeserialize(using = OneZeroDeserializer.class)
        @JsonFormat(shape=JsonFormat.Shape.STRING)
        public Boolean special=false, moderated=false, is_archived=false;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int rate, status, readperm, price, digest, closed;


        public String attachment;
        @JsonDeserialize(using = OneZeroDeserializer.class)
        @JsonProperty("stickreply")
        public boolean stickReply;
        @JsonFormat(shape=JsonFormat.Shape.STRING)
        public int recommends, recommend_add, recommend_sub;
        public String isgroup;
        @JsonFormat(shape=JsonFormat.Shape.STRING)
        public int favtimes = 0, sharedtimes = 0, heats = 0;
        public String stamp, icon, pushedaid, cover;
        @JsonProperty("replycredit")
        public String replyCredit;
        public String relatebytag, bgcolor;
        @JsonProperty("maxposition")
        @JsonFormat(shape=JsonFormat.Shape.STRING)
        public long maxPosition;
        @JsonProperty("comments")
        @JsonFormat(shape= JsonFormat.Shape.STRING)
        public int comments;
        @JsonFormat(shape= JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroDeserializer.class)
        public boolean hidden;
        public String threadtable, threadtableid, posttable;
        @JsonProperty("allreplies")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int allreplies;
        public String archiveid;
        public String subjectenc, short_subject;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int relay, ordertype, recommend;
        @JsonProperty("recommendlevel")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int recommendLevel;
        @JsonProperty("heatlevel")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int heatLevel;
        @JsonProperty("freemessage")
        public String freeMessage;
        @JsonProperty("replycredit_rule")
        public replyCreditRule creditRule;

        public FavoriteThread toFavoriteThread(int bbsId, int userId){
            FavoriteThread favoriteThread = new FavoriteThread();
            favoriteThread.belongedBBSId = bbsId;
            favoriteThread.uid = this.authorId;
            favoriteThread.idKey = this.tid;
            favoriteThread.idType = "tid";
            favoriteThread.title = this.subject;
            favoriteThread.description = this.freeMessage == null ?"":this.freeMessage;
            favoriteThread.author = this.author;
            favoriteThread.date = this.lastPostTime;
            favoriteThread.replies = this.replies;
            favoriteThread.userId = userId;
            return favoriteThread;
        }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class replyCreditRule{
        public String tid;
        @JsonProperty("extcredits")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int extCredits;
        @JsonProperty("extcreditstype")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int extCreditsType;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int times, membertimes;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int random;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int remaining;
    }

    public static PostParameterResult parseThreadPostParameter(String s) {
        try {

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(s, PostParameterResult.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static SecureInfoResult parseSecureInfoResult(String s) {
        try {

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(s, SecureInfoResult.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static FavoriteThreadResult getFavoriteThreadResult(String s) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(s, FavoriteThreadResult.class);


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
