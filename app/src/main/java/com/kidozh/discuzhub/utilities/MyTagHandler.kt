package com.kidozh.discuzhub.utilities

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Handler
import android.text.Editable
import android.text.Html.TagHandler
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.activities.FullImageActivity
import com.kidozh.discuzhub.utilities.NetworkUtils.getPreferredClient
import org.xml.sax.XMLReader
import java.io.InputStream
import java.util.*

class MyTagHandler(private val mContext: Context, var textView: TextView, var rootView: View) :
    TagHandler {
    override fun handleTag(opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader) {

        // 处理标签<img>
        if (tag.lowercase(Locale.getDefault()) == "img") {
            // 获取长度
            val len = output.length
            // 获取图片地址
            val images = output.getSpans(len - 1, len, ImageSpan::class.java)
            val imgURL = images[0].source

            // 使图片可点击并监听点击事件
            //Log.d(TAG,"set Onclick span "+imgURL+" length "+len);
            output.setSpan(
                imgURL?.let { ClickableImage(mContext, it) },
                len - 1,
                len,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private inner class ClickableImage(private val context: Context, private val url: String) :
        ClickableSpan() {
        override fun onClick(widget: View) {
            // 进行图片点击之后的处理 usually textview
            //Log.d(TAG,"You pressed image "+widget.toString()+" URL "+url);
            val client = getPreferredClient(mContext)
            val factory = OkHttpUrlLoader.Factory(client)
            Glide.get(mContext)
                .registry
                .replace(GlideUrl::class.java, InputStream::class.java, factory)
            val myDrawable = MyDrawableWrapper()
            Glide.with(context)
                .load(url)
                .error(R.drawable.vector_drawable_image_failed)
                .placeholder(R.drawable.vector_drawable_loading_image)
                .into(GlideImageTarget(context, myDrawable, textView, rootView))
            Glide.with(mContext)
                .load(url)
                .onlyRetrieveFromCache(true)
                .error(R.drawable.vector_drawable_image_failed)
                .placeholder(R.drawable.vector_drawable_loading_image)
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        //Log.d(TAG,"The resource is not loaded...");
                        val mainHandler = Handler(context.mainLooper)
                        val myRunnable = Runnable {
                            Glide.with(context)
                                .load(url)
                                .error(R.drawable.vector_drawable_image_failed)
                                .placeholder(R.drawable.vector_drawable_loading_image)
                                .into(GlideImageTarget(context, myDrawable, textView, rootView))
                        }
                        mainHandler.post(myRunnable)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any,
                        target: Target<Drawable?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        //Log.d(TAG,"The resource is loaded and ready to open in external activity...");
                        val intent = Intent(mContext, FullImageActivity::class.java)
                        intent.putExtra("URL", url)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        mContext.startActivity(intent)
                        return false
                    }
                }).into(GlideImageTarget(context, myDrawable, textView, rootView))


//            Handler mHandler = new Handler(Looper.getMainLooper());
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            });
        }
    }

    companion object {
        private val TAG = MyTagHandler::class.java.simpleName
    }
}