package com.steven.android29_recyclerview.utils;

import android.net.Uri;

import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.ResponseBody;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.NetworkPolicy;
import com.steven.android29_recyclerview.helper.OkHttpClientHelper;

import java.io.File;
import java.io.IOException;

/**
 * Created by StevenWang on 16/6/3.
 */
public class MyOkHttpDownloader implements Downloader {
    private static OkHttpClient defaultOkHttpClient() {
        OkHttpClient client = OkHttpClientHelper.getOkHttpSingletonInstance();
        return client;
    }

    private final OkHttpClient client;



    /**
     * Create new downloader that uses OkHttp. This will install an image cache into the specified
     * directory.
     *
     * @param cacheDir The directory in which the cache should be stored
     * @param maxSize The size limit for the cache.
     */
    public MyOkHttpDownloader(final File cacheDir, final long maxSize) {
        this(defaultOkHttpClient());
        try {
            client.setCache(new com.squareup.okhttp.Cache(cacheDir, maxSize));
        } catch (Exception ignored) {
        }
    }

    /**
     * Create a new downloader that uses the specified OkHttp instance. A response cache will not be
     * automatically configured.
     */
    public MyOkHttpDownloader(OkHttpClient client) {
        this.client = client;
    }

    protected final OkHttpClient getClient() {
        return client;
    }

    @Override public Response load(Uri uri, int networkPolicy) throws IOException {
        CacheControl cacheControl = null;
        if (networkPolicy != 0) {
            if (NetworkPolicy.isOfflineOnly(networkPolicy)) {
                cacheControl = CacheControl.FORCE_CACHE;
            } else {
                CacheControl.Builder builder = new CacheControl.Builder();
                if (!NetworkPolicy.shouldReadFromDiskCache(networkPolicy)) {
                    builder.noCache();
                }
                if (!NetworkPolicy.shouldWriteToDiskCache(networkPolicy)) {
                    builder.noStore();
                }
                cacheControl = builder.build();
            }
        }

        Request.Builder builder = new Request.Builder().url(uri.toString());
        if (cacheControl != null) {
            builder.cacheControl(cacheControl);
        }

        com.squareup.okhttp.Response response = client.newCall(builder.build()).execute();
        int responseCode = response.code();
        if (responseCode >= 300) {
            response.body().close();
            throw new ResponseException(responseCode + " " + response.message(), networkPolicy,
                    responseCode);
        }

        boolean fromCache = response.cacheResponse() != null;

        ResponseBody responseBody = response.body();
        return new Response(responseBody.byteStream(), fromCache, responseBody.contentLength());
    }

    @Override public void shutdown() {
        com.squareup.okhttp.Cache cache = client.getCache();
        if (cache != null) {
            try {
                cache.close();
            } catch (IOException ignored) {
            }
        }
    }
}
