package com.kidozh.discuzhub.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Html
import android.text.Html.ImageGetter
import android.text.Html.TagHandler
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.text.style.StrikethroughSpan
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.graphics.drawable.DrawableWrapper
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.FullImageActivity
import com.kidozh.discuzhub.activities.OnPostAdmined
import com.kidozh.discuzhub.activities.UserProfileActivity
import com.kidozh.discuzhub.adapter.PostAdapter.PostViewHolder
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.Post
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.entities.ViewThreadQueryStatus
import com.kidozh.discuzhub.results.ThreadResult
import com.kidozh.discuzhub.utilities.ConstUtils
import com.kidozh.discuzhub.utilities.NetworkUtils
import com.kidozh.discuzhub.utilities.TimeDisplayUtils.Companion.getLocalePastTimeString
import com.kidozh.discuzhub.utilities.URLUtils
import com.kidozh.discuzhub.utilities.UserPreferenceUtils.conciseRecyclerView
import com.kidozh.discuzhub.utilities.UserPreferenceUtils.isJammerContentsRemoved
import com.kidozh.discuzhub.utilities.bbsLinkMovementMethod
import com.kidozh.discuzhub.utilities.bbsLinkMovementMethod.OnLinkClickedListener
import okhttp3.OkHttpClient
import org.xml.sax.XMLReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*
import java.util.function.BiConsumer
import java.util.regex.Pattern


val BACKGROUND_ALPHA = 25
val POST_HIDDEN = 1
val POST_WARNED = 2
val POST_REVISED = 4
val POST_MOBILE = 8

class PostAdapter(private val bbsInfo: Discuz, private val user: User?, viewThreadQueryStatus: ViewThreadQueryStatus) : RecyclerView.Adapter<PostViewHolder>() {
    private var postList: List<Post> = ArrayList()
    set(value) {
        Log.d(TAG,"Value new size "+value.size+" old size "+field.size)
        field = value
        val result = DiffUtil.calculateDiff(Post.Companion.DiffCallback(field, value))
        result.dispatchUpdatesTo(this)
    }

    private val postCommentList: MutableMap<String, List<ThreadResult.Comment>> = HashMap()
    private var client = OkHttpClient()
    private var viewThreadQueryStatus: ViewThreadQueryStatus
    private var mListener: onFilterChanged? = null
    private val listener: AdapterView.OnItemClickListener? = null
    private var replyListener: onAdapterReply? = null
    private var onLinkClickedListener: OnLinkClicked? = null
    private var onAdvanceOptionClickedListener: OnAdvanceOptionClicked? = null
    private var authorId = 0
    private lateinit var context : Context

    init {
        setHasStableIds(false)
    }

    fun clearList() {
        updateList(ArrayList())

    }

    fun getPosts(): List<Post>{
        return postList
    }

    init {
        setHasStableIds(true)
    }

    private fun updateList(newList: List<Post>?) {
        if(newList == null){
            return
        }
        Log.d(TAG,"Recv post list "+this.postList.size+" new list "+newList.size)
        val result = DiffUtil.calculateDiff(Post.Companion.DiffCallback(postList, newList))
        this.postList = newList
        result.dispatchUpdatesTo(this)
        notifyDataSetChanged()
    }



    fun setPosts(newList: MutableList<Post>, viewThreadQueryStatus: ViewThreadQueryStatus, authorId: Int){
        val iterator = newList.iterator()
        while (iterator.hasNext()) {
            val post = iterator.next()
            // remove nullable message
            if (post.message == "") {
                iterator.remove()
            }
        }
        Log.d(TAG,"set post list "+this.postList.size+" new list "+newList.size)
        this.viewThreadQueryStatus = viewThreadQueryStatus
        this.authorId = authorId
        updateList(newList)
    }
    
    


    override fun getItemId(position: Int): Long {
        val post = postList[position]
        return post.pid.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        context = parent.context

        client = NetworkUtils.getPreferredClient(context)
        val layoutIdForListItem = R.layout.item_post
        val inflater = LayoutInflater.from(context)
        val shouldAttachToParentImmediately = false
        replyListener = context as onAdapterReply
        onLinkClickedListener = context as OnLinkClicked
        if (context is OnAdvanceOptionClicked) {
            onAdvanceOptionClickedListener = context as OnAdvanceOptionClicked
        }
        val view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        if (post.author == "") {
            return
        }
        holder.mThreadPublisher.text = post.author
        if (post.authorId == authorId) {
            holder.isAuthorLabel.visibility = View.VISIBLE
        } else {
            holder.isAuthorLabel.visibility = View.GONE
        }
        // parse status
        val status = post.status

        if (status and POST_HIDDEN != 0) {
            holder.mPostStatusBlockedView.visibility = View.VISIBLE
            holder.mPostStatusBlockedView.setBackgroundColor(context.getColor(R.color.colorPomegranate))
            holder.mPostStatusBlockedView.background.alpha = BACKGROUND_ALPHA
        } else {
            holder.mPostStatusBlockedView.visibility = View.GONE
        }
        if (status and POST_WARNED != 0) {
            holder.mPostStatusWarnedView.visibility = View.VISIBLE
            holder.mPostStatusWarnedView.setBackgroundColor(context.getColor(R.color.colorOrange))
            holder.mPostStatusWarnedView.background.alpha = BACKGROUND_ALPHA
        } else {
            holder.mPostStatusWarnedView.visibility = View.GONE
        }
        if (status and POST_REVISED != 0) {
            holder.mPostStatusEditedView.visibility = View.VISIBLE
            holder.mPostStatusEditedView.setBackgroundColor(context.getColor(R.color.colorPrimary))
            holder.mPostStatusEditedView.background.alpha = BACKGROUND_ALPHA
        } else {
            holder.mPostStatusEditedView.visibility = View.GONE
        }
        if (status and POST_MOBILE != 0 && !conciseRecyclerView(context)) {
            holder.mPostStatusMobileIcon.visibility = View.VISIBLE
        } else {
            holder.mPostStatusMobileIcon.visibility = View.GONE
        }
        var decodeString = post.message
        // extract quote message
        //String quoteRegexInVer4 = "^<div class=.reply_wrap.>.*?</div><br />";
        val quoteRegexInVer4 = "^<div class=\"reply_wrap\">(.+?)</div><br .>"

        // remove it if possible
        val quotePatternInVer4 = Pattern.compile(quoteRegexInVer4, Pattern.DOTALL)
        val quoteMatcherInVer4 = quotePatternInVer4.matcher(decodeString)
        // delete it first
        if (quoteMatcherInVer4.find()) {

            //decodeString = quoteMatcherInVer4.replaceAll("");
            val quoteString = quoteMatcherInVer4.group(1)
            holder.mPostQuoteContent.visibility = View.VISIBLE
            // set html
            val HtmlTagHandler: HtmlTagHandler = HtmlTagHandler(context, holder.mPostQuoteContent)
            val sp = Html.fromHtml(quoteString, HtmlCompat.FROM_HTML_MODE_LEGACY, PostImageGetter(holder.mPostQuoteContent), HtmlTagHandler)
            val spannableString = SpannableString(sp)
            holder.mPostQuoteContent.setText(spannableString, TextView.BufferType.SPANNABLE)
            holder.mPostQuoteContent.isFocusable = true
            holder.mPostQuoteContent.setTextIsSelectable(true)
            holder.mPostQuoteContent.movementMethod = bbsLinkMovementMethod(object :
                OnLinkClickedListener {
                override fun onLinkClicked(url: String): Boolean {
                    return if (onLinkClickedListener != null) {
                        if (url.lowercase().startsWith("http://") || url.lowercase()
                                .startsWith("https://")
                        ) {
                            onLinkClickedListener!!.onLinkClicked(url)
                            true
                        } else {
                            false
                        }
                    } else {
                        false
                    }
                }
            })
        } else {
            holder.mPostQuoteContent.visibility = View.GONE
        }
        decodeString = quoteMatcherInVer4.replaceAll("")
        // handle jammer contents
        Log.d(TAG, "is removed contents " + isJammerContentsRemoved(context))
        if (isJammerContentsRemoved(context)) {
            decodeString = decodeString.replace("<font class=\"jammer\">.+</font>".toRegex(), "")
            decodeString = decodeString.replace("<span style=\"display:none\">.+</span>".toRegex(), "")

        }
        val htmlTagHandler: HtmlTagHandler = HtmlTagHandler(context, holder.mContent)
        val sp = Html.fromHtml(decodeString, HtmlCompat.FROM_HTML_MODE_LEGACY, PostImageGetter(holder.mContent), htmlTagHandler)
        val spannableString = SpannableString(sp)
        holder.mContent.setText(spannableString, TextView.BufferType.SPANNABLE)
        holder.mContent.isFocusable = true
        holder.mContent.setTextIsSelectable(true)
        // handle links
        holder.mContent.movementMethod = bbsLinkMovementMethod(object : OnLinkClickedListener {
            override fun onLinkClicked(url: String): Boolean {
                print("Click link on adapter : ${url}")
                if (onLinkClickedListener != null) {
                    if (url.lowercase().startsWith("http://") || url.lowercase().startsWith("https://")) {
                        onLinkClickedListener!!.onLinkClicked(url)
                        return true
                    } else {
                        return false
                    }
                } else {
                    return false
                }
            }
        })

        // some discuz may return a null dbdateline fields
        holder.mPublishDate.text = getLocalePastTimeString(context, post.publishAt)
        holder.mThreadType.text = context.getString(R.string.post_index_number, post.position)
        var avatar_num = post.authorId % 16
        if (avatar_num < 0) {
            avatar_num = -avatar_num
        }
        val avatarResource = context.resources.getIdentifier(String.format("avatar_%s", avatar_num + 1), "drawable", context.packageName)
        val factory = OkHttpUrlLoader.Factory(client)
        Glide.get(context).registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
        val source = URLUtils.getSmallAvatarUrlByUid(post.authorId)
        val options = RequestOptions()
                .placeholder(ContextCompat.getDrawable(context, avatarResource))
                .error(ContextCompat.getDrawable(context, avatarResource))
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
        holder.mAvatarImageview.setOnClickListener {
            val intent = Intent(context, UserProfileActivity::class.java)
            intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo)
            intent.putExtra(ConstUtils.PASS_BBS_USER_KEY, user)
            intent.putExtra("UID", post.authorId)
            val options = ActivityOptions
                    .makeSceneTransitionAnimation(context as Activity, holder.mAvatarImageview, "user_info_avatar")
            val bundle = options.toBundle()
            context.startActivity(intent, bundle)
        }
        holder.mReplyBtn.setOnClickListener { replyListener!!.replyToSomeOne(post) }

        // advance option
        if (bbsInfo.apiVersion > 4) {
            holder.mPostAdvanceOptionImageView.visibility = View.VISIBLE
            // bind option
            holder.mPostAdvanceOptionImageView.setOnClickListener { v: View? -> showPopupMenu(holder.mPostAdvanceOptionImageView, post) }
        } else {
            holder.mPostAdvanceOptionImageView.visibility = View.GONE
        }
        if (!post.allAttachments.isEmpty()) {
            val attachmentAdapter = AttachmentAdapter()
            attachmentAdapter.setAttachmentInfoList(post.allAttachments)
            holder.mRecyclerview.isNestedScrollingEnabled = false
            holder.mRecyclerview.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            holder.mRecyclerview.adapter = attachmentAdapter
        } else {
            val attachmentAdapter = AttachmentAdapter()
            attachmentAdapter.setAttachmentInfoList(post.allAttachments)
            holder.mRecyclerview.isNestedScrollingEnabled = false
            holder.mRecyclerview.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            holder.mRecyclerview.adapter = attachmentAdapter
        }
        registerListener()
        if (viewThreadQueryStatus.authorId == -1) {
            // no author is filtered
            holder.mFilterByAuthorIdBtn.text = context.getString(R.string.bbs_post_only_see_him)
            holder.mFilterByAuthorIdBtn.setOnClickListener { mListener!!.setAuthorId(post.authorId) }
        } else {
            holder.mFilterByAuthorIdBtn.text = context.getString(R.string.bbs_post_see_all)
            holder.mFilterByAuthorIdBtn.setOnClickListener { // set it back
                mListener!!.setAuthorId(-1)
            }
        }

        // check with tid
        val pidString = post.pid.toString()
        if (postCommentList.containsKey(pidString)) {
            holder.commentRecyclerview.visibility = View.VISIBLE
            holder.commentRecyclerview.layoutManager = LinearLayoutManager(context)
            val commentAdapter = CommentAdapter()
            val comments = postCommentList.getOrDefault(pidString, ArrayList())
            commentAdapter.commentList = comments
            Log.d(TAG, "Get comments size " + comments.size)
            holder.mRecyclerview.isNestedScrollingEnabled = false
            holder.commentRecyclerview.adapter = commentAdapter
        } else {
            holder.commentRecyclerview.visibility = View.GONE
        }
        // admin post interface
        if(context is OnPostAdmined){
            val onPostAdmined: OnPostAdmined = context as OnPostAdmined
            holder.postCard.setOnLongClickListener { it ->
                onPostAdmined.adminPost(post)
                true
            }
        }
    }

    fun mergeCommentMap(commentList: Map<String, List<ThreadResult.Comment>>?) {
        if (commentList != null) {
            postCommentList.putAll(commentList)
            commentList.forEach(
                    BiConsumer { key: String, value: List<ThreadResult.Comment> ->
                        Log.d(TAG, "get comment key $key value $value")
                        val pid = key.toInt()
                        // this.postCommentList.put(key,value);
                        // find adapter and change it
                        for (i in postList.indices) {
                            val post = postList[i]
                            if (post.pid == pid) {
                                notifyItemChanged(i)
                                break
                            }
                        }
                    }
            )
        }
    }

    fun showPopupMenu(view: View, post: Post) {
        if (onAdvanceOptionClickedListener == null) {
            return
        }
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.post_option)
        // listen to this
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.getItemId()) {
                R.id.action_report_post -> {
                    onAdvanceOptionClickedListener!!.reportPost(post)
                    return@setOnMenuItemClickListener true
                }
            }
            false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true)
        }
        popupMenu.show()
    }

    private fun registerListener() {
        mListener = if (context is onFilterChanged) {
            context as onFilterChanged
        } else {
            throw RuntimeException(context.toString()
                    + " must implement onFilterChanged")
        }
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mThreadPublisher: TextView = itemView.findViewById(R.id.bbs_post_publisher)
        var mPublishDate: TextView = itemView.findViewById(R.id.bbs_post_publish_date)
        var mContent: TextView = itemView.findViewById(R.id.bbs_thread_content)
        var mThreadType: TextView = itemView.findViewById(R.id.bbs_thread_type)
        var mAvatarImageview: ImageView = itemView.findViewById(R.id.bbs_post_avatar_imageView)
        var mRecyclerview: RecyclerView = itemView.findViewById(R.id.bbs_thread_attachment_recyclerview)
        var mReplyBtn: Button = itemView.findViewById(R.id.bbs_thread_reply_button)
        var mFilterByAuthorIdBtn: Button = itemView.findViewById(R.id.bbs_thread_only_see_him_button)
        var mPostStatusMobileIcon: ImageView = itemView.findViewById(R.id.bbs_post_status_mobile)
        var mPostStatusBlockedView: View = itemView.findViewById(R.id.bbs_post_status_blocked_layout)
        var mPostStatusWarnedView: View = itemView.findViewById(R.id.bbs_post_status_warned_layout)
        var mPostStatusEditedView: View = itemView.findViewById(R.id.bbs_post_status_edited_layout)
        var isAuthorLabel: TextView = itemView.findViewById(R.id.bbs_post_is_author)
        var mPostQuoteContent: TextView = itemView.findViewById(R.id.bbs_thread_quote_content)
        var mPostAdvanceOptionImageView: ImageView = itemView.findViewById(R.id.bbs_post_advance_option)
        var commentRecyclerview: RecyclerView = itemView.findViewById(R.id.comment_recyclerview)
        var postCard : CardView = itemView.findViewById(R.id.post_card)
    }

    interface onFilterChanged {
        fun setAuthorId(authorId: Int)
    }


    @SuppressLint("RestrictedApi")
    class PostDrawableWrapper(var drawable: Drawable?) : DrawableWrapper(drawable) {

        override fun draw(canvas: Canvas) {
            if (drawable != null) drawable!!.draw(canvas)
        }
    }



    internal inner class PostImageGetter(var textView: TextView) : ImageGetter {
        override fun getDrawable(source: String): Drawable {
            val drawable = ContextCompat.getDrawable(context, R.drawable.vector_drawable_image_wider_placeholder_stroke)
            val myDrawable = PostDrawableWrapper(drawable)
            val currentDrawable = DrawableTarget(myDrawable, textView)

            // Glide configuration
            client = NetworkUtils.getPreferredClient(context)
            val factory = OkHttpUrlLoader.Factory(client)
            Glide.get(context)
                    .registry
                    .replace(GlideUrl::class.java, InputStream::class.java, factory)

            // check if there are multiple drawable target
            if (urlDrawableMapper.containsKey(source)) {
                val targetList = urlDrawableMapper[source]
                targetList!!.add(currentDrawable)
                urlDrawableMapper[source] = targetList
            } else {
                val targetList: MutableList<DrawableTarget> = ArrayList()
                targetList.add(currentDrawable)
                urlDrawableMapper[source] = targetList
            }

            // glide load
            val glideUrl = GlideUrl(source,
                    LazyHeaders.Builder().addHeader("referer", bbsInfo.base_url).build()
            )
            if (NetworkUtils.canDownloadImageOrFile(context)) {

                Glide.with(context)
                        .load(glideUrl)
                        .error(R.drawable.vector_drawable_image_crash)
                        .placeholder(R.drawable.ic_loading_picture)
                        // refresh it manually
                        .listener(object : RequestListener<Drawable?> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable?>?, isFirstResource: Boolean): Boolean {
                                textView.text = textView.text
                                textView.invalidate()
                                return false
                            }

                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable?>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                textView.text = textView.text
                                textView.invalidate()
                                return false
                            }

                        })
                        .into(currentDrawable)
            }
            else {
                Glide.with(context)
                        .load(glideUrl)
                        .error(R.drawable.vector_drawable_image_wider_placeholder)
                        .placeholder(R.drawable.ic_loading_picture)
                        .onlyRetrieveFromCache(true)
                        // refresh it manually
                        .listener(object : RequestListener<Drawable?> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable?>?, isFirstResource: Boolean): Boolean {
                                textView.text = textView.text
                                textView.invalidate()
                                return false
                            }

                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable?>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                textView.text = textView.text
                                textView.invalidate()
                                return false
                            }

                        })
                        .into(currentDrawable)
            }
            return currentDrawable.myDrawable
        }
    }

    var urlDrawableMapper: MutableMap<String, MutableList<DrawableTarget>> = HashMap()


    inner class DrawableTarget(public val myDrawable: PostDrawableWrapper, val textView: TextView) : CustomTarget<Drawable?>() {



        override fun onLoadFailed(errorDrawable: Drawable?) {
            super.onLoadFailed(errorDrawable)

            // errorDrawable = context.getDrawable(R.drawable.vector_drawable_image_placeholder);
            val width = errorDrawable!!.intrinsicWidth
            val height = errorDrawable.intrinsicHeight

            val screenWidth = textView.width - textView.paddingLeft - textView.paddingRight
            var rescaleFactor = screenWidth.toDouble() / width
            if (rescaleFactor <= 0) {
                rescaleFactor = 1.0
            }
            val newHeight = (height * rescaleFactor).toInt()
            myDrawable.setBounds(0, 0, screenWidth, newHeight)
            errorDrawable.setBounds(0, 0, screenWidth, newHeight)

            myDrawable.drawable = errorDrawable


            textView.invalidate()



        }

        fun invalidateDrawable(drawable: Drawable){
            myDrawable.drawable = drawable
            textView.text = textView.text
            textView.invalidate()
        }

        fun getDrawableToBitmap(drawable: Drawable): Bitmap {

            // 获取 drawable 长宽
            val width = drawable.intrinsicWidth
            val heigh = drawable.intrinsicHeight
            drawable.setBounds(0, 0, width, heigh)

            // 获取drawable的颜色格式
            val config = if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
            // 创建bitmap
            val bitmap = Bitmap.createBitmap(width, heigh, config)
            // 创建bitmap画布
            val canvas = Canvas(bitmap)
            // 将drawable 内容画到画布中
            drawable.draw(canvas)
            return bitmap
        }


        override fun onLoadCleared(placeholder: Drawable?) {
            if(placeholder != null){
                val width = placeholder.intrinsicWidth
                val height = placeholder.intrinsicHeight
                myDrawable.setBounds(0, 0, width, height)
                myDrawable.drawable = placeholder
                textView.text = textView.text
                textView.invalidate()
            }



        }

        override fun onLoadStarted(placeholder: Drawable?) {
            if(placeholder != null) {
                invalidateDrawable(placeholder)
            }
            super.onLoadStarted(placeholder)
        }


        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable?>?) {

            val width = resource.intrinsicWidth
            val height = resource.intrinsicHeight
            val DRAWABLE_COMPRESS_THRESHOLD = 250000
            val DRAWABLE_SIMLEY_THRESHOLD = 10000
            // Rescale to image
            var drawable = resource
            val screenWidth = textView.measuredWidth
            Log.d(TAG,"The image is loaded "+width+" height "+height)

            if (screenWidth != 0 && width * height > DRAWABLE_SIMLEY_THRESHOLD) {
                // this is not the simley and the size should be resized to screen width
                val rescaleFactor = (screenWidth.toDouble()) / width
                val newHeight = (height * rescaleFactor).toInt()
                if (width * height > DRAWABLE_COMPRESS_THRESHOLD) {
                    // this is the big image and should be compressed
                    // compress it for swift display
                    var bitmap = getDrawableToBitmap(resource)
                    // scale it first
                    bitmap = Bitmap.createScaledBitmap(bitmap, screenWidth, newHeight, true)
                    val out = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    val compressedBitmap = BitmapFactory.decodeStream(ByteArrayInputStream(out.toByteArray()))
                    val compressedResource = BitmapDrawable(context.resources, compressedBitmap)
                    drawable = compressedResource
                }
                myDrawable.setBounds(0, 0, screenWidth, newHeight)
                resource.setBounds(0, 0, screenWidth, newHeight)
                myDrawable.drawable = resource
            }
            else {
                myDrawable.setBounds(0, 0, width, height)
                resource.setBounds(0, 0, width, height)
                myDrawable.drawable = resource
            }

            textView.text = textView.text
            textView.invalidate()


        }
    }

    // make image clickable
    inner class HtmlTagHandler(val context: Context, val textView: TextView) : TagHandler {

        override fun handleTag(opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader) {

            // 处理标签<img>
            if (tag.equals("img", ignoreCase = true)) {
                // 获取长度
                val len = output.length
                // 获取图片地址
                val images = output.getSpans(len - 1, len, ImageSpan::class.java)
                val imgURL = images[0].source

                // 使图片可点击并监听点击事件
                if(imgURL != null){
                    output.setSpan(ClickableImage(context, imgURL), len - 1, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

            } else if (tag.equals("del", ignoreCase = true)) {
                var startTag = 0
                var endTag = 0
                if (opening) {
                    startTag = output.length
                } else {
                    endTag = output.length
                    output.setSpan(StrikethroughSpan(), startTag, endTag, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        private inner class ClickableImage(private val context: Context, private val url: String) : ClickableSpan() {
            private var isLoading = false
            override fun onClick(widget: View) {
                // 进行图片点击之后的处理
                if (isLoading) {
                    return
                }
                isLoading = true
                client = NetworkUtils.getPreferredClient(context)
                val factory = OkHttpUrlLoader.Factory(client)

                Glide.get(context)
                        .registry
                        .replace(GlideUrl::class.java, InputStream::class.java, factory)
                // need to judge whether the image is cached or not
                // find from imageGetter!
                if (urlDrawableMapper.containsKey(url) && urlDrawableMapper[url] != null) {
                    val drawableTargetList: List<DrawableTarget>? = urlDrawableMapper[url]
                    // update all target
                    val glideUrl = GlideUrl(url,
                            LazyHeaders.Builder().addHeader("referer", bbsInfo.base_url).build()
                    )
                    val firstDrawableTarget = drawableTargetList!![0]
                    Glide.with(context)
                            .load(glideUrl)
                            .error(R.drawable.vector_drawable_image_failed)
                            .placeholder(R.drawable.vector_drawable_loading_image)
                            .onlyRetrieveFromCache(true)
                            .listener(object : RequestListener<Drawable?> {
                                override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable?>, isFirstResource: Boolean): Boolean {
                                    isLoading = false
                                    val handler = Handler(Looper.getMainLooper())
                                    // update all drawable target
                                    for (drawTarget in drawableTargetList) {
                                        handler.post {
                                            val glideUrl = GlideUrl(url,
                                                    LazyHeaders.Builder().addHeader("referer", bbsInfo.base_url).build()
                                            )
                                            Glide.with(context)
                                                    .load(glideUrl)
                                                    .error(R.drawable.vector_drawable_image_failed)
                                                    .placeholder(R.drawable.vector_drawable_loading_image)
                                                    .listener(object : RequestListener<Drawable> {
                                                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                                            textView.invalidate()
                                                            return false
                                                        }

                                                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                                            textView.invalidate()
                                                            return false
                                                        }

                                                    })
                                                    .into(drawTarget)
                                        }

                                    }

                                    return false
                                }

                                override fun onResourceReady(resource: Drawable?, model: Any, target: Target<Drawable?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                                    isLoading = false
                                    val intent = Intent(context, FullImageActivity::class.java)
                                    intent.putExtra("URL", url)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    context.startActivity(intent)
                                    return false
                                }
                            })
                            .into(firstDrawableTarget)
//                    drawableTargetList?.forEach { drawableTarget ->
//
//                    }

                }
//                else {
//                    val glideUrl = GlideUrl(url,
//                            LazyHeaders.Builder().addHeader("referer", bbsInfo.base_url).build()
//                    )
//                    Glide.with(context)
//                            .load(glideUrl)
//                            .error(R.drawable.vector_drawable_image_failed)
//                            .placeholder(R.drawable.vector_drawable_loading_image)
//                            .onlyRetrieveFromCache(true)
//                            .listener(object : RequestListener<Drawable> {
//                                override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
//                                    // load from network
//                                    isLoading = false
//                                    val glideUrl = GlideUrl(url,
//                                            LazyHeaders.Builder().addHeader("referer", bbsInfo.base_url).build()
//                                    )
//                                    Glide.with(context)
//                                            .load(glideUrl)
//                                            .error(R.drawable.vector_drawable_image_failed)
//                                            .placeholder(R.drawable.vector_drawable_loading_image)
//                                            .into(target)
//                                    return false
//                                }
//
//                                override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
//                                    isLoading = false
//                                    val intent = Intent(context, FullImageActivity::class.java)
//                                    intent.putExtra("URL", url)
//                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                                    context.startActivity(intent)
//                                    //textView.invalidateDrawable(resource);
//                                    return false
//                                }
//                            }).into(DrawableTarget(myDrawable = ,textView))
//                }
            }
        }

    }

    interface onAdapterReply {
        fun replyToSomeOne(post : Post)
    }

    interface OnLinkClicked {
        fun onLinkClicked(url: String)
    }

    interface OnAdvanceOptionClicked {
        fun reportPost(post: Post)
    }

    companion object {
        private val TAG = PostAdapter::class.java.simpleName
    }

    init {

        this.viewThreadQueryStatus = viewThreadQueryStatus
        setHasStableIds(true)
    }
}