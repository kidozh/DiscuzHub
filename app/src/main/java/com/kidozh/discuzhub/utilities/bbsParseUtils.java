package com.kidozh.discuzhub.utilities;

import android.content.Intent;
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
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.bbsPollInfo;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.entities.forumCategorySection;
import com.kidozh.discuzhub.entities.forumInfo;
import com.kidozh.discuzhub.entities.threadCommentInfo;
import com.kidozh.discuzhub.entities.threadInfo;
import com.kidozh.discuzhub.results.ThreadPostParameterResult;

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

    public static bbsInformation parseInformationByJson(String base_url, String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            String discuzVersion = jsonObject.getString("discuzversion");
            String charset = jsonObject.getString("charset");
            String version = jsonObject.getString("version");
            String pluginVersion = jsonObject.getString("pluginversion");
            String registerName = jsonObject.getString("regname");
            String wsqQQConnect = jsonObject.optString("wsqqqconnect", "0");
            String wsqHideregsiter = jsonObject.optString("wsqhideregister", "1");
            String siteName = jsonObject.getString("sitename");
            String siteId = jsonObject.getString("mysiteid");
            String uCenterUrl = jsonObject.getString("ucenterurl");
            String defaultFid = jsonObject.optString("defaultfid", null);
            String totalPost = jsonObject.optString("totalposts", "0");
            String totalMember = jsonObject.optString("totalmembers", "0");
            Boolean qqConnect = wsqQQConnect.equals("1");
            Boolean hideRegister = wsqHideregsiter.equals("1");

            bbsInformation newForumInfo = new bbsInformation(
                    base_url, siteName, discuzVersion,
                    charset, version, pluginVersion, totalPost,
                    totalMember, siteId, defaultFid, uCenterUrl,
                    registerName, "", hideRegister,
                    qqConnect
            );
            return newForumInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static forumInfo getForumInfoByFid(String s, int fid) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray catagoryList = variables.getJSONArray("forumlist");
            for (int i = 0; i < catagoryList.length(); i++) {
                JSONObject catagory = catagoryList.getJSONObject(i);
                String fidString = catagory.getString("fid");
                if (!fidString.equals(String.valueOf(fid))) {
                    continue;
                }
                String forumName = catagory.getString("name");
                String allPost = catagory.getString("posts");
                String thread = catagory.getString("threads");
                String todayPosts = catagory.getString("todayposts");
                String iconURLString = catagory.optString("icon", "");
                String description = catagory.optString("description", "");

                return new forumInfo(forumName, fid, iconURLString, description, todayPosts, allPost, thread);

            }


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public static List<forumCategorySection> parseCategoryFids(String s) {
        try {
            List<forumCategorySection> categorySectionFidList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray catagoryList = variables.getJSONArray("catlist");
            for (int i = 0; i < catagoryList.length(); i++) {
                JSONObject catagory = catagoryList.getJSONObject(i);
                String cataName = catagory.getString("name");
                String fid = catagory.getString("fid");
                JSONArray forumFidList = catagory.getJSONArray("forums");
                List<Integer> fidList = new ArrayList<>();
                for (int j = 0; j < forumFidList.length(); j++) {
                    String fidString = forumFidList.getString(j);
                    fidList.add(Integer.parseInt(fidString));
                }

                forumCategorySection categorySectionFid = new forumCategorySection(cataName, Integer.parseInt(fid), fidList);
                categorySectionFidList.add(categorySectionFid);
            }
            return categorySectionFidList;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String parseErrorInformation(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            String errorText = jsonObject.getString("error");

            return errorText;

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

    public static List<threadInfo> parseThreadListInfo(String s, Boolean isFirst) {
        try {
            List<threadInfo> threadInfoList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray threadList = variables.getJSONArray("forum_threadlist");
            for (int i = 0; i < threadList.length(); i++) {
                JSONObject threadObj = threadList.getJSONObject(i);
                String tid = threadObj.getString("tid");
                String typeid = threadObj.getString("typeid");
                String readPerm = threadObj.getString("readperm");
                String author = threadObj.getString("author");
                String authorId = threadObj.getString("authorid");
                String subject = threadObj.getString("subject");
                String updateAtString = threadObj.getString("lastpost");
                String publishAtStringTimestamp = threadObj.getString("dbdateline");
                String lastUpdator = threadObj.optString("lastposter", "");
                String viewNum = threadObj.getString("views");
                String replyNum = threadObj.getString("replies");
                String displayOrder = threadObj.getString("displayorder");
                String digest = threadObj.optString("digest", "0");
                String special = threadObj.optString("special", "0");
                String recommendNum = threadObj.optString("recommend_add", "0");
                String rushReply = threadObj.optString("rushreply", "0");
                String price = threadObj.optString("price", "0");
                String attachment = threadObj.optString("attachment", "0");
                String replyCredit = threadObj.optString("replycredit", "0");

                // parse short reply
                List<threadInfo.shortReplyInfo> shortReplyInfoList = new ArrayList<>();
                if (threadObj.has("reply")) {
                    JSONArray shortReplies = threadObj.getJSONArray("reply");
                    for (int j = 0; j < shortReplies.length(); j++) {
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
                thread.publishAt = new Timestamp(Long.parseLong(publishAtStringTimestamp) * 1000);
                thread.lastUpdateTimeString = updateAtString;
                thread.digest = !digest.equals("0");
                thread.special = !special.equals("0");
                thread.rushReply = !rushReply.equals("0");
                thread.recommendNum = Integer.parseInt(recommendNum);
                thread.price = Integer.parseInt(price);
                thread.attachment = Integer.parseInt(attachment);
                thread.shortReplyInfoList = shortReplyInfoList;
                thread.displayOrder = displayOrder;
                thread.replyCredit = Integer.parseInt(replyCredit);

                if (!displayOrder.equals("0")) {
                    thread.isTop = true;
                }
                if (isFirst == false && thread.isTop == true) {
                    continue;
                }
                threadInfoList.add(thread);
            }
            return threadInfoList;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class forumDetailedInfo{
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int fid, fup, threads, posts, autoclose, threadcount, password;
        public String name, rules, description;


    }

    public static forumDetailedInfo getForumDetailedInfo(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONObject forumInfo = variables.getJSONObject("forum");
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(forumInfo.toString(), forumDetailedInfo.class);


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getThreadRuleString(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONObject forumInfo = variables.getJSONObject("forum");

            return forumInfo.optString("rules", "");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getThreadDescriptionString(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONObject forumInfo = variables.getJSONObject("forum");

            return forumInfo.optString("description", "");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<threadCommentInfo.attachmentInfo> getAttachmentInfo(JSONObject jsonObject) {
        try {

            List<threadCommentInfo.attachmentInfo> attachmentInfoList = new ArrayList<>();
            Iterator<String> stringIterator = jsonObject.keys();
            while (stringIterator.hasNext()) {
                String key = stringIterator.next();
                JSONObject attachmentObj = jsonObject.getJSONObject(key);
                String aid = attachmentObj.getString("aid");
                String tid = attachmentObj.getString("tid");
                String pid = attachmentObj.getString("pid");
                String uid = attachmentObj.getString("uid");
                String filename = attachmentObj.getString("filename");
//                if (!(filename.endsWith("png")||filename.endsWith("jpg")||filename.endsWith("gif"))){
//                    continue;
//                }
                String relativeUrl = attachmentObj.getString("attachment");
                String prefixUrl = attachmentObj.getString("url");
                String publishAtStr = attachmentObj.getString("dbdateline");
                Date publishAt = new Timestamp(Long.parseLong(publishAtStr) * 1000);

                attachmentInfoList.add(new threadCommentInfo.attachmentInfo(aid, tid, pid, uid, relativeUrl, filename, publishAt, prefixUrl));
            }
            return attachmentInfoList;
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

    public static forumUserBriefInfo parseLoginBreifUserInfo(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            //Log.d(TAG,"Get ->"+jsonObject.toString());
            //List<bbsNotification> notifications = new ArrayList<>();
            JSONObject variables = jsonObject.getJSONObject("Variables");

            if (isLoginSuccessful(jsonObject)) {
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

    public static List<threadCommentInfo> parseThreadCommentInfo(String s) {
        try {
            List<threadCommentInfo> threadInfoList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray threadList = variables.getJSONArray("postlist");
            for (int i = 0; i < threadList.length(); i++) {
                JSONObject threadObj = threadList.getJSONObject(i);
                String tid = threadObj.getString("tid");
                String pid = threadObj.getString("pid");
                String firstString = threadObj.getString("first");
                String author = threadObj.getString("author");
                String authorId = threadObj.getString("authorid");
                String message = threadObj.getString("message");
                String publishAtStringTimestamp = threadObj.getString("dbdateline");
                String lastPostTimeString = threadObj.getString("dateline");
                Date publishAt = new Timestamp(Long.parseLong(publishAtStringTimestamp) * 1000);
                threadCommentInfo threadComment = new threadCommentInfo(tid, pid, author, authorId, message, publishAt, lastPostTimeString);
                if (firstString.equals("1")) {
                    threadComment.first = true;
                } else {
                    threadComment.first = false;
                }
                // attachment
                if (threadObj.has("attachments")) {
                    Log.d(TAG, "Find attachment!!!");
                    try {
                        JSONObject attachmentObj = threadObj.getJSONObject("attachments");
                        List<threadCommentInfo.attachmentInfo> attachmentInfoList = getAttachmentInfo(attachmentObj);
                        Log.d(TAG, "get attachmentInfo" + attachmentInfoList.size());
                        threadComment.attachmentInfoList = attachmentInfoList;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                threadInfoList.add(threadComment);


            }
            return threadInfoList;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String parseUploadHashString(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONObject allowperm = variables.getJSONObject("allowperm");
            return allowperm.getString("uploadhash");
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

    public static String parsePostThreadInfo(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONObject message = jsonObject.getJSONObject("Message");
            return message.getString("messagestr");
        } catch (Exception e) {
            return null;
        }
    }

    public static List<threadInfo> parseHotThreadListInfo(String s) {
        try {
            List<threadInfo> threadInfoList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray threadList = variables.getJSONArray("data");
            for (int i = 0; i < threadList.length(); i++) {
                JSONObject threadObj = threadList.getJSONObject(i);
                String tid = threadObj.getString("tid");
                String typeid = threadObj.getString("typeid");
                String readPerm = threadObj.getString("readperm");
                String author = threadObj.getString("author");
                String authorId = threadObj.getString("authorid");
                String subject = threadObj.getString("subject");
                String updateAtString = threadObj.getString("lastpost");
                String publishAtStringTimestamp = threadObj.getString("dbdateline");
                String lastUpdator = threadObj.optString("lastposter", "");
                String viewNum = threadObj.getString("views");
                String replyNum = threadObj.getString("replies");
                String displayOrder = threadObj.getString("displayorder");
                String digest = threadObj.optString("digest", "0");
                String special = threadObj.optString("special", "0");
                String recommendNum = threadObj.optString("recommend_add", "0");
                String rushReply = threadObj.optString("rushreply", "0");
                String price = threadObj.optString("price", "0");
                String attachment = threadObj.optString("attachment", "0");

                // parse short reply
                List<threadInfo.shortReplyInfo> shortReplyInfoList = new ArrayList<>();
                if (threadObj.has("reply")) {
                    JSONArray shortReplies = threadObj.getJSONArray("reply");
                    for (int j = 0; j < shortReplies.length(); j++) {
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
                thread.publishAt = new Timestamp(Long.parseLong(publishAtStringTimestamp) * 1000);
                thread.lastUpdateTimeString = updateAtString;
                thread.digest = !digest.equals("0");
                thread.special = !special.equals("0");
                thread.rushReply = !rushReply.equals("0");
                thread.recommendNum = Integer.parseInt(recommendNum);
                thread.price = Integer.parseInt(price);
                thread.attachment = Integer.parseInt(attachment);
                thread.shortReplyInfoList = shortReplyInfoList;
                thread.displayOrder = displayOrder;

                if (!displayOrder.equals("0")) {
                    thread.isTop = true;
                }
                threadInfoList.add(thread);
            }
            return threadInfoList;

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

    public static List<threadInfo> parseMyThreadListInfo(String s) {
        try {
            List<threadInfo> threadInfoList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray threadList = variables.getJSONArray("data");
            for (int i = 0; i < threadList.length(); i++) {
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
                thread.publishAt = new Timestamp(Long.parseLong(publishAtStringTimestamp) * 1000);
                thread.fid = fid;
                if (!displayOrder.equals("0")) {
                    thread.isTop = true;
                }
                threadInfoList.add(thread);
            }
            return threadInfoList;

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

    public static Map<String, String> parseUserProfile(String s) {

        try {
            Map<String, String> info = new HashMap<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");

            JSONObject spaceInfo = variables.getJSONObject("space");
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

    public static Map<String, String> parseUserGroupInfo(String s) {

        try {
            Map<String, String> info = new HashMap<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");

            JSONObject spaceInfo = variables.getJSONObject("space");
            JSONObject groupInfo = spaceInfo.getJSONObject("group");
            Iterator<String> sIterator = groupInfo.keys();
            while (sIterator.hasNext()) {
                String key = sIterator.next();
                if (groupInfo.get(key) instanceof String) {
                    String val = groupInfo.getString(key);
                    info.put(key, val);
                }
            }
            return info;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
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

    public static List<userFriend> parseUserFriendInfo(String s) {
        try {
            List<userFriend> userFriendList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray pmList = variables.getJSONArray("list");
            for (int i = 0; i < pmList.length(); i++) {
                JSONObject pm = (JSONObject) pmList.get(i);
                userFriend userFriend = new userFriend(
                        Integer.parseInt(pm.getString("uid")),
                        pm.getString("username")
                );
                userFriendList.add(userFriend);
            }
            return userFriendList;

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

    public static int parseNotificationCount(String s) {
        try {
            List<privateDetailMessage> privateDetailMessageList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");

            return Integer.parseInt(variables.getString("count"));

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static List<notificationDetailInfo> parseNotificationDetailInfo(String s) {
        try {
            List<notificationDetailInfo> notificationDetailInfoList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONArray noticeList = variables.getJSONArray("list");
            for (int i = 0; i < noticeList.length(); i++) {
                JSONObject notice = noticeList.getJSONObject(i);
                String publishAtStringTimestamp = notice.getString("dateline");
                Date publishAt = new Timestamp(Long.parseLong(publishAtStringTimestamp) * 1000);
                notificationDetailInfo notificationDetail = new notificationDetailInfo(
                        Integer.parseInt(notice.getString("id")),
                        Integer.parseInt(notice.getString("uid")),
                        notice.getString("type"),
                        !notice.getString("new").equals("0"),
                        Integer.parseInt(notice.getString("authorid")),
                        notice.optString("author", ""),
                        notice.getString("note"),
                        publishAt,
                        Integer.parseInt(notice.getString("from_id")),
                        notice.getString("from_idtype"),
                        Integer.parseInt(notice.getString("from_num")),
                        parseNotificationVariable(notice)

                );
                notificationDetailInfoList.add(notificationDetail);

            }
            return notificationDetailInfoList;

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


    public static bbsPollInfo parsePollInfo(String s) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            List<smileyInfo> smileyInfoList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            if(variables.has("special_poll")){
                JSONObject pollInfo = variables.getJSONObject("special_poll");
                return mapper.readValue(pollInfo.toString(), bbsPollInfo.class);
            }
            else {
                return null;
            }



        } catch (Exception e) {

            e.printStackTrace();
            return null;
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
        public String tid;
        public String fid;
        @JsonProperty("posttableid")
        public String postableId;
        @JsonProperty("typeid")
        public String typeId;
        public String sortid,  author, authorid, subject;
        @JsonProperty("lastpost")
        @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="s")
        public Date lastPostTime;
        public String lastposter, displayorder;
        @JsonFormat(shape=JsonFormat.Shape.STRING)
        public int views, replies;
        public String highlight;
        @JsonDeserialize(using = OneZeroDeserializer.class)
        @JsonFormat(shape=JsonFormat.Shape.STRING)
        public Boolean  special, moderated, is_archived;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int rate, closed, status, readperm, price, digest;


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
        public String relatebytag, maxposition, bgcolor;
        @JsonProperty("maxposition")
        @JsonFormat(shape=JsonFormat.Shape.STRING)
        public int maxPosition;
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
        public String recommendlevel, heatlevel, relay, ordertype, recommend;
        @JsonProperty("freemessage")
        public String freeMessage;
        @JsonProperty("replycredit_rule")
        public replyCreditRule creditRule;
    }

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

    public static DetailedThreadInfo parseDetailedThreadInfo(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONObject variables = jsonObject.getJSONObject("Variables");
            JSONObject threadInfo = variables.getJSONObject("thread");
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(threadInfo.toString(), DetailedThreadInfo.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ThreadPostParameterResult parseThreadPostParameter(String s) {
        try {

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(s, ThreadPostParameterResult.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
