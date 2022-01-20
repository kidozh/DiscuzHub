package com.kidozh.discuzhub.utilities

import java.io.UnsupportedEncodingException
import java.net.URLEncoder

object CharsetUtils {
    @JvmStatic
    fun EncodeStringByCharset(str: String, charset: String?): String {
        return try {
            URLEncoder.encode(str, charset)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            str
        }
    }
}