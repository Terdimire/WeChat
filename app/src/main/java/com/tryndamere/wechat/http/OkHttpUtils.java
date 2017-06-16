package com.tryndamere.wechat.http;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * description
 * Created by 张芸艳 on 2017/5/31.
 */

public class OkHttpUtils {
    private static final int SUCCESS_CODE = 1;
    private static final int ERROR_CODE = 2;
    private final OkHttpClient mOkHttpClient;
    private final Handler mHandler;
    private final Gson mGson;
    private volatile static OkHttpUtils mOkHttpUtils;
    private ImageUtils imageUtils;

    //私有构造，创建handler，client，gson实例
    private OkHttpUtils() {
//        ClearableCookieJar cookieJar =
//                new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(IApplication.application));
//        //  cookieJar.c
        mOkHttpClient = new OkHttpClient().newBuilder()
                .cookieJar(new CookiesManager())
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .build();
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case SUCCESS_CODE:
                        ((HandlerData) msg.obj).success();
                        break;
                    case ERROR_CODE:
                        ((HandlerData) msg.obj).error();
                        break;
                }
            }
        };
        mGson = new Gson();
        if (imageUtils == null) {
            imageUtils = new ImageUtils();
        }

    }

    //回调网络请求处理结果的接口
    public interface HttpCallBack<T> {
        void onSuccess(T t);

        void onFailure(IOException e);
    }

    //回调接口
    class HandlerData<T> {
        HttpCallBack<T> httpCallBack;
        T t;
        IOException e;

        public void success() {
            if (httpCallBack != null) {
                httpCallBack.onSuccess(t);
            }
        }

        public void error() {
            if (httpCallBack != null) {
                httpCallBack.onFailure(e);
            }
        }
    }

    //单例模式
    public static OkHttpUtils getInstance() {
        if (mOkHttpUtils == null) {
            synchronized (OkHttpUtils.class) {
                if (mOkHttpUtils == null) {
                    mOkHttpUtils = new OkHttpUtils();
                }
            }
        }
        return mOkHttpUtils;
    }

    //处理图片
    private class ImageUtils {
        /**
         * 根据InputStream获取图片实际的宽度和高度
         *
         * @param imageStream
         * @return
         */
        public ImageSize getImageSize(InputStream imageStream) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(imageStream, null, options);
            return new ImageSize(options.outWidth, options.outHeight);
        }

        public class ImageSize {
            int width;
            int height;

            public ImageSize() {
            }

            public ImageSize(int width, int height) {
                this.width = width;
                this.height = height;
            }

            @Override
            public String toString() {
                return "ImageSize{" +
                        "width=" + width +
                        ", height=" + height +
                        '}';
            }
        }

        public int calculateInSampleSize(ImageSize srcSize, ImageSize targetSize) {
            // 源图片的宽度
            int width = srcSize.width;
            int height = srcSize.height;
            int inSampleSize = 1;

            int reqWidth = targetSize.width;
            int reqHeight = targetSize.height;

            if (width > reqWidth && height > reqHeight) {
                // 计算出实际宽度和目标宽度的比率
                int widthRatio = Math.round((float) width / (float) reqWidth);
                int heightRatio = Math.round((float) height / (float) reqHeight);
                inSampleSize = Math.max(widthRatio, heightRatio);
            }
            return inSampleSize;
        }

        /**
         * 根据ImageView获适当的压缩的宽和高
         *
         * @param view
         * @return
         */
        public ImageSize getImageViewSize(View view) {

            ImageSize imageSize = new ImageSize();

            imageSize.width = getExpectWidth(view);
            imageSize.height = getExpectHeight(view);

            return imageSize;
        }

        /**
         * 根据view获得期望的高度
         *
         * @param view
         * @return
         */
        private int getExpectHeight(View view) {

            int height = 0;
            if (view == null) return 0;

            final ViewGroup.LayoutParams params = view.getLayoutParams();
            //如果是WRAP_CONTENT，此时图片还没加载，getWidth根本无效
            if (params != null && params.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                height = view.getWidth(); // 获得实际的宽度
            }
            if (height <= 0 && params != null) {
                height = params.height; // 获得布局文件中的声明的宽度
            }

            if (height <= 0) {
                height = getImageViewFieldValue(view, "mMaxHeight");// 获得设置的最大的宽度
            }

            //如果宽度还是没有获取到，使用屏幕的宽度
            if (height <= 0) {
                DisplayMetrics displayMetrics = view.getContext().getResources()
                        .getDisplayMetrics();
                height = displayMetrics.heightPixels;
            }

            return height;
        }

        /**
         * 根据view获得期望的宽度
         *
         * @param view
         * @return
         */
        private int getExpectWidth(View view) {
            int width = 0;
            if (view == null) return 0;

            final ViewGroup.LayoutParams params = view.getLayoutParams();
            //如果是WRAP_CONTENT，此时图片还没加载，getWidth根本无效
            if (params != null && params.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
                width = view.getWidth(); // 获得实际的宽度
            }
            if (width <= 0 && params != null) {
                width = params.width; // 获得布局文件中的声明的宽度
            }

            if (width <= 0)

            {
                width = getImageViewFieldValue(view, "mMaxWidth");// 获得设置的最大的宽度
            }
            //如果宽度还是没有获取到，使用屏幕的宽度
            if (width <= 0)

            {
                DisplayMetrics displayMetrics = view.getContext().getResources()
                        .getDisplayMetrics();
                width = displayMetrics.widthPixels;
            }

            return width;
        }

        /**
         * 通过反射获取imageview的某个属性值
         *
         * @param object
         * @param fieldName
         * @return
         */
        private int getImageViewFieldValue(Object object, String fieldName) {
            int value = 0;
            try {
                Field field = ImageView.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                int fieldValue = field.getInt(object);
                if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                    value = fieldValue;
                }
            } catch (Exception e) {
            }
            return value;

        }
    }

//=======================内部用到的工具方法====================

    /**
     * 把map和url拼接到一起的方法
     *
     * @param url 请求的接口
     * @param map 拼接的参数
     * @return string 拼接好的接口
     */
    private String getUrl(String url, Map<String, Object> map) {
        //当map集合为空的时候，直接返回url
        if (map == null || map.size() == 0) {
            return url;
        }
        StringBuffer sb = new StringBuffer();
        for (String key : map.keySet()) {
            sb.append(key + "=" + map.get(key) + "&");
        }
        return url + "?" + sb.substring(0, sb.length() - 1);
    }

    /**
     * handler 消息
     *
     * @param what 消息标识
     * @param obj  消息携带的数据
     * @return Message
     */
    private Message getMessage(int what, Object obj) {
        Message message = Message.obtain();
        message.what = what;
        message.obj = obj;
        return message;
    }

    /**
     * get 用到的Request
     *
     * @param url 接口地址
     * @param map 拼接参数
     * @return Request
     */
    private Request getRequest(String url, Map<String, Object> map) {
        return new Request.Builder()
                .url(getUrl(url, map))
                .build();
    }

    /**
     * post 用到的Request
     *
     * @param url 接口地址
     * @param map 拼接参数
     * @return Request
     */
    private Request PostRequest(String url, Map<String, Object> map) {
        if (map == null || map.size() == 0) {
            return new Request.Builder()
                    .url(url)
                    .build();
        }
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            builder.add(entry.getKey(), (String) entry.getValue());
        }
        RequestBody requestBody = builder.build();
        return new Request.Builder()
                .url(getUrl(url, null))
                .post(requestBody)
                .build();
    }

    //文件类型
    private String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }

    /**
     * 得到post上传时用到的Request
     *
     * @param url      地址
     * @param files    文件
     * @param fileKeys
     * @return Request
     */
    private Request buildMultipartFormRequest(String url, File[] files,
                                              String[] fileKeys) {


        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        if (files != null) {
            RequestBody fileBody = null;
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String fileName = file.getName();
                Log.d("OkHttpUtils", fileName);
                fileBody = RequestBody.create(MediaType.parse(guessMimeType(fileName)), file);
                //根据文件名设置contentType
                builder.addFormDataPart(fileKeys[i], fileName, fileBody);
            }
        }

        RequestBody requestBody = builder.build();
        return new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
    }

    private Request buildMultipartFormRequest(String url, Map<String, Object> map) {


        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        //map没有测试
        if (map != null && map.size() != 0) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String name = ((File) entry.getValue()).getName();
                builder.addFormDataPart(entry.getKey(), name, RequestBody.create(MediaType.parse(guessMimeType(name)), (File) entry.getValue()));

            }
        }

        RequestBody requestBody = builder.build();
        return new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
    }

    /**
     * 请求数据用的,需要bean类
     *
     * @param httpCallBack 接口回调
     * @param classType    bean反射
     * @param request      okhttp3
     * @param <T>          泛型
     */
    private <T> void deliveryResult(final HttpCallBack<T> httpCallBack, final Class<T> classType, Request request) {
        final HandlerData<T> hd = new HandlerData<>();
        hd.httpCallBack = httpCallBack;
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                hd.e = e;
                mHandler.sendMessage(getMessage(ERROR_CODE, hd));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String json = response.body().string();
                    if (json != null) {
                        T t = mGson.fromJson(json, classType);
                        hd.t = t;
                        mHandler.sendMessage(getMessage(SUCCESS_CODE, hd));
                    } else {
                        hd.e = new IOException("没有数据!");
                        mHandler.sendMessage(getMessage(ERROR_CODE, hd));
                    }
                } catch (IOException e) {
                    mHandler.sendMessage(getMessage(ERROR_CODE, hd));
                } catch (com.google.gson.JsonParseException e)//Json解析的错误
                {
                    mHandler.sendMessage(getMessage(ERROR_CODE, hd));
                }
            }
        });

    }


    /**
     * 上传用的,不需要bean类
     *
     * @param httpCallBack 接口回调
     * @param request      okhttp3
     * @param <T>          泛型
     */
    private <T> void deliveryResult(final HttpCallBack<T> httpCallBack, Request request) {
        final HandlerData<T> hd = new HandlerData<>();
        hd.httpCallBack = httpCallBack;
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                hd.e = e;
                mHandler.sendMessage(getMessage(ERROR_CODE, hd));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String json = response.body().string();
                    hd.t = (T) json;
                    boolean ok = response.isSuccessful();
                    if (ok) {
                        mHandler.sendMessage(getMessage(SUCCESS_CODE, hd));
                    } else {
                        mHandler.sendMessage(getMessage(ERROR_CODE, hd));
                    }


                } catch (IOException e) {
                    mHandler.sendMessage(getMessage(ERROR_CODE, hd));
                } catch (com.google.gson.JsonParseException e)//Json解析的错误
                {
                    mHandler.sendMessage(getMessage(ERROR_CODE, hd));
                }
            }
        });

    }

    //文件名
    private String getFileName(String path) {
        int separatorIndex = path.lastIndexOf("/");
        String name = (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
        if (name.contains(":")) {
            name = System.currentTimeMillis()+"" ;
        }
        return name;
    }

    //设置错误图片
    private void setErrorResId(final ImageView view, final int errorResId) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                view.setImageResource(errorResId);
            }
        });
    }

//==================用于内部的请求处理=======================

    /**
     * 同步get，返回response的方法
     * 用于内部
     *
     * @param url
     * @param map
     * @return
     * @throws IOException
     */
    private Response _getSyn(String url, Map<String, Object> map) throws IOException {
        Request request = getRequest(url, map);
        Response response = mOkHttpClient.newCall(request)
                .execute();
        return response;
    }

    /**
     * 同步get，返回实体bean的方法
     * 用于内部
     *
     * @param url
     * @param map
     * @param classType
     * @param <T>
     * @return
     * @throws IOException
     */
    private <T> T _getSynData(String url, Map<String, Object> map, Class<T> classType) throws IOException {
        Response response = _getSyn(url, map);
        String json = response.body().string();
        T t = mGson.fromJson(json, classType);
        return t;
    }

    /**
     * 异步get
     * 用于内部
     *
     * @param url          请求的地址（可以是拼接过得，也可以不是）
     * @param map          用于拼接的参数，之前在url拼接过的可以传入null
     * @param classType    反射，该网络请求的地址中的json对应的bean类
     * @param httpCallBack 接口回调，两个方法，成功和失败
     * @param <T>          泛型
     */
    private <T> void _getAsyn(String url, Map<String, Object> map, final Class<T> classType, HttpCallBack<T> httpCallBack) {

        Request request = getRequest(url, map);
        final HandlerData<T> hd = new HandlerData<>();
        hd.httpCallBack = httpCallBack;
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                hd.e = e;
                mHandler.sendMessage(getMessage(ERROR_CODE, hd));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                try {
                    if (json != null) {
                        T t = mGson.fromJson(json, classType);
                        hd.t = t;
                        mHandler.sendMessage(getMessage(SUCCESS_CODE, hd));
                    } else {
                        hd.e = new IOException("没有数据");
                        mHandler.sendMessage(getMessage(ERROR_CODE, hd));
                    }
                } catch (com.google.gson.JsonParseException e)//Json解析的错误
                {
                    mHandler.sendMessage(getMessage(ERROR_CODE, hd));
                }
            }
        });

    }

    /**
     * 同步post
     * 用于内部
     *
     * @param url 接口地址
     * @param map 拼接参数
     * @return Response
     * @throws IOException
     */
    private Response _postSyn(String url, Map<String, Object> map) throws IOException {
        Request request = PostRequest(url, map);
        Response response = mOkHttpClient.newCall(request).execute();
        return response;
    }

    /**
     * 同步post
     * 用于内部
     *
     * @param url 接口地址
     * @param map 拼接参数
     * @return json串
     * @throws IOException
     */
    private String _postSynString(String url, Map<String, Object> map) throws IOException {
        Response response = _postSyn(url, map);
        return response.body().string();
    }

    /**
     * 异步post
     * 用于内部
     *
     * @param url          接口地址
     * @param classType    bean反射
     * @param map          参数
     * @param httpCallBack 回调接口
     * @param <T>          泛型
     */
    private <T> void _postAsyn(String url, Map<String, Object> map, Class<T> classType, HttpCallBack<T> httpCallBack) {
        Request request = PostRequest(url, map);
        deliveryResult(httpCallBack, classType, request);
    }

    /**
     * 同步post文件上传
     * 用于内部
     *
     * @param url      地址
     * @param files    提交的文件数组
     * @param fileKeys 提交的文件数组key
     * @return Response
     * @throws IOException
     */
    private Response _postUpLoadSyn(String url, File[] files, String[] fileKeys) throws IOException {
        Request request = buildMultipartFormRequest(url, files, fileKeys);
        return mOkHttpClient.newCall(request).execute();
    }

    /**
     * 同步post文件上传
     * 用于内部
     *
     * @param url     地址
     * @param file    提交的文件
     * @param fileKey 提交的文件key
     * @return Response
     * @throws IOException
     */
    private Response _postUpLoadSyn(String url, File file, String fileKey) throws IOException {
        Request request = buildMultipartFormRequest(url, new File[]{file}, new String[]{fileKey});
        return mOkHttpClient.newCall(request).execute();
    }

    /**
     * 同步post文件上传
     * 用于内部
     *
     * @param url 地址
     * @param map 参数
     * @return Response
     * @throws IOException
     */
    private Response _postUpLoadSyn(String url, Map<String, Object> map) throws IOException {
        Request request = buildMultipartFormRequest(url, map);
        return mOkHttpClient.newCall(request).execute();
    }

    /**
     * 异步post文件上传
     * 用于内部
     *
     * @param url          接口地址
     * @param httpCallBack 接口回调
     * @param files        提交的文件数组
     * @param fileKeys     提交的文件数组的key
     * @param <T>          泛型
     * @throws IOException
     */
    private <T> void _postUpLoadAsyn(String url, File[] files, String[] fileKeys, HttpCallBack<T> httpCallBack) {
        Request request = buildMultipartFormRequest(url, files, fileKeys);
        deliveryResult(httpCallBack, request);
    }

    /**
     * 异步post文件上传
     * 用于内部
     *
     * @param url          接口地址
     * @param httpCallBack 接口回调
     * @param <T>          泛型
     * @throws IOException
     */
    private <T> void _postUpLoadAsyn(String url, Map<String, Object> map, HttpCallBack<T> httpCallBack) {
        Request request = buildMultipartFormRequest(url, map);
        deliveryResult(httpCallBack, request);
    }

    /**
     * 异步post文件上传，单文件不带参数上传
     * 用于内部
     *
     * @param url      接口地址
     * @param callback 接口回调
     * @param file     提交的文件
     * @param fileKey  提交的文件的key
     * @param <T>      泛型
     * @throws IOException
     */
    private <T> void _postUpLoadAsyn(String url, HttpCallBack<T> callback, File file, String fileKey) {
        Request request = buildMultipartFormRequest(url, new File[]{file}, new String[]{fileKey});
        deliveryResult(callback, request);
    }


    /**
     * 异步下载文件
     * 用于内部
     *
     * @param url         地址
     * @param destFileDir 本地文件存储的文件夹
     * @param callback    接口回调
     */
    private <T> void _downloadAsyn(final String url, final String destFileDir, final HttpCallBack<T> callback) {
        final Request request = new Request.Builder()
                .url(url)
                .build();
        final Call call = mOkHttpClient.newCall(request);
        final HandlerData<T> hd = new HandlerData<>();
        hd.httpCallBack = callback;
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                hd.e = e;
                mHandler.sendMessage(getMessage(ERROR_CODE, hd));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream();
                    File file = new File(destFileDir, getFileName(url));
                    Log.d("OkHttpUtils", file.getPath());
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    fos = new FileOutputStream(file);
                    long downLen = 0;
                    long total = response.body().contentLength();
                    while ((len = is.read(buf)) != -1) {
                        downLen += len;
                        /**
                         * 进度,此处如果需要progressbar,就写一个进度的接口回调
                         */
                        Log.d("OkHttpUtils", "current:" + downLen + "=======total:" + total);
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                    hd.t = (T) file;
                    mHandler.sendMessage(getMessage(SUCCESS_CODE, hd));
                } catch (IOException e) {
                    hd.e = e;
                    mHandler.sendMessage(getMessage(ERROR_CODE, hd));
                } finally {
                    try {
                        if (is != null) is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null) fos.close();
                    } catch (IOException e) {
                    }
                }

            }
        });

    }

    /**
     * 加载图片
     * 用于内部
     *
     * @param view
     * @param url
     * @throws IOException
     */
    private void _displayImage(final ImageView view, final String url, final int errorResId) {
        final Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                setErrorResId(view, errorResId);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                try {
                    is = response.body().byteStream();
                    ImageUtils.ImageSize actualImageSize = imageUtils.getImageSize(is);
                    ImageUtils.ImageSize imageViewSize = imageUtils.getImageViewSize(view);
                    int inSampleSize = imageUtils.calculateInSampleSize(actualImageSize, imageViewSize);
                    try {
                        is.reset();
                    } catch (IOException e) {
                        response = _getSyn(url, null);
                        is = response.body().byteStream();
                    }

                    BitmapFactory.Options ops = new BitmapFactory.Options();
                    ops.inJustDecodeBounds = false;
                    ops.inSampleSize = inSampleSize;
                    final Bitmap bm = BitmapFactory.decodeStream(is, null, ops);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            view.setImageBitmap(bm);
                        }
                    });
                } catch (Exception e) {
                    setErrorResId(view, errorResId);

                } finally {
                    if (is != null) try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }


    //===========================对外公布的方法=========================

    /**
     * 同步的get请求
     * 用于外部
     *
     * @param url 请求的地址
     * @param map 拼接的参数,允许为null
     * @return Response
     * @throws IOException
     */
    public Response getSyn(String url, Map<String, Object> map) throws IOException {
        return getInstance()._getSyn(url, map);
    }

    /**
     * 同步get
     * 用于外部
     *
     * @param url       请求的地址
     * @param map       拼接的参数,允许为null
     * @param classType bean类的反射
     * @param <T>       泛型
     * @return classType对应的bean类
     * @throws IOException
     */
    public <T> T getSynData(String url, Map<String, Object> map, Class<T> classType) throws IOException {
        return (T) getInstance()._getSynData(url, map, classType);
    }

    /**
     * 异步get
     * 用于外部
     *
     * @param url          请求的地址
     * @param map          拼接的参数,允许为null
     * @param classType    bean类的反射
     * @param httpCallBack 接口回调
     * @param <T>          泛型
     */
    public <T> void getAsyn(String url, Map<String, Object> map, Class<T> classType, HttpCallBack<T> httpCallBack) {
        getInstance()._getAsyn(url, map, classType, httpCallBack);
    }

    /**
     * 同步post 得到Response
     * 用于外部
     *
     * @param url 接口地址
     * @param map 拼接参数
     * @return Response
     * @throws IOException
     */
    public Response postSyn(String url, Map<String, Object> map) throws IOException {
        return getInstance()._postSyn(url, map);
    }

    /**
     * 同步post 得到json串
     * 用于外部
     *
     * @param url 接口地址
     * @param map 拼接参数
     * @return String
     * @throws IOException
     */
    public String postSynString(String url, Map<String, Object> map) throws IOException {
        return getInstance()._postSynString(url, map);
    }

    /**
     * 异步post
     * 用于外部
     *
     * @param url          接口地址
     * @param map          拼接参数
     * @param classType    bean反射
     * @param httpCallBack 回调接口
     * @param <T>          泛型
     */
    public <T> void postAsyn(String url, Map<String, Object> map, Class<T> classType, HttpCallBack<T> httpCallBack) {
        getInstance()._postAsyn(url, map, classType, httpCallBack);
    }

    /**
     * 同步post文件上传,提交文件是数组
     * 用于外部
     *
     * @param url      地址
     * @param files    提交的文件数组
     * @param fileKeys 提交的文件数组key
     * @return Response
     * @throws IOException
     */
    public Response postUpLoadSyn(String url, File[] files, String[] fileKeys) throws IOException {
        return getInstance()._postUpLoadSyn(url, files, fileKeys);
    }

    /**
     * 同步post文件上传,提交文件是单个的
     * 用于外部
     *
     * @param url     地址
     * @param file    提交的文件
     * @param fileKey 提交的文件key
     * @return Response
     * @throws IOException
     */
    public Response postUpLoadSyn(String url, File file, String fileKey) throws IOException {
        return getInstance()._postUpLoadSyn(url, file, fileKey);
    }

    /**
     * 同步post文件上传,提交的文件可以是多个,使用map集合版
     * 用于外部
     *
     * @param url 地址
     * @param map 参数
     * @return Response
     * @throws IOException
     */
    public Response postUpLoadSyn(String url, Map<String, Object> map) throws IOException {
        return getInstance()._postUpLoadSyn(url, map);
    }

    /**
     * 异步post的文件上传,提交的文件是数组
     * 用于外部
     *
     * @param url      地址
     * @param files    提交的文件数组
     * @param fileKeys 提交的文件数组的key
     * @param callback 回调接口
     * @param <T>      泛型
     * @throws IOException
     */
    public <T> void postUpLoadAsyn(String url, File[] files, String[] fileKeys, HttpCallBack<T> callback) {
        getInstance()._postUpLoadAsyn(url, files, fileKeys, callback);
    }

    /**
     * 异步post的文件上传,提交的文件是单个的
     * 用于外部
     *
     * @param url      地址
     * @param file     提交的文件
     * @param fileKey  提交的文件的key
     * @param callback 回调接口
     * @param <T>      泛型
     * @throws IOException
     */
    public <T> void postUpLoadAsyn(String url, File file, String fileKey, HttpCallBack<T> callback) {
        getInstance()._postUpLoadAsyn(url, callback, file, fileKey);
    }

    /**
     * 异步post的文件上传,提交文件可以是多个,使用map集合版
     * 用于外部
     *
     * @param url      地址
     * @param map      参数
     * @param callback 回调接口
     * @param <T>      泛型
     * @throws IOException
     */
    public <T> void postUpLoadAsyn(String url, Map<String, Object> map, HttpCallBack<T> callback) {
        getInstance()._postUpLoadAsyn(url, map, callback);
    }

    /**
     * 显示图片
     * 用于外部
     *
     * @param view       imageview
     * @param url        图片地址
     * @param errorResId 图片加载错误时显示的图片
     * @throws IOException
     */
    public void displayImage(final ImageView view, String url, int errorResId) throws IOException {
        getInstance()._displayImage(view, url, errorResId);
    }

    /**
     * 显示图片
     * 用于外部
     *
     * @param view imageview
     * @param url  图片地址
     */
    public void displayImage(final ImageView view, String url) {
        getInstance()._displayImage(view, url, -1);
    }

    /**
     * 异步下载文件
     * 用于外部
     *
     * @param url
     * @param destDir
     * @param callback
     * @param <T>
     */
    public <T> void downloadAsyn(String url, String destDir, HttpCallBack<T> callback) {
        getInstance()._downloadAsyn(url, destDir, callback);
    }

}
