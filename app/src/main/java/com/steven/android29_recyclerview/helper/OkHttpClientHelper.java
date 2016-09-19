package com.steven.android29_recyclerview.helper;


import android.content.Context;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Created by Steven
 * on 16-1-7.
 */
public class OkHttpClientHelper {
    private static OkHttpClient okHttpClient = null;
    private static OkHttpClientHelper okHttpUtils = null;

    private OkHttpClientHelper(Context context) {
        okHttpClient = getOkHttpSingletonInstance();

        //开启响应缓存
        okHttpClient.setCookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER));
        //设置缓存目录和大小
        int cacheSize = 10 << 20; // 10 MiB
        Cache cache = new Cache(context.getCacheDir(), cacheSize);
        okHttpClient.setCache(cache);

        //设置合理的超时
        okHttpClient.setConnectTimeout(15, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(20, TimeUnit.SECONDS);
        okHttpClient.setWriteTimeout(20, TimeUnit.SECONDS);

        //以下验证不设置，那么默认就已经设置了验证
        okHttpClient.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
    }

    public static OkHttpClientHelper getOkHttpClientUtils(Context context) {
        if (okHttpUtils == null) {
            synchronized (OkHttpClientHelper.class) {
                if (okHttpUtils == null) {
                    okHttpUtils = new OkHttpClientHelper(context);
                }
            }
        }
        return okHttpUtils;
    }

    public static OkHttpClient getOkHttpSingletonInstance() {
        if (okHttpClient == null) {
            synchronized (OkHttpClient.class) {
                okHttpClient = new OkHttpClient();
            }
        }
        return okHttpClient;
    }

    ///////////////////////////////////////////////////////////////////////////
    // GET方式网络访问
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 基方法，返回Request对象
     *
     * @param urlString
     * @param tag
     * @return
     */
    private Request buildGetRequest(String urlString, Object tag) {
        Request.Builder builder = new Request.Builder();
        builder.url(urlString);
        if (tag != null) {
            builder.tag(tag);
        }
        return builder.build();
    }

    /**
     * 自定义方法，返回Response对象
     *
     * @param urlString
     * @return
     * @throws IOException
     */
    private Response buildResponse(String urlString, Object tag) throws IOException {
        Request request = buildGetRequest(urlString, tag);
        Response response = okHttpClient.newCall(request).execute();
        return response;
    }

    //基础方法，返回ResponseBody对象
    private ResponseBody buildResponseBody(String urlString, Object tag) throws IOException {
        Response response = buildResponse(urlString, tag);
        if (response.isSuccessful()) {
            return response.body();
        }
        return null;
    }

    /**
     * 作用：实现网络访问文件，将获取到数据储存在文件流中
     *
     * @param urlString ：访问网络的url地址
     * @return InputStream
     */
    public static InputStream getStreamFromURL(Context context, String urlString, Object tag)
            throws IOException {
        ResponseBody body = getOkHttpClientUtils(context).buildResponseBody(urlString, tag);
        if (body != null) {
            return body.byteStream();
        }
        return null;
    }

    /**
     * 作用：实现网络访问文件，将获取到的数据存在字节数组中
     *
     * @param urlString ：访问网络的url地址
     * @return byte[]
     */
    public static byte[] getBytesFromURL(Context context, String urlString, Object tag) throws
            IOException {
        ResponseBody body = getOkHttpClientUtils(context).buildResponseBody(urlString, tag);
        if (body != null) {
            return body.bytes();
        }
        return null;
    }

    /**
     * 作用：实现网络访问文件，将获取到的数据存在字符串中
     *
     * @param urlString ：访问网络的url地址
     * @return String
     */
    public static String getStringFromURL(Context context, String urlString, Object tag) throws
            IOException {
        ResponseBody body = getOkHttpClientUtils(context).buildResponseBody(urlString, tag);
        if (body != null) {
            return body.string();
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // POST方式访问网络
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 基方法，返回Request对象
     *
     * @param urlString
     * @param tag
     * @return
     */
    private Request buildPostRequest(String urlString, RequestBody requestBody, Object tag) {
        Request.Builder builder = new Request.Builder();
        builder.url(urlString).post(requestBody);
        //builder.addHeader("Accept", "application/json; q=0.5");
        if (tag != null) {
            builder.tag(tag);
        }
        return builder.build();
    }

    /**
     * 作用：post提交数据，返回服务器端返回的字节数组
     *
     * @param urlString ：访问网络的url地址
     * @return byte[]
     */
    private String postRequestBody(String urlString, RequestBody requestBody, Object tag) {
        Request request = buildPostRequest(urlString, requestBody, tag);
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 作用：POST提交键值对，再返回相应的数据
     *
     * @param urlString ：访问网络的url地址
     * @param map       ：访问url时，需要传递给服务器的键值对数据。
     * @return String
     */
    public static String postKeyValuePair(Context context, String urlString, Map<String, String>
            map, Object tag) {
        //往FormEncodingBuilder对象中放置键值对
        FormEncodingBuilder formBuilder = new FormEncodingBuilder();
        if (map != null && !map.isEmpty()) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                formBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        //生成请求体对象
        RequestBody requestBody = formBuilder.build();
        //将请求提放置到请求对象中
        return getOkHttpClientUtils(context).postRequestBody(urlString, requestBody, tag);
    }

    /**
     * 作用：POST提交Json字符串，再返回相应的数据
     *
     * @param urlString  ：访问网络的url地址
     * @param jsonString ：访问url时，需要传递给服务器的json字符串
     * @return byte[]
     */
    public static String postJsonString(Context context, String urlString, String jsonString,
                                        Object tag) {
        //定义mimetype对象
        /*String MEDIA_TYPE_STREAM = "application/octet-stream;charset=utf-8";
        String MEDIA_TYPE_STRING = "text/plain;charset=utf-8";*/
        String MEDIA_TYPE_JSON = "application/json;charset=utf-8";
        MediaType JSON = MediaType.parse(MEDIA_TYPE_JSON);
        RequestBody requestBody = RequestBody.create(JSON, jsonString);
        return getOkHttpClientUtils(context).postRequestBody(urlString, requestBody, tag);
    }

    ///////////////////////////////////////////////////////////////////////////
    // 异步网络访问
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 开启异步线程访问网络，通过回调方法实现数据加载
     * 如果第二个参数为null，空callback， 则说明不在意返回结果
     *
     * @param urlString
     * @param callback
     */
    public static void getDataAsync(Context context, String urlString, Callback callback, Object
            tag) {
        Request request = getOkHttpClientUtils(context).buildGetRequest(urlString, tag);
        getOkHttpSingletonInstance().newCall(request).enqueue(callback);
    }

    /**
     * 作用：post提交数据，返回服务器端返回的字节数组
     *
     * @param urlString ：访问网络的url地址
     */
    private void postRequestBodyAsync(String urlString, RequestBody requestBody, Callback
            callback, Object tag) {
        Request request = buildPostRequest(urlString, requestBody, tag);
        if (callback == null) {
            new Callback() {

                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {

                }
            };
        }
        okHttpClient.newCall(request).enqueue(callback);
    }

    /**
     * 作用：POST提交键值对，再返回相应的数据
     *
     * @param urlString ：访问网络的url地址
     * @param map       ：访问url时，需要传递给服务器的键值对数据。
     */
    public static void postKeyValuePairAsync(Context context, String urlString, Map<String,
            String> map, Callback callback, Object tag) {
        //往FormEncodingBuilder对象中放置键值对
        FormEncodingBuilder formBuilder = new FormEncodingBuilder();
        if (map != null && !map.isEmpty()) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                formBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        //生成请求体对象
        RequestBody requestBody = formBuilder.build();
        //将请求提放置到请求对象中
        getOkHttpClientUtils(context).postRequestBodyAsync(urlString, requestBody, callback, tag);
    }

    /**
     * 作用：post异步上传文件，提交分块请求
     *
     * @param urlString     网络地址
     * @param map           提交给服务器的表单信息键值对
     * @param files         提交的文件
     * @param formFieldName 每个需要提交的文件对应的文件input的name值
     * @param callback      异步上传回调方法
     * @throws IOException
     */
    public static void postUploadFilesAsync(Context context, String urlString, Map<String,
            String> map, File[] files, String[] formFieldName, Callback callback, Object tag)
            throws IOException {
        RequestBody requestBody = getOkHttpClientUtils(context).buildRequestBody(map, files,
                formFieldName);
        getOkHttpClientUtils(context).postRequestBodyAsync(urlString, requestBody, callback, tag);
    }

    ///////////////////////////////////////////////////////////////////////////
    // POST方式提交分块请求，实现文件上传
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 同步基于post的文件上传:上传多个文件以及携带key-value对：主方法
     *
     * @param urlString
     * @param formFiledName
     * @param files
     * @param map
     * @param tag
     * @return String
     * @throws IOException
     */
    public static String postUploadFiles(Context context, String urlString, Map<String, String>
            map, File[] files, String[] formFiledName, Object tag) throws IOException {
        RequestBody requestBody = getOkHttpClientUtils(context).buildRequestBody(map, files,
                formFiledName);
        return getOkHttpClientUtils(context).postRequestBody(urlString, requestBody, tag);
    }

    /**
     * 创建post上传附件的request对象
     * Post方式提交分块请求——上传文件及其它表单数据
     *
     * @param files
     * @param formFiledName
     * @param map
     * @return
     */
    private RequestBody buildRequestBody(Map<String, String> map, File[] files, String[]
            formFiledName) {
        MultipartBuilder builder = new MultipartBuilder().type(MultipartBuilder.FORM);
        //往MultipartBuilder对象中添加普通input控件的内容
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                //添加普通input块的数据
                builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + entry
                                .getKey() + "\""),
                        RequestBody.create(null, entry.getValue()));
            }
        }
        //往MultipartBuilder对象中添加file input控件的内容
        if (files != null && formFiledName != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String fileName = file.getName();
                RequestBody requestBody = RequestBody.create(MediaType.parse
                        ("multipart/form-data"), file);
                //添加file input块的数据
                builder.addPart(Headers.of("Content-Disposition",
                        "form-data; name=\"" + formFiledName[i] + "\"; filename=\"" + fileName +
                                "\""), requestBody);
            }
        }
        //生成RequestBody对象
        return builder.build();
    }

    /**
     * 获取Mime类型
     *
     * @param filename
     * @return
     */
    private static String getMimeType(String filename) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(filename);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }


    public static void cancelCall(Object tag) {
        getOkHttpSingletonInstance().cancel(tag);
    }

}