package com.kidozh.discuzhub.adapter

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.text.Html
import android.text.SpannableString
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.OnThreadAdmined
import com.kidozh.discuzhub.activities.ThreadActivity
import com.kidozh.discuzhub.activities.UserProfileActivity
import com.kidozh.discuzhub.daos.ViewHistoryDao
import com.kidozh.discuzhub.database.ViewHistoryDatabase.Companion.getInstance
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.Thread
import com.kidozh.discuzhub.entities.Thread.Companion.ThreadDiffCallback
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.results.ForumResult.ShortReply
import com.kidozh.discuzhub.utilities.*
import com.kidozh.discuzhub.utilities.AnimationUtils.getRecyclerviewAnimation
import com.kidozh.discuzhub.utilities.TimeDisplayUtils.Companion.getLocalePastTimeString
import com.kidozh.discuzhub.utilities.UserPreferenceUtils.conciseRecyclerView
import java.io.InputStream
import java.util.*

class ThreadAdapter(var threadType: Map<String, String>?, var bbsInfo: Discuz, var userBriefInfo: User?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    @JvmField
    var threadList: MutableList<Thread> = ArrayList()
    @JvmField
    var ignoreDigestStyle = false
    lateinit var context: Context
    var viewHistoryDao: ViewHistoryDao? = null
    override fun getItemId(position: Int): Long {
        return threadList[position].tid.toLong()
    }

    fun updateList(newThreadList: List<Thread>?) {
        if(newThreadList == null){
            return
        }
        Log.d(TAG,"Update new threads "+newThreadList.size)
        val result = DiffUtil.calculateDiff(ThreadDiffCallback(threadList, newThreadList))
        this.threadList = newThreadList as MutableList<Thread>
        result.dispatchUpdatesTo(this)
    }

    fun updateListAndType(newThreadList: List<Thread>?, threadType: Map<String, String>?){
        this.threadType = threadType
        updateList(newThreadList)
    }

    override fun getItemViewType(position: Int): Int {
        val thread = threadList[position]
        return if (thread.displayOrder <= 0) {
            R.layout.item_thread
        } else {
            R.layout.item_thread_pinned
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        
        viewHistoryDao = getInstance(context).dao
        val inflater = LayoutInflater.from(context)
        val shouldAttachToParentImmediately = false
        return if (!ignoreDigestStyle && viewType == R.layout.item_thread_pinned) {
            val view = inflater.inflate(R.layout.item_thread_pinned, parent, shouldAttachToParentImmediately)
            PinnedViewHolder(view)
        } else {
            // normal item
            if (!conciseRecyclerView(context)) {
                val view = inflater.inflate(R.layout.item_thread, parent, shouldAttachToParentImmediately)
                ThreadViewHolder(view)
            } else {
                val view = inflater.inflate(R.layout.item_thread_concise, parent, shouldAttachToParentImmediately)
                ConciseThreadViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holderRaw: RecyclerView.ViewHolder, position: Int) {
        val thread = threadList[position]
        if (holderRaw is PinnedViewHolder) {
            val holder = holderRaw
            val sp = Html.fromHtml(thread.subject)
            val spannableString = SpannableString(sp)
            holder.mTitle.setText(spannableString, TextView.BufferType.SPANNABLE)

            // thread type
            if (thread.displayOrder != 0) {
                var textResource = R.string.bbs_forum_pinned
                textResource = when (thread.displayOrder) {
                    3 -> R.string.display_order_3
                    2 -> R.string.display_order_2
                    1 -> R.string.display_order_1
                    -1 -> R.string.display_order_n1
                    -2 -> R.string.display_order_n2
                    -3 -> R.string.display_order_n3
                    -4 -> R.string.display_order_n4
                    else -> R.string.bbs_forum_pinned
                }
                holder.mThreadType.setText(textResource)
                //holder.mThreadType.setBackgroundColor(context.getColor(R.color.colorAccent));
            } else {
                if (threadType == null) {
                    holder.mThreadType.visibility = View.GONE
                } else {
                    // provided by label
                    holder.mThreadType.visibility = View.VISIBLE
                    val type = threadType!![java.lang.String.valueOf(thread.typeId)]
                    if (type != null) {
                        val threadSpanned = Html.fromHtml(type)
                        val threadSpannableString = SpannableString(threadSpanned)
                        holder.mThreadType.text = threadSpannableString
                    } else {
                        holder.mThreadType.setText(R.string.bbs_forum_pinned)
                    }
                }
            }
            holder.mCardview.setOnClickListener {
                val intent = Intent(context, ThreadActivity::class.java)
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                intent.putExtra(ConstUtils.PASS_THREAD_KEY, thread)
                intent.putExtra("FID", thread.fid)
                intent.putExtra("TID", thread.tid)
                intent.putExtra("SUBJECT", thread.subject)
                VibrateUtils.vibrateForClick(context)
                val options = ActivityOptions.makeSceneTransitionAnimation(context as Activity?,
                        Pair.create(holder.mTitle, "bbs_thread_subject")
                )
                val bundle = options.toBundle()
                context.startActivity(intent, bundle)
            }
            holder.mCardview.setOnLongClickListener {
                VibrateUtils.vibrateForNotice(context)
                this.onlongPressCard(position)
                true
            }
        } else if (holderRaw is ThreadViewHolder) {
            val holder = holderRaw
            val sp = Html.fromHtml(thread.subject,Html.FROM_HTML_MODE_COMPACT)
            val spannableString = SpannableString(sp)
            holder.mTitle.setText(spannableString, TextView.BufferType.SPANNABLE)
            holder.mThreadViewNum.text = numberFormatUtils.getShortNumberText(thread.views)
            holder.mThreadReplyNum.text = numberFormatUtils.getShortNumberText(thread.replies)
            holder.mPublishDate.text = getLocalePastTimeString(context, thread.publishAt!!)
            if (thread.displayOrder != 0) {
                var textResource = R.string.bbs_forum_pinned
                textResource = when (thread.displayOrder) {
                    3 -> R.string.display_order_3
                    2 -> R.string.display_order_2
                    1 -> R.string.display_order_1
                    -1 -> R.string.display_order_n1
                    -2 -> R.string.display_order_n2
                    -3 -> R.string.display_order_n3
                    -4 -> R.string.display_order_n4
                    else -> R.string.bbs_forum_pinned
                }
                holder.mThreadType.setText(textResource)
                //holder.mThreadType.setBackgroundColor(context.getColor(R.color.colorAccent));
            } else {
                if (threadType == null) {
                    holder.mThreadType.visibility = View.GONE
                } else {
                    // provided by label
                    holder.mThreadType.visibility = View.VISIBLE
                    val type = threadType!![java.lang.String.valueOf(thread.typeId)]
                    if (type != null) {
                        val threadSpanned = Html.fromHtml(type)
                        val threadSpannableString = SpannableString(threadSpanned)
                        holder.mThreadType.text = threadSpannableString
                    } else {
                        holder.mThreadType.text = String.format("%s", position + 1)
                    }
                }

                //holder.mThreadType.setBackgroundColor(context.getColor(R.color.ThreadTypeBackgroundColor));
            }
            holder.mThreadPublisher.text = thread.author
            var avatar_num = thread.authorId % 16
            if (avatar_num < 0) {
                avatar_num = -avatar_num
            }
            val avatarResource = context.resources.getIdentifier(String.format("avatar_%s", avatar_num + 1), "drawable", context.packageName)
            val factory = OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(context))
            Glide.get(context).registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
            val source = URLUtils.getSmallAvatarUrlByUid(thread.authorId)
            val options = RequestOptions()
                    .placeholder(context.getDrawable(avatarResource))
                    .error(context.getDrawable(avatarResource))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH)
            val glideUrl = GlideUrl(source,
                    LazyHeaders.Builder().addHeader("referer", bbsInfo.base_url).build()
            )
            if (NetworkUtils.canDownloadImageOrFile(context)) {
                Glide.with(context)
                        .load(glideUrl)
                        .apply(options)
                        .into(holder.mAvatarImageview)
            } else {
                Glide.with(context)
                        .load(glideUrl)
                        .apply(options)
                        .onlyRetrieveFromCache(true)
                        .into(holder.mAvatarImageview)
            }
            // set short reply
            if (thread.recommendNum != 0) {
                holder.mRecommendationNumber.visibility = View.VISIBLE
                holder.mRecommendationNumber.text = numberFormatUtils.getShortNumberText(thread.recommendNum)
            } else {
                holder.mRecommendationNumber.visibility = View.GONE
            }
            if (thread.readPerm == 0) {
                holder.mReadPerm.visibility = View.GONE
            } else {
                holder.mReadPerm.visibility = View.VISIBLE
                holder.mReadPerm.text = java.lang.String.valueOf(thread.readPerm)
                //holder.mReadPerm.setText(numberFormatUtils.getShortNumberText(threadInfo.readPerm));
                val readPermissionVal = thread.readPerm
                if (userBriefInfo == null || userBriefInfo!!.readPerm < readPermissionVal) {
                    holder.mReadPerm.setTextColor(context.getColor(R.color.colorWarn))
                } else {
                    holder.mReadPerm.setTextColor(context.getColor(R.color.colorTextDefault))
                }
            }
            if (thread.attachment == 0) {
                holder.mAttachmentIcon.visibility = View.GONE
            } else {
                holder.mAttachmentIcon.visibility = View.VISIBLE
                if (thread.attachment == 1) {
                    holder.mAttachmentIcon.setImageDrawable(context.getDrawable(R.drawable.ic_thread_attachment_24px))
                } else {
                    holder.mAttachmentIcon.setImageDrawable(context.getDrawable(R.drawable.ic_image_outlined_24px))
                }
            }
            if (thread.price != 0) {
                holder.mPriceNumber.text = java.lang.String.valueOf(thread.price)
                holder.mPriceNumber.visibility = View.VISIBLE
            } else {
                // holder.mPriceNumber.setText(String.valueOf(threadInfo.price));
                holder.mPriceNumber.visibility = View.GONE
            }
            if (thread.shortReplyList.isNotEmpty()) {
                val linearLayoutManager = LinearLayoutManager(context)
                holder.mReplyRecyclerview.isFocusable = false
                holder.mReplyRecyclerview.isNestedScrollingEnabled = false
                holder.mReplyRecyclerview.layoutManager = linearLayoutManager
                holder.mReplyRecyclerview.isClickable = false
                val adapter = ShortPostAdapter()
                adapter.setShortReplyInfoList((thread.shortReplyList as List<ShortReply?>))
                holder.mReplyRecyclerview.itemAnimator = getRecyclerviewAnimation(context)
                holder.mReplyRecyclerview.adapter = adapter
                holder.mReplyRecyclerview.isNestedScrollingEnabled = false
            } else {
                // still flush it to avoid cache problem
                holder.mReplyRecyclerview.adapter = ShortPostAdapter()
            }
            holder.mCardview.setOnClickListener {
                val intent = Intent(context, ThreadActivity::class.java)
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                intent.putExtra(ConstUtils.PASS_THREAD_KEY, thread)
                intent.putExtra("FID", thread.fid)
                intent.putExtra("TID", thread.tid)
                intent.putExtra("SUBJECT", thread.subject)
                VibrateUtils.vibrateForClick(context)
                val options = ActivityOptions.makeSceneTransitionAnimation(context as Activity?,
                        Pair.create(holder.mTitle, "bbs_thread_subject")
                )
                val bundle = options.toBundle()
                context.startActivity(intent, bundle)
            }
            holder.mCardview.setOnLongClickListener {
                VibrateUtils.vibrateForNotice(context)
                this.onlongPressCard(position)
                true
            }
            holder.mAvatarImageview.setOnClickListener {
                val intent = Intent(context, UserProfileActivity::class.java)
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                intent.putExtra("UID", thread.authorId)
                val options = ActivityOptions
                        .makeSceneTransitionAnimation(context as Activity?, holder.mAvatarImageview, "user_info_avatar")
                val bundle = options.toBundle()
                context.startActivity(intent, bundle)
            }
        } else if (holderRaw is ConciseThreadViewHolder) {
            val sp = Html.fromHtml(thread.subject)
            val spannableString = SpannableString(sp)
            holderRaw.mTitle.setText(spannableString, TextView.BufferType.SPANNABLE)
            holderRaw.mThreadReplyNum.text = numberFormatUtils.getShortNumberText(thread.replies)
            holderRaw.mPublishDate.text = getLocalePastTimeString(context, thread.publishAt!!)

            //holder.mPublishDate.setText(df.format(threadInfo.publishAt));
            if (thread.displayOrder != 0) {
                var textResource = R.string.bbs_forum_pinned
                textResource = when (thread.displayOrder) {
                    3 -> R.string.display_order_3
                    2 -> R.string.display_order_2
                    1 -> R.string.display_order_1
                    -1 -> R.string.display_order_n1
                    -2 -> R.string.display_order_n2
                    -3 -> R.string.display_order_n3
                    -4 -> R.string.display_order_n4
                    else -> R.string.bbs_forum_pinned
                }
                holderRaw.mThreadType.setText(textResource)
                holderRaw.mThreadType.setTextColor(context.getColor(R.color.colorAccent))
                holderRaw.mThreadType.visibility = View.VISIBLE
            } else {
                holderRaw.mThreadType.visibility = View.GONE
            }
            var avatar_num = thread.authorId % 16
            if (avatar_num < 0) {
                avatar_num = -avatar_num
            }
            val avatarResource = context.resources.getIdentifier(
                String.format("avatar_%s", avatar_num + 1),
                "drawable",
                context.packageName
            )
            val factory = OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(context))
            Glide.get(context).registry.replace(
                GlideUrl::class.java,
                InputStream::class.java,
                factory
            )
            val source = URLUtils.getDefaultAvatarUrlByUid(thread.authorId)
            val options = RequestOptions()
                .placeholder(context.getDrawable(avatarResource))
                .error(context.getDrawable(avatarResource))
            val glideUrl = GlideUrl(
                source,
                LazyHeaders.Builder().addHeader("referer", bbsInfo.base_url).build()
            )
            if (NetworkUtils.canDownloadImageOrFile(context)) {
                Glide.with(context)
                    .load(glideUrl)
                    .apply(options)
                    .into(holderRaw.mAvatarImageview)
            } else {
                Glide.with(context)
                    .load(glideUrl)
                    .apply(options)
                    .onlyRetrieveFromCache(true)
                    .into(holderRaw.mAvatarImageview)
            }
            holderRaw.mThreadPublisher.text = thread.author
            holderRaw.mCardview.setOnClickListener {
                val intent = Intent(context, ThreadActivity::class.java)
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, userBriefInfo)
                intent.putExtra(ConstUtils.PASS_THREAD_KEY, thread)
                intent.putExtra("FID", thread.fid)
                intent.putExtra("TID", thread.tid)
                intent.putExtra("SUBJECT", thread.subject)
                VibrateUtils.vibrateForClick(context)
                val options = ActivityOptions.makeSceneTransitionAnimation(
                    context as Activity?,
                    Pair.create(holderRaw.mTitle, "bbs_thread_subject")
                )
                val bundle = options.toBundle()
                context.startActivity(intent, bundle)
            }

            holderRaw.mCardview.setOnLongClickListener {
                VibrateUtils.vibrateForNotice(context)
                this.onlongPressCard(position)
                true
            }
        }

    }

    override fun getItemCount(): Int {
        return threadList.size
    }

    inner class PinnedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mTitle: TextView
        var mThreadType: TextView
        var mCardview: CardView

        init {
            mTitle = itemView.findViewById(R.id.bbs_thread_title)
            mThreadType = itemView.findViewById(R.id.bbs_thread_type)
            mCardview = itemView.findViewById(R.id.bbs_thread_cardview)
        }
    }

    inner class ThreadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mThreadPublisher: TextView
        var mPublishDate: TextView
        var mTitle: TextView
        var mThreadViewNum: TextView
        var mThreadReplyNum: TextView
        var mThreadType: TextView
        var mAvatarImageview: ShapeableImageView
        var mCardview: CardView
        var mReplyRecyclerview: RecyclerView
        var mRecommendationNumber: TextView
        var mReadPerm: TextView
        var mAttachmentIcon: ImageView
        var mPriceNumber: TextView

        init {
            mThreadPublisher = itemView.findViewById(R.id.bbs_post_publisher)
            mPublishDate = itemView.findViewById(R.id.bbs_post_publish_date)
            mTitle = itemView.findViewById(R.id.bbs_thread_title)

            mThreadViewNum = itemView.findViewById(R.id.bbs_thread_view_textview)
            mThreadReplyNum = itemView.findViewById(R.id.bbs_thread_reply_number)
            mThreadType = itemView.findViewById(R.id.bbs_thread_type)
            mAvatarImageview = itemView.findViewById(R.id.bbs_post_avatar_imageView)
            mCardview = itemView.findViewById(R.id.bbs_thread_cardview)
            mReplyRecyclerview = itemView.findViewById(R.id.bbs_thread_short_reply_recyclerview)
            mRecommendationNumber = itemView.findViewById(R.id.bbs_thread_recommend_number)
            mReadPerm = itemView.findViewById(R.id.bbs_thread_read_perm_number)
            mAttachmentIcon = itemView.findViewById(R.id.bbs_thread_attachment_image)
            mPriceNumber = itemView.findViewById(R.id.bbs_thread_price_number)
        }
    }

    inner class ConciseThreadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mThreadPublisher: TextView
        var mPublishDate: TextView
        var mTitle: TextView
        var mThreadReplyNum: TextView
        var mThreadType: TextView
        var mCardview: CardView
        var mAvatarImageview: ShapeableImageView

        init {
            mThreadPublisher = itemView.findViewById(R.id.bbs_post_publisher)
            mPublishDate = itemView.findViewById(R.id.bbs_post_publish_date)
            mTitle = itemView.findViewById(R.id.bbs_thread_title)
            mThreadReplyNum = itemView.findViewById(R.id.bbs_thread_reply_number)
            mThreadType = itemView.findViewById(R.id.bbs_thread_type)
            mAvatarImageview = itemView.findViewById(R.id.bbs_post_avatar_imageView)
            mCardview = itemView.findViewById(R.id.bbs_thread_cardview)
        }
    }



    fun onlongPressCard(position: Int){
        if (context is OnThreadAdmined){
            val mlistener: OnThreadAdmined = context as OnThreadAdmined
            mlistener.adminThread(threadList[position])
        }
    }

    companion object {
        private val TAG = ThreadAdapter::class.java.simpleName
    }

    init {
        setHasStableIds(true)
    }
}