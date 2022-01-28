package com.kidozh.discuzhub.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity
class Discuz : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id = 0
    @JvmField
    var base_url: String
    @JvmField
    var site_name: String
    @JvmField
    var discuz_version: String
    @JvmField
    var version: Int
    @JvmField
    var plugin_version: String
    @JvmField
    var total_posts: Long
    @JvmField
    var total_members: Long
    @JvmField
    var default_fid: Int
    @JvmField
    var mysite_id: String
    @JvmField
    var ucenter_url: String
    @JvmField
    var register_name: String
    @JvmField
    var charset: String
    @JvmField
    var primaryColor: String
    @JvmField
    var hideRegister: Boolean
    @JvmField
    var qqConnect: Boolean
    @JvmField
    var isSync = true
    @JvmField
    var addedTime: Date
    @JvmField
    var updateTime: Date
    @JvmField
    var position = 0
    val apiVersion: Int
        get() = try {
            version.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }

    constructor(base_url: String, site_name: String, discuz_version: String,
                charset: String,
                version: Int, plugin_version: String, total_posts: Long,
                total_members: Long, mysite_id: String, default_fid: Int,
                ucenter_url: String, register_name: String, primaryColor: String,
                hideRegister: Boolean, qqConnect: Boolean, position: Int) {
        this.charset = charset
        this.base_url = base_url
        this.site_name = site_name
        this.discuz_version = discuz_version
        this.version = version
        this.plugin_version = plugin_version
        this.total_members = total_members
        this.total_posts = total_posts
        this.default_fid = default_fid
        this.ucenter_url = ucenter_url
        this.mysite_id = mysite_id
        this.register_name = register_name
        this.primaryColor = primaryColor
        addedTime = Date()
        updateTime = Date()
        this.hideRegister = hideRegister
        this.qqConnect = qqConnect
        this.position = position
    }

    @Ignore
    constructor(base_url: String, site_name: String, discuz_version: String,
                charset: String,
                version: Int, plugin_version: String, total_posts: Long,
                total_members: Long, mysite_id: String, default_fid: Int,
                ucenter_url: String, register_name: String, primaryColor: String,
                hideRegister: Boolean, qqConnect: Boolean) {
        this.charset = charset
        this.base_url = base_url
        this.site_name = site_name
        this.discuz_version = discuz_version
        this.version = version
        this.plugin_version = plugin_version
        this.total_members = total_members
        this.total_posts = total_posts
        this.default_fid = default_fid
        this.ucenter_url = ucenter_url
        this.mysite_id = mysite_id
        this.register_name = register_name
        this.primaryColor = primaryColor
        addedTime = Date()
        updateTime = Date()
        this.hideRegister = hideRegister
        this.qqConnect = qqConnect
    }



    val isSecureClient: Boolean
        get() = base_url.startsWith("https://")
    val registerURL: String
        get() = "$base_url/member.php?mod=$register_name"

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is Discuz) return false
        val that = o
        return id == that.id && position == that.position &&
                base_url == that.base_url &&
                site_name == that.site_name &&
                discuz_version == that.discuz_version &&
                version == that.version &&
                plugin_version == that.plugin_version &&
                total_posts == that.total_posts &&
                total_members == that.total_members &&
                default_fid == that.default_fid &&
                mysite_id == that.mysite_id &&
                ucenter_url == that.ucenter_url &&
                register_name == that.register_name &&
                charset == that.charset &&
                primaryColor == that.primaryColor &&
                hideRegister == that.hideRegister &&
                qqConnect == that.qqConnect &&
                isSync == that.isSync &&
                addedTime == that.addedTime &&
                updateTime == that.updateTime
    }

    override fun hashCode(): Int {
        return Objects.hash(id, base_url, site_name, discuz_version, version, plugin_version, total_posts, total_members, default_fid, mysite_id, ucenter_url, register_name, charset, primaryColor, hideRegister, qqConnect, isSync, addedTime, updateTime, position)
    }

    fun getAvatarUrl(uid: Int): String{
        return String.format("%s/data/avatar/%03d/%02d/%02d/%02d_avatar_big.jpg", this.ucenter_url, uid / 1000000, uid / 10000 % 100, uid / 100 % 100, uid % 100)
    }
}