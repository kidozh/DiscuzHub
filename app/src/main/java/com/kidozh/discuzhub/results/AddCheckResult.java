package com.kidozh.discuzhub.results;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.utilities.OneZeroBooleanJsonDeserializer;

@JsonIgnoreProperties(ignoreUnknown=true)
public class AddCheckResult {
    @JsonProperty("discuzversion")
    public String discuz_version;
    public String charset;
    @JsonProperty("version")
    public String apiVersion;
    @JsonProperty("pluginversion")
    public String pluginVersion;
    @JsonProperty("regname")
    public String registerName;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
    @JsonProperty("qqconnect")
    public boolean qqConnect;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
    @JsonProperty("wsqqqconnect")
    public boolean wsqQQConnect;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanJsonDeserializer.class)
    @JsonProperty("wsqhideregister")
    public boolean wsqHideRegister;
    @JsonProperty("sitename")
    public String siteName;
    @JsonProperty("mysiteid")
    public String mySiteId;
    @JsonProperty("ucenterurl")
    public String uCenterURL;
    @JsonProperty("defaultfid")
    public String defaultFid;
    @JsonProperty("totalposts")
    public int totalPosts;
    @JsonProperty("totalmembers")
    public int totalMembers;
    @JsonProperty("testcookie")
    public String testCookie;

    public bbsInformation toBBSInformation(String baseUrl){
        return new bbsInformation(baseUrl,siteName,discuz_version,charset,apiVersion,pluginVersion,
                String.valueOf(totalPosts),String.valueOf(totalMembers),String.valueOf(mySiteId),
                defaultFid,uCenterURL,registerName,"",wsqHideRegister,qqConnect
                );
    }
}
