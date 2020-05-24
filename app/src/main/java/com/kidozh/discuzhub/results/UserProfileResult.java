package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kidozh.discuzhub.utilities.OneZeroBooleanJsonDeserializer;
import com.kidozh.discuzhub.utilities.URLUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfileResult extends BaseResult {
    @JsonProperty("Variables")
    public UserProfileVariableResult userProfileVariableResult;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserProfileVariableResult extends VariableResults{

        @JsonProperty("space")
        public SpaceVariables space;

    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SpaceVariables{
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int uid,status, credits;
        public String username;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        @JsonProperty("emailstatus")
        public boolean emailStatus;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        @JsonProperty("avatarstatus")
        public boolean avatarStatus;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        @JsonProperty("videophotostatus")
        public boolean videoPhotoStatus;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("adminid")
        public int adminId;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("groupid")
        public int groupId;
        public String groupexpiry, regdate;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("extgroupids")
        public String extendGroup;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        @JsonProperty("notifysound")
        public boolean notifySound;
        @JsonProperty("timeoffset")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int timeZone;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int newpm, newprompt;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        @JsonProperty("accessmasks")
        public boolean accessMasks;
        public String allowadmincp;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        @JsonProperty("onlyacceptfriendpm")
        public boolean onlyAcceptFriendPM;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        @JsonProperty("conisbind")
        public boolean connectBinded;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        public boolean freeze;
        // need to rewrite
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int extcredits1,extcredits2,extcredits3,extcredits4,extcredits5,extcredits6,extcredits7,extcredits8;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int friends, posts, threads, digestposts, doings, blogs, albums, sharings,
                views, feeds, follower, following, newfollower, blacklist;
        @JsonProperty("attachsize")
        public String attachmentSize;
        @JsonProperty("oltime")
        public int onlineHours;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int todayattachs ;
        @JsonProperty("todayattachsize")
        public String todayAttachSize;
        public String videophoto, spacename, spacedescription, domain,addsize,
                addfriend,menunum,theme,spacecss, blockposition, feedfriend, magicgift;
        @JsonProperty("recentnote")
        public String recentNotification;
        @JsonProperty("spacenote")
        public String spaceNotification;
        @JsonProperty("privacy")
        public PrivacySetting privacySetting;
        @JsonProperty("sightml")
        public String sigatureHtml;
        public String publishfeed, authstr, groupterms, groups, attentiongroup;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int gender, birthyear, birthmonth, birthday;

        public String constellation, zodiac, nationality, birthprovince,
                birthcity, birthdist, birthcommunity, resideprovince,
                residecity, residedist, residecommunity, residesuite, graduateschool;
        @JsonProperty("position")
        public String workPosition;
        public String company, education, occupation, revenue, lookingfor, bloodtype;
        @JsonProperty("affectivestatus")
        public String marriedStatus;
        public String height, weight, site, bio, interest;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int port;
        public String lastvisit, lastactivity,lastpost, lastsendmail;
        public String field1,field2,field3,field4,field5,field6,field7,field8;
        public String regipport,lastipport;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        public boolean invisible;
        public String buyercredit,sellercredit;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int favtimes, sharetimes, profileprogress;
        @JsonProperty("admingroup")
        public AdminGroupVariables adminGroup;
        @JsonProperty("group")
        public GroupVariables group;
        @JsonProperty("lastactivitydb")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "s")
        public Date lastActivityDate;

        public String buyerrank, sellerrank, groupiconid;
        public int upgradecredit, upgradeprogress;
        @JsonDeserialize(using = MedalInfoJsonDeserializer.class)
        public List<Medal> medals = new ArrayList<>();



    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PrivacySetting{
        @JsonProperty("feed")
        public FeedPrivacySetting feedPrivacy;
        @JsonProperty("view")
        public ViewPrivacySetting viewPrivacySetting;
        @JsonProperty("profile")
        @JsonIgnoreProperties(ignoreUnknown = true)
        public ProfilePrivacySetting profilePrivacySetting;
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FeedPrivacySetting{
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int doing, blog, upload, poll, newthread,
                share, friend, comment, show, credit, invite, task, profile, click, newreply;

    }

    public static class ViewPrivacySetting{
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int friend = 2, wall = 2, home = 2, doing  = 2, blog = 2, album = 2, share = 2;

    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProfilePrivacySetting{
        public int realname ,gender ,birthday ,birthcity ,residecity ,affectivestatus ,lookingfor ,
                bloodtype ,telephone ,mobile ,qq ,msn ,taobao ,graduateschool ,education ,company ,
                occupation ,position ,revenue ,idcardtype ,idcard ,address ,zipcode ,site ,bio ,interest;
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AdminGroupVariables{
        public String type;
        @JsonProperty("grouptitle")
        public String groupTitle;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int stars;
        public String color, icon;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("readaccess")
        public int readAccess;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("allowgetattach")
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        public boolean allowGetAttach;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("allowgetimage")
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        public boolean allowGetImage;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("allowmediacode")
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        public boolean allowMediaCode;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("maxsigsize")
        public int maxSignatureSize;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("allowbegincode")
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        public boolean allowBeginCode;

        public String userstatusby;

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GroupVariables{
        public String type;
        @JsonProperty("grouptitle")
        public String groupTitle;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int stars;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("creditshigher")
        public int upperBoundCredit = -1;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("creditslower")
        public int lowerBoundCredit = -1;

        public String color, icon;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("readaccess")
        public int readAccess;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("allowgetattach")
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        public boolean allowGetAttach;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("allowgetimage")
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        public boolean allowGetImage;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("allowmediacode")
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        public boolean allowMediaCode;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("maxsigsize")
        public int maxSignatureSize;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("allowbegincode")
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
        public boolean allowBeginCode;

        public String userstatusby;

    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class extendCredit{
        public String img, title, unit, ratio;

    }

    public static class Medal{
        public String name, image, description;
        @JsonProperty("medalid")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int medalId;

        public String getMedalImageURL(){
            return URLUtils.getBBSMedalImageURL(image);
        }
    }

    public static class MedalInfoJsonDeserializer extends JsonDeserializer<List<Medal>> {

        @Override
        public List<Medal> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonToken currentToken = p.getCurrentToken();
            if(currentToken.equals(JsonToken.VALUE_STRING)){
                return new ArrayList<>();
            }
            else if(currentToken.equals(JsonToken.START_ARRAY)) {
                ObjectMapper mapper = new ObjectMapper();

                return mapper.readValue(p,  new TypeReference<List<Medal>>(){});
            }
            else {
                return new ArrayList<>();
            }

        }
    }

    public boolean isError(){
        return this.message == null;
    }
}
