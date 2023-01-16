package com.kidozh.discuzhub.utilities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Html.ImageGetter
import android.text.Html.TagHandler
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.text.style.StrikethroughSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.FullImageActivity
import com.kidozh.discuzhub.entities.User
import com.kidozh.discuzhub.utilities.GlideImageGetter
import com.kidozh.discuzhub.utilities.NetworkUtils.canDownloadImageOrFile
import com.kidozh.discuzhub.utilities.NetworkUtils.getPreferredClient
import com.kidozh.discuzhub.utilities.NetworkUtils.getPreferredClientWithCookieJarByUserWithDefaultHeader
import okhttp3.OkHttpClient
import org.xml.sax.XMLReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

class GlideImageGetter(textView: TextView, userBriefInfo: User?) : ImageGetter {
    private val textView: TextView
    private val context: Context
    private val userBriefInfo: User?

    init {
        context = textView.context
        this.textView = textView
        this.userBriefInfo = userBriefInfo
    }

    override fun getDrawable(source: String): Drawable {
        val drawable =
            context.getDrawable(R.drawable.vector_drawable_image_wider_placeholder_stroke)
        val glideDrawableWrapper = GlideDrawableWrapper(drawable)
        // myDrawable.setDrawable(drawable);
        client = getPreferredClientWithCookieJarByUserWithDefaultHeader(context, userBriefInfo)
        val factory = OkHttpUrlLoader.Factory(client)
        Glide.get(context)
            .registry
            .replace(GlideUrl::class.java, InputStream::class.java, factory)
        val currentDrawable = GlideDrawableTarget(glideDrawableWrapper, textView)
        // put image source in the key
        if (urlDrawableMapper.containsKey(source)) {
            val targetList = urlDrawableMapper[source]
            targetList!!.add(currentDrawable)
            urlDrawableMapper[source] = targetList
        } else {
            val targetList: MutableList<GlideDrawableTarget> = ArrayList()
            targetList.add(currentDrawable)
            urlDrawableMapper[source] = targetList
        }
        // judge network status
        if (canDownloadImageOrFile(context)) {
            Log.d(TAG, "load the picture from network $source")
            val glideUrl = GlideUrl(source)
            Glide.with(context)
                .load(glideUrl)
                .error(R.drawable.vector_drawable_image_crash)
                .placeholder(R.drawable.ic_loading_picture)
                .into(currentDrawable)
        } else {
            Log.d(TAG, "load the picture from cache $source")
            val glideUrl = GlideUrl(source)
            Glide.with(context)
                .load(glideUrl)
                .error(R.drawable.vector_drawable_image_wider_placeholder)
                .placeholder(R.drawable.ic_loading_picture)
                .onlyRetrieveFromCache(true)
                .into(currentDrawable)
        }
        return glideDrawableWrapper
    }

    @SuppressLint("RestrictedApi")
    class GlideDrawableWrapper(drawable: Drawable?) : DrawableWrapper(drawable) {
        private var drawable: Drawable? = null
        override fun draw(canvas: Canvas) {
            if (drawable != null) drawable!!.draw(canvas)
        }

        override fun getDrawable(): Drawable? {
            return drawable
        }

        override fun setDrawable(drawable: Drawable?) {
            this.drawable = drawable
        }
    }

    inner class GlideDrawableTarget(
        private val myDrawable: GlideDrawableWrapper,
        var textView: TextView
    ) : CustomTarget<Drawable?>() {
        override fun onLoadFailed(errorDrawable: Drawable?) {
            super.onLoadFailed(errorDrawable)
            val width = errorDrawable!!.intrinsicWidth
            val height = errorDrawable.intrinsicHeight
            Log.d(TAG, "Unable to get the image $errorDrawable W $width H $height")
            myDrawable.setBounds(0, 0, width, height)
            errorDrawable.setBounds(0, 0, width, height)
            myDrawable.drawable = errorDrawable
            textView.text = textView.text
            textView.invalidate()
        }

        fun DrawableToBitmap(drawable: Drawable): Bitmap {

            // 获取 drawable 长宽
            val width = drawable.intrinsicWidth
            val heigh = drawable.intrinsicHeight
            drawable.setBounds(0, 0, width, heigh)

            // 获取drawable的颜色格式
            val config =
                if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
            // 创建bitmap
            val bitmap = Bitmap.createBitmap(width, heigh, config)
            // 创建bitmap画布
            val canvas = Canvas(bitmap)
            // 将drawable 内容画到画布中
            drawable.draw(canvas)
            return bitmap
        }

        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable?>?) {

            //获取原图大小
            textView.post {
                var drawable = resource
                val width = drawable.intrinsicWidth
                val height = drawable.intrinsicHeight
                val DRAWABLE_COMPRESS_THRESHOLD = 250000
                val DRAWABLE_SIMLEY_THRESHOLD = 10000
                // Rescale to image
                val screenWidth = textView.measuredWidth
                Log.d(TAG, "Screen width $screenWidth image width $width")
                if (screenWidth != 0 && width * height > DRAWABLE_SIMLEY_THRESHOLD) {
                    val rescaleFactor = screenWidth.toDouble() / width
                    val newHeight = (height * rescaleFactor).toInt()
                    Log.d(TAG, "rescaleFactor $rescaleFactor image new height $newHeight")
                    if (width * height > DRAWABLE_COMPRESS_THRESHOLD) {
                        // compress it for swift display
                        var bitmap = DrawableToBitmap(drawable)
                        // scale it first
                        bitmap = Bitmap.createScaledBitmap(bitmap, screenWidth, newHeight, true)
                        val out = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                        val compressedBitmap =
                            BitmapFactory.decodeStream(ByteArrayInputStream(out.toByteArray()))
                        drawable = BitmapDrawable(context.resources, compressedBitmap)
                    }
                    myDrawable.setBounds(0, 0, screenWidth, newHeight)
                    drawable.setBounds(0, 0, screenWidth, newHeight)
                    resource.setBounds(0, 0, screenWidth, newHeight)
                } else if (screenWidth == 0) {
                    Log.d(TAG, "Get textview width : 0")
                    myDrawable.setBounds(0, 0, width, height)
                    drawable.setBounds(0, 0, width, height)
                    resource.setBounds(0, 0, width, height)
                } else {
                    myDrawable.setBounds(0, 0, width * 2, height * 2)
                    drawable.setBounds(0, 0, width * 2, height * 2)
                    resource.setBounds(0, 0, width * 2, height * 2)
                }
                myDrawable.drawable = drawable
                val tv = textView
                tv.text = tv.text
                tv.invalidate()
            }
        }

        override fun onLoadCleared(placeholder: Drawable?) {}
    }

    class HtmlTagHandler(context: Context, textView: TextView) : TagHandler {
        private val mContext: Context
        var textView: TextView

        init {
            mContext = context.applicationContext
            this.textView = textView
        }

        override fun handleTag(
            opening: Boolean,
            tag: String,
            output: Editable,
            xmlReader: XMLReader
        ) {

            // 处理标签<img>
            if (tag.equals("img", ignoreCase = true)) {
                // 获取长度
                val len = output.length
                // 获取图片地址
                val images = output.getSpans(len - 1, len, ImageSpan::class.java)
                val imgURL = images[0].source

                // 使图片可点击并监听点击事件
                output.setSpan(
                    imgURL?.let { ClickableImage(mContext, it) },
                    len - 1,
                    len,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else if (tag.equals("del", ignoreCase = true)) {
                var startTag = 0
                var endTag = 0
                if (opening) {
                    startTag = output.length
                } else {
                    endTag = output.length
                    output.setSpan(
                        StrikethroughSpan(),
                        startTag,
                        endTag,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }

        private inner class ClickableImage     //private MyDrawableWrapper myDrawable;
            (private val context: Context, private val url: String) : ClickableSpan() {
            private val isLoaded = false
            private var isLoading = false
            override fun onClick(widget: View) {
                // 进行图片点击之后的处理
                if (isLoading) {
                    return
                }
                isLoading = true
                Log.d(TAG, "You pressed image URL $url")
                client = getPreferredClient(mContext)
                val factory = OkHttpUrlLoader.Factory(client)
                Glide.get(mContext)
                    .registry
                    .replace(GlideUrl::class.java, InputStream::class.java, factory)
                // need to judge whether the image is cached or not
                // find from imageGetter!
                Log.d(TAG, "You press the image ")
                if (urlDrawableMapper.containsKey(url) && urlDrawableMapper[url] != null) {
                    val drawableTargetList: List<GlideDrawableTarget>? = urlDrawableMapper[url]
                    // update all target
                    val glideUrl = GlideUrl(url)
                    Glide.with(mContext)
                        .load(glideUrl)
                        .error(R.drawable.vector_drawable_image_failed)
                        .placeholder(R.drawable.vector_drawable_loading_image)
                        .onlyRetrieveFromCache(true)
                        .listener(object : RequestListener<Drawable?> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any,
                                target: Target<Drawable?>,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.d(TAG, "Can't find the image! ")
                                isLoading = false
                                val handler = Handler(Looper.getMainLooper())
                                // update all drawable target
                                for (drawTarget in drawableTargetList!!) {
                                    handler.post {
                                        val glideUrl = GlideUrl(url)
                                        Glide.with(mContext)
                                            .load(glideUrl)
                                            .error(R.drawable.vector_drawable_image_failed)
                                            .placeholder(R.drawable.vector_drawable_loading_image)
                                            .into(drawTarget)
                                    }
                                }
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any,
                                target: Target<Drawable?>,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.d(TAG, "Find the image! Goes to other activity")
                                isLoading = false
                                val intent = Intent(mContext, FullImageActivity::class.java)
                                intent.putExtra("URL", url)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                mContext.startActivity(intent)
                                return false
                            }
                        })
                        .submit()
                } else {
                    Log.d(TAG, "Can not find the drawable via URL $url")
                }
            }
        }
    }

    companion object {
        private val TAG = GlideImageGetter::class.java.simpleName
        private var client = OkHttpClient()
        var urlDrawableMapper: MutableMap<String, MutableList<GlideDrawableTarget>?> = HashMap()
    }
}