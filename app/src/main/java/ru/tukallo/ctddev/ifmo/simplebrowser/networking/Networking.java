package ru.tukallo.ctddev.ifmo.simplebrowser.networking;

import android.content.Context;

import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;

import java.io.IOException;

/**
 * Created by Aleksandr Tukallo on 21.06.17.
 */
public class Networking {
    private static Networking instance;
    private final Context mCtx;
    private RequestQueue mRequestQueue;

    private Networking(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static Networking getInstance(Context context) {
        if (instance == null) {
            instance = new Networking(context);
        }
        return instance;
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = createRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    private RequestQueue createRequestQueue(final Context context) {
        HttpStack stack = new HurlStack();
        Network network = new BasicNetwork(stack);
        RequestQueue queue = new RequestQueue(createCache(context), network);
        queue.start();

        return queue;
    }

    private Cache createCache(Context context) {
        return new Cache(context);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    private static class Cache implements com.android.volley.Cache {
        private CacheFileManager cfm;

        private Cache(Context context) {
            cfm = new CacheFileManager(context);
        }

        @Override
        public Entry get(final String key) {
            return null;
        }

        @Override
        public void put(final String key, final Entry entry) {
            String s = prepareKey(key);
            String s1 = new String(entry.data);
            try {
                cfm.put(s, s1);
            } catch (IOException e) {
            }
        }

        @Override
        public void initialize() {
        }

        @Override
        public void invalidate(final String key, final boolean fullExpire) {
        }

        @Override
        public void remove(final String key) {
        }

        @Override
        public void clear() {
        }

        private String prepareKey(final String key) {
            return key;
        }
    }
}
