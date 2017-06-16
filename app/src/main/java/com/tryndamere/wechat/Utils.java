package com.tryndamere.wechat;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.google.android.gms.common.api.Status.st;

/**
 * Created by Administrar on 2017/6/16 0016.
 */

public class Utils {
    public static  String toUTF(String info){
        String result = "";
        try {
            result =  URLEncoder.encode("男", "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return  result;
    }
}
