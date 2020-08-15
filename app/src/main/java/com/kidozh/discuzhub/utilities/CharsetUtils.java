package com.kidozh.discuzhub.utilities;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class CharsetUtils {
    public static String EncodeStringByCharset(String str,String charset){
        try{
            return URLEncoder.encode(str,charset);
        }
        catch (UnsupportedEncodingException e){
            e.printStackTrace();
            return str;
        }
    }
}
