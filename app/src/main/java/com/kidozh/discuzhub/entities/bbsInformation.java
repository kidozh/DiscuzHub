package com.kidozh.discuzhub.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
public class bbsInformation implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    public String base_url;

    public String site_name;
    public String discuz_version, version, plugin_version;
    public String total_posts, total_members;
    public String default_fid, mysite_id;
    public String ucenter_url,register_name,charset;
    public String primaryColor;
    public Boolean hideRegister, qqConnect;
    public Boolean useSafeClient = true, isSync = true;
    public Date addedTime, updateTime;
    @Nullable
    public int position = 0;

    public int getId(){
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAPIVersion(){
        try{
            return Integer.parseInt(version);
        }
        catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public bbsInformation(String base_url, String site_name, String discuz_version,
                          String charset,
                          String version, String plugin_version, String total_posts,
                          String total_members, String mysite_id, String default_fid,
                          String ucenter_url, String register_name, String primaryColor,
                          Boolean hideRegister, Boolean qqConnect, int position){
        this.charset = charset;
        this.base_url = base_url;
        this.site_name = site_name;
        this.discuz_version = discuz_version;
        this.version = version;
        this.plugin_version = plugin_version;
        this.total_members = total_members;
        this.total_posts = total_posts;
        this.default_fid = default_fid;
        this.ucenter_url = ucenter_url;
        this.mysite_id = mysite_id;
        this.register_name = register_name;
        this.primaryColor = primaryColor;
        this.addedTime = new Date();
        this.updateTime = new Date();
        this.hideRegister = hideRegister;
        this.qqConnect = qqConnect;
        this.position = position;
    }
    @Ignore
    public bbsInformation(String base_url, String site_name, String discuz_version,
                          String charset,
                          String version, String plugin_version, String total_posts,
                          String total_members, String mysite_id, String default_fid,
                          String ucenter_url, String register_name, String primaryColor,
                          Boolean hideRegister, Boolean qqConnect){
        this.charset = charset;
        this.base_url = base_url;
        this.site_name = site_name;
        this.discuz_version = discuz_version;
        this.version = version;
        this.plugin_version = plugin_version;
        this.total_members = total_members;
        this.total_posts = total_posts;
        this.default_fid = default_fid;
        this.ucenter_url = ucenter_url;
        this.mysite_id = mysite_id;
        this.register_name = register_name;
        this.primaryColor = primaryColor;
        this.addedTime = new Date();
        this.updateTime = new Date();
        this.hideRegister = hideRegister;
        this.qqConnect = qqConnect;
    }

    public void setUseSafeClient(Boolean useSafeClient){
        this.useSafeClient = useSafeClient;
    }

    public Boolean isSecureClient(){
        return this.useSafeClient && this.base_url.startsWith("https://");
    }

    public String getRegisterURL(){
        return base_url + "/member.php?mod="+register_name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof bbsInformation)) return false;
        bbsInformation that = (bbsInformation) o;
        return getId() == that.getId() &&
                position == that.position &&
                Objects.equals(base_url, that.base_url) &&
                Objects.equals(site_name, that.site_name) &&
                Objects.equals(discuz_version, that.discuz_version) &&
                Objects.equals(version, that.version) &&
                Objects.equals(plugin_version, that.plugin_version) &&
                Objects.equals(total_posts, that.total_posts) &&
                Objects.equals(total_members, that.total_members) &&
                Objects.equals(default_fid, that.default_fid) &&
                Objects.equals(mysite_id, that.mysite_id) &&
                Objects.equals(ucenter_url, that.ucenter_url) &&
                Objects.equals(register_name, that.register_name) &&
                Objects.equals(charset, that.charset) &&
                Objects.equals(primaryColor, that.primaryColor) &&
                Objects.equals(hideRegister, that.hideRegister) &&
                Objects.equals(qqConnect, that.qqConnect) &&
                Objects.equals(useSafeClient, that.useSafeClient) &&
                Objects.equals(isSync, that.isSync) &&
                Objects.equals(addedTime, that.addedTime) &&
                Objects.equals(updateTime, that.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), base_url, site_name, discuz_version, version, plugin_version, total_posts, total_members, default_fid, mysite_id, ucenter_url, register_name, charset, primaryColor, hideRegister, qqConnect, useSafeClient, isSync, addedTime, updateTime, position);
    }
}
