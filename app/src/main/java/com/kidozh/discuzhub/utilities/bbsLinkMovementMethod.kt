package com.kidozh.discuzhub.utilities

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.Spannable
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.ForumActivity
import com.kidozh.discuzhub.activities.InternalWebViewActivity
import com.kidozh.discuzhub.activities.ThreadActivity
import com.kidozh.discuzhub.activities.UserProfileActivity
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.Forum
import com.kidozh.discuzhub.entities.Thread
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.utilities.UserPreferenceUtils.getRewriteRule
import com.kidozh.discuzhub.utilities.UserPreferenceUtils.saveRewriteRule
import java.util.regex.Pattern

class bbsLinkMovementMethod(private val mOnLinkClickedListener: OnLinkClickedListener) :
    LinkMovementMethod() {
    var x1 = 0
    var x2 = 0
    var y1 = 0
    var y2 = 0
    override fun onTouchEvent(
        widget: TextView, buffer: Spannable,
        event: MotionEvent
    ): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_DOWN) {
            x1 = event.x.toInt()
            y1 = event.y.toInt()
        }
        if (action == MotionEvent.ACTION_UP) {
            x2 = event.x.toInt()
            y2 = event.y.toInt()
            if (Math.abs(x1 - x2) < 10 && Math.abs(y1 - y2) < 10) {
                x2 -= widget.totalPaddingLeft
                y2 -= widget.totalPaddingTop
                x2 += widget.scrollX
                y2 += widget.scrollY
                val layout = widget.layout
                val line = layout.getLineForVertical(y2)
                val off = layout.getOffsetForHorizontal(line, x2.toFloat())
                val spans = buffer.getSpans(off, off, URLSpan::class.java)
                if (spans.size != 0) {
                    val url = spans[0].url
                    val handled = mOnLinkClickedListener.onLinkClicked(url)
                    return if (handled) {
                        true
                    } else super.onTouchEvent(widget, buffer, event)
                }
            }
        }
        return super.onTouchEvent(widget, buffer, event)
    }

    override fun canSelectArbitrarily(): Boolean {
        return true
    }

    override fun onKeyUp(
        widget: TextView, buffer: Spannable, keyCode: Int,
        event: KeyEvent
    ): Boolean {
        return false
    }

    interface OnLinkClickedListener {
        fun onLinkClicked(url: String): Boolean
    }

    companion object {
        private val TAG = bbsLinkMovementMethod::class.java.simpleName
        @JvmStatic
        private fun parseURLAndOpen(
            context: Context,
            bbsInfo: Discuz,
            userBriefInfo: User?,
            urlString: String
        ): Boolean {
            // simple unescape
            var url = urlString
            url = url
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&nbsp;", " ")
            val finalURL = url
            Log.d(TAG, "Parse and open URL in movement method $url")
            val uri = Uri.parse(url)
            val baseUri = Uri.parse(bbsInfo.base_url)
            val clickedUri = Uri.parse(url)
            var clickedURLPath = clickedUri.path
            val basedURLPath = baseUri.path
            if (clickedURLPath != null && basedURLPath != null) {
                if (clickedURLPath.matches(Regex("^${basedURLPath}.*"))) {
                    clickedURLPath = clickedURLPath.substring(basedURLPath.length)
                }
            }
            return if (clickedUri.host == null || clickedUri.host == baseUri.host) {
                // check static first
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && clickedURLPath != null) {
                    if (!TextUtils.isEmpty(
                            getRewriteRule(
                                context,
                                bbsInfo,
                                UserPreferenceUtils.REWRITE_FORM_DISPLAY_KEY
                            )
                        )
                    ) {
                        var rewriteRule = getRewriteRule(
                            context,
                            bbsInfo,
                            UserPreferenceUtils.REWRITE_FORM_DISPLAY_KEY
                        )
                        saveRewriteRule(
                            context,
                            bbsInfo,
                            UserPreferenceUtils.REWRITE_FORM_DISPLAY_KEY,
                            rewriteRule
                        )

                        // match template such as f{fid}-{page}
                        // crate reverse copy
                        rewriteRule = rewriteRule!!.replace("{fid}", "(?<fid>\\d+)")
                        rewriteRule = rewriteRule.replace("{page}", "(?<page>\\d+)")
                        val pattern = Pattern.compile(rewriteRule)
                        val matcher = pattern.matcher(clickedURLPath)
                        if (matcher.find()) {
                            val fidStr = matcher.group("fid")
                            //val pageStr = matcher.group("page")
                            // handle it
                            if (fidStr != null) {
                                val fid: Int = try {
                                    fidStr.toInt()
                                } catch (e: Exception) {
                                    0
                                }
                                val intent = Intent(context, ForumActivity::class.java)
                                val clickedForum = Forum()
                                clickedForum.fid = fid
                                intent.putExtra(ConstUtils.PASS_FORUM_THREAD_KEY, clickedForum)
                                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                                Log.d(TAG, "put base url " + bbsInfo.base_url)
                                VibrateUtils.vibrateForClick(context)
                                context.startActivity(intent)
                                return true
                            }
                        }
                    }
                    if (!TextUtils.isEmpty(
                            getRewriteRule(
                                context,
                                bbsInfo,
                                UserPreferenceUtils.REWRITE_VIEW_THREAD_KEY
                            )
                        )
                    ) {
                        // match template such as t{tid}-{page}-{prevpage}
                        var rewriteRule = getRewriteRule(
                            context,
                            bbsInfo,
                            UserPreferenceUtils.REWRITE_VIEW_THREAD_KEY
                        )
                        saveRewriteRule(
                            context,
                            bbsInfo,
                            UserPreferenceUtils.REWRITE_VIEW_THREAD_KEY,
                            rewriteRule
                        )

                        // match template such as f{fid}-{page}
                        // crate reverse copy
                        rewriteRule = rewriteRule!!.replace("{tid}", "(?<tid>\\d+)")
                        rewriteRule = rewriteRule.replace("{page}", "(?<page>\\d+)")
                        rewriteRule = rewriteRule.replace("{prevpage}", "(?<prevpage>\\d+)")
                        val pattern = Pattern.compile(rewriteRule)
                        val matcher = pattern.matcher(clickedURLPath)
                        if (matcher.find()) {
                            val tidStr = matcher.group("tid")
                            // val pageStr = matcher.group("page")
                            // handle it
                            if (tidStr != null) {
                                val putThread = Thread()
                                val tid: Int = try {
                                    tidStr.toInt()
                                } catch (e: Exception) {
                                    0
                                }
                                putThread.tid = tid
                                val intent = Intent(context, ThreadActivity::class.java)
                                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                                intent.putExtra(ConstUtils.PASS_THREAD_KEY, putThread)
                                intent.putExtra("FID", "0")
                                intent.putExtra("TID", tid)
                                intent.putExtra("SUBJECT", url)
                                VibrateUtils.vibrateForClick(context)
                                val options =
                                    ActivityOptions.makeSceneTransitionAnimation(context as Activity)
                                val bundle = options.toBundle()
                                context.startActivity(intent, bundle)
                                return true
                            }
                        }
                    }
                    if (!TextUtils.isEmpty(
                            getRewriteRule(
                                context,
                                bbsInfo,
                                UserPreferenceUtils.REWRITE_HOME_SPACE
                            )
                        )
                    ) {
                        // match template such as s{user}-{name}
                        var rewriteRule =
                            getRewriteRule(context, bbsInfo, UserPreferenceUtils.REWRITE_HOME_SPACE)
                        //UserPreferenceUtils.saveRewriteRule(context,bbsInfo,UserPreferenceUtils.REWRITE_HOME_SPACE,rewriteRule);
                        Log.d(TAG, "Get home space rewrite url $rewriteRule path $clickedURLPath")

                        // match template such as f{fid}-{page}
                        // crate reverse copy
                        rewriteRule = rewriteRule!!.replace("{user}", "(?<user>\\w+)")
                        rewriteRule = rewriteRule.replace("{value}", "(?<value>\\d+)")
                        val pattern = Pattern.compile(rewriteRule)
                        val matcher = pattern.matcher(clickedURLPath)
                        if (matcher.find()) {
                            // val userString = matcher.group("user")
                            val uidString = matcher.group("value")
                            Log.d(TAG, "Get uid $uidString")
                            // handle it
                            if (uidString != null) {
                                var uid = 0
                                try {
                                    uid = uidString.toInt()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                val intent = Intent(context, UserProfileActivity::class.java)
                                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                                intent.putExtra("UID", uid)
                                VibrateUtils.vibrateForClick(context)
                                val options =
                                    ActivityOptions.makeSceneTransitionAnimation(context as Activity)
                                val bundle = options.toBundle()
                                context.startActivity(intent, bundle)
                                return true
                            }
                        }
                    }
                }
                if (uri != null && uri.path != null) {
                    if (uri.getQueryParameter("mod") != null && uri.getQueryParameter("mod") == "redirect" && uri.getQueryParameter(
                            "goto"
                        ) != null && uri.getQueryParameter("goto") == "findpost" && uri.getQueryParameter(
                            "pid"
                        ) != null && uri.getQueryParameter("ptid") != null
                    ) {
                        val pidString = uri.getQueryParameter("pid")
                        val tidString = uri.getQueryParameter("ptid")
                        var redirectTid = 0
                        var redirectPid = 0
                        try {
                            redirectTid = tidString!!.toInt()
                            redirectPid = pidString!!.toInt()
                        } catch (e: Exception) {
                        }
                        Log.d(TAG, "Find the current $redirectPid tid $redirectTid")
                        val putThread = Thread()
                        putThread.tid = redirectTid
                        val intent = Intent(context, ThreadActivity::class.java)
                        intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                        intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                        intent.putExtra(ConstUtils.PASS_THREAD_KEY, putThread)
                        intent.putExtra("FID", 0)
                        intent.putExtra("TID", redirectTid)
                        intent.putExtra("SUBJECT", url)
                        VibrateUtils.vibrateForClick(context)
                        context.startActivity(intent)
                        return true
                    } else if (uri.getQueryParameter("mod") != null && uri.getQueryParameter("mod") == "viewthread" && uri.getQueryParameter(
                            "tid"
                        ) != null
                    ) {
                        val tidString = uri.getQueryParameter("tid")
                        val redirectTid: Int = try {
                            tidString!!.toInt()
                        } catch (e: Exception) {
                            0
                        }
                        val putThread = Thread()
                        putThread.tid = redirectTid
                        val intent = Intent(context, ThreadActivity::class.java)
                        intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                        intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                        intent.putExtra(ConstUtils.PASS_THREAD_KEY, putThread)
                        intent.putExtra("FID", 0)
                        intent.putExtra("TID", redirectTid)
                        intent.putExtra("SUBJECT", url)
                        VibrateUtils.vibrateForClick(context)
                        context.startActivity(intent)
                        return true
                    } else if (uri.getQueryParameter("mod") != null && uri.getQueryParameter("mod") == "forumdisplay" && uri.getQueryParameter(
                            "fid"
                        ) != null
                    ) {
                        val fidString = uri.getQueryParameter("fid")
                        val fid : Int = try {
                            fidString!!.toInt()
                        } catch (e: Exception) {
                            0
                        }
                        val intent = Intent(context, ForumActivity::class.java)
                        val clickedForum = Forum()
                        clickedForum.fid = fid
                        intent.putExtra(ConstUtils.PASS_FORUM_THREAD_KEY, clickedForum)
                        intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                        intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                        Log.d(TAG, "put base url " + bbsInfo.base_url)
                        VibrateUtils.vibrateForClick(context)
                        context.startActivity(intent)
                        return true
                    } else if (uri.getQueryParameter("mod") != null && uri.getQueryParameter("mod") == "space" && uri.getQueryParameter(
                            "uid"
                        ) != null
                    ) {
                        val uidStr = uri.getQueryParameter("uid")
                        val uid = try {
                            uidStr!!.toInt()
                        } catch (e: Exception) {
                            0
                        }
                        val intent = Intent(context, UserProfileActivity::class.java)
                        intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                        intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                        intent.putExtra("UID", uid)
                        context.startActivity(intent)
                        return true
                    }
                    val intent = Intent(context, InternalWebViewActivity::class.java)
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                    intent.putExtra(ConstUtils.PASS_URL_KEY, url)
                    Log.d(TAG, "Inputted URL $url")
                    context.startActivity(intent)
                    true
                } else {
                    val intent = Intent(context, InternalWebViewActivity::class.java)
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                    intent.putExtra(ConstUtils.PASS_URL_KEY, url)
                    Log.d(TAG, "Inputted URL $url")
                    context.startActivity(intent)
                    true
                }
            } else {
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                val outLinkWarn =
                    prefs.getBoolean(context.getString(R.string.preference_key_outlink_warn), true)
                if (outLinkWarn) {
                    AlertDialog.Builder(context)
                        .setTitle(R.string.outlink_warn_title)
                        .setMessage(
                            context.getString(
                                R.string.outlink_warn_message,
                                clickedUri.host,
                                baseUri.host
                            )
                        )
                        .setNeutralButton(R.string.bbs_show_in_internal_browser) { _, _ ->
                            val intent = Intent(context, InternalWebViewActivity::class.java)
                            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                            intent.putExtra(ConstUtils.PASS_URL_KEY, finalURL)
                            Log.d(TAG, "Inputted URL $finalURL")
                            context.startActivity(intent)
                        }
                        .setPositiveButton(R.string.bbs_show_in_external_browser) { _, _ ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalURL))
                            context.startActivity(intent)
                        }
                        .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                        .show()
                    true
                } else {
                    val intent = Intent(context, InternalWebViewActivity::class.java)
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                    intent.putExtra(ConstUtils.PASS_URL_KEY, url)
                    Log.d(TAG, "Inputted URL $url")
                    context.startActivity(intent)
                    true
                }
            }
        }

        fun onLinkClicked(context: Context,
                          bbsInfo: Discuz,
                          userBriefInfo: User?,
                          url: String) : Boolean{
            val unescapedURL = url
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&nbsp;", " ")
            // judge the host
            val baseURL = URLUtils.getBaseUrl()
            val baseUri = Uri.parse(baseURL)
            val clickedUri = Uri.parse(unescapedURL)
            Log.i(TAG, "checking with ${unescapedURL} ${clickedUri.host} ${baseUri.host} ${clickedUri.host == null} ${clickedUri.host == baseUri.host}")
            if (clickedUri.host == null || clickedUri.host == baseUri.host) {
                // internal link

                if (true) {
                    val rewriteRules: MutableMap<String, String> = HashMap()
                    var clickedURLPath = clickedUri.path
                    Log.i(TAG,"clickedURLPath ${clickedURLPath}")
                    if (clickedURLPath == null) {
                        parseURLAndOpen(context,bbsInfo,userBriefInfo,unescapedURL)
                    }
                    val basedURLPath = baseUri.path
                    if (clickedURLPath != null && basedURLPath != null) {
                        if (clickedURLPath.matches(Regex("^$basedURLPath.*"))) {
                            clickedURLPath = clickedURLPath.substring(basedURLPath.length)
                        }
                    }
                    Log.i(TAG,"new potiential clickedURLPath ${clickedURLPath}")
                    // only catch two type : forum_forumdisplay & forum_viewthread
                    // only 8.0+ support reverse copy
                    Log.i(TAG,"new judge ${Build.VERSION.SDK_INT >= Build.VERSION_CODES.O} ${rewriteRules}")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // load from cache if not exist
                        val displayForumRewriteRule = UserPreferenceUtils.getRewriteRule(
                            context,
                            bbsInfo,
                            rewriteKey = UserPreferenceUtils.REWRITE_FORM_DISPLAY_KEY
                        )
                        if (displayForumRewriteRule != null) {
                            rewriteRules.put("forum_forumdisplay", displayForumRewriteRule)
                        }

                        val viewThreadRewriteRule = UserPreferenceUtils.getRewriteRule(
                            context,
                            bbsInfo,
                            rewriteKey = UserPreferenceUtils.REWRITE_VIEW_THREAD_KEY
                        )
                        if (viewThreadRewriteRule != null) {
                            rewriteRules.put("forum_viewthread", viewThreadRewriteRule)
                        }


                        val homeSpaceRewriteRule = UserPreferenceUtils.getRewriteRule(
                            context,
                            bbsInfo,
                            rewriteKey = UserPreferenceUtils.REWRITE_HOME_SPACE
                        )
                        if (homeSpaceRewriteRule != null) {
                            rewriteRules.put("home_space", homeSpaceRewriteRule)
                        }

                        Log.i(TAG,"after fix  ${rewriteRules}")

                        if (rewriteRules.containsKey("forum_forumdisplay")) {
                            var rewriteRule = rewriteRules["forum_forumdisplay"]
                            UserPreferenceUtils.saveRewriteRule(
                                context,
                                bbsInfo,
                                UserPreferenceUtils.REWRITE_FORM_DISPLAY_KEY,
                                rewriteRule
                            )
                            if (rewriteRule == null || clickedURLPath == null) {
                                parseURLAndOpen(context,bbsInfo,userBriefInfo,unescapedURL)
                                return true
                            }
                            // match template such as f{fid}-{page}
                            // crate reverse copy
                            rewriteRule = rewriteRule.replace("{fid}", "(?<fid>\\d+)")
                            rewriteRule = rewriteRule.replace("{page}", "(?<page>\\d+)")
                            val pattern = Pattern.compile(rewriteRule)
                            val matcher = pattern.matcher(clickedURLPath)
                            if (matcher.find()) {
                                val fidStr = matcher.group("fid")
                                val pageStr = matcher.group("page")
                                // handle it
                                if (fidStr != null) {
                                    var fid = 0
                                    fid = try {
                                        fidStr.toInt()
                                    } catch (e: Exception) {
                                        0
                                    }

                                    //                                    int page = Integer.parseInt(pageStr);
                                    val intent = Intent(context, ForumActivity::class.java)
                                    val clickedForum = Forum()
                                    clickedForum.fid = fid
                                    intent.putExtra(ConstUtils.PASS_FORUM_THREAD_KEY, clickedForum)
                                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                                    Log.d(TAG, "put base url " + bbsInfo.base_url)
                                    VibrateUtils.vibrateForClick(context)
                                    context.startActivity(intent)
                                    return true
                                }
                            }
                        }
                        if (rewriteRules.containsKey("forum_viewthread")) {
                            // match template such as t{tid}-{page}-{prevpage}
                            var rewriteRule = rewriteRules["forum_viewthread"]
                            UserPreferenceUtils.saveRewriteRule(
                                context,
                                bbsInfo,
                                UserPreferenceUtils.REWRITE_VIEW_THREAD_KEY,
                                rewriteRule
                            )
                            if (rewriteRule == null || clickedURLPath == null) {
                                parseURLAndOpen(context,bbsInfo,userBriefInfo,unescapedURL)
                                return true
                            }
                            Log.i(TAG,"Match view thread ${rewriteRule} : ${clickedURLPath}")
                            // match template such as f{fid}-{page}
                            // crate reverse copy
                            rewriteRule = rewriteRule.replace("{tid}", "(?<tid>\\d+)")
                            rewriteRule = rewriteRule.replace("{page}", "(?<page>\\d+)")
                            rewriteRule = rewriteRule.replace("{prevpage}", "(?<prevpage>\\d+)")
                            val pattern = Pattern.compile(rewriteRule)
                            val matcher = pattern.matcher(clickedURLPath)
                            if (matcher.find()) {
                                val tidStr = matcher.group("tid")
                                val pageStr = matcher.group("page")
                                // handle it
                                if (tidStr != null) {
                                    val putThreadInfo = Thread()
                                    var tid = 0
                                    tid = try {
                                        tidStr.toInt()
                                    } catch (e: Exception) {
                                        0
                                    }
                                    putThreadInfo.tid = tid
                                    val intent = Intent(context, ThreadActivity::class.java)
                                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                                    intent.putExtra(ConstUtils.PASS_THREAD_KEY, putThreadInfo)
                                    intent.putExtra("FID", 0)
                                    intent.putExtra("TID", tid)
                                    intent.putExtra("SUBJECT", url)
                                    VibrateUtils.vibrateForClick(context)
                                    val options = ActivityOptions.makeSceneTransitionAnimation(context as Activity)
                                    val bundle = options.toBundle()
                                    context.startActivity(intent, bundle)
                                    return true
                                }
                            }
                        }
                        if (rewriteRules.containsKey("home_space")) {
                            // match template such as t{tid}-{page}-{prevpage}
                            var rewriteRule = rewriteRules["home_space"]
                            Log.d(TAG, "get home space url $rewriteRule")
                            UserPreferenceUtils.saveRewriteRule(
                                context,
                                bbsInfo,
                                UserPreferenceUtils.REWRITE_HOME_SPACE,
                                rewriteRule
                            )
                            if (rewriteRule == null || clickedURLPath == null) {
                                parseURLAndOpen(context,bbsInfo,userBriefInfo,unescapedURL)
                                return true
                            }
                            // match template such as f{fid}-{page}
                            // crate reverse copy
                            rewriteRule = rewriteRule.replace("{user}", "(?<user>\\d+)")
                            rewriteRule = rewriteRule.replace("{value}", "(?<value>\\d+)")
                            val pattern = Pattern.compile(rewriteRule)
                            val matcher = pattern.matcher(clickedURLPath)
                            if (matcher.find()) {
                                val userString = matcher.group("user")
                                val uidString = matcher.group("value")
                                // handle it
                                if (uidString != null) {
                                    var uid = 0
                                    uid = try {
                                        uidString.toInt()
                                    } catch (e: Exception) {
                                        0
                                    }
                                    val intent = Intent(context, UserProfileActivity::class.java)
                                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                                    intent.putExtra("UID", uid)
                                    VibrateUtils.vibrateForClick(context)
                                    val options = ActivityOptions.makeSceneTransitionAnimation(context as Activity)
                                    val bundle = options.toBundle()
                                    context.startActivity(intent, bundle)
                                    return true
                                }
                            }
                        }
                        parseURLAndOpen(context,bbsInfo,userBriefInfo,unescapedURL)
                    } else {
                        parseURLAndOpen(context,bbsInfo,userBriefInfo,unescapedURL)
                    }
                }
                else {
                    // parse the URL
                    parseURLAndOpen(context,bbsInfo,userBriefInfo,unescapedURL)
                }
            }
            else {
                val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
                val outLinkWarn = prefs.getBoolean(context.getString(R.string.preference_key_outlink_warn), true)
                if (outLinkWarn) {
                    android.app.AlertDialog.Builder(context)
                        .setTitle(R.string.outlink_warn_title)
                        .setMessage(context.getString(R.string.outlink_warn_message, clickedUri.host, baseUri.host))
                        .setNeutralButton(R.string.bbs_show_in_internal_browser) { dialog, which ->
                            val intent = Intent(context, InternalWebViewActivity::class.java)
                            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                            intent.putExtra(ConstUtils.PASS_URL_KEY, unescapedURL)
                            Log.d(TAG, "Inputted URL $unescapedURL")
                            context.startActivity(intent)
                        }
                        .setPositiveButton(R.string.bbs_show_in_external_browser) { dialog, which ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(unescapedURL))
                            context.startActivity(intent)
                        }
                        .setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.dismiss() }
                        .show()
                } else {
                    val intent = Intent(context, InternalWebViewActivity::class.java)
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                    intent.putExtra(ConstUtils.PASS_URL_KEY, unescapedURL)
                    Log.d(TAG, "Inputted URL $unescapedURL")
                    context.startActivity(intent)
                    return true
                }
            }
            return true
        }
    }
}