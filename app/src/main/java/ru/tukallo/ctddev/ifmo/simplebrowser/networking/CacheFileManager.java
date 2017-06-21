package ru.tukallo.ctddev.ifmo.simplebrowser.networking;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aleksandr Tukallo on 21.06.17.
 */
class CacheFileManager {
    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static final ThreadPoolExecutor tpe = new ThreadPoolExecutor(
            NUMBER_OF_CORES * 2,
            NUMBER_OF_CORES * 2,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>()
    );
    private final Handler h = new Handler(Looper.getMainLooper());
    @NonNull
    private final Context context;
    @Nullable
    private MessageDigest digest;

    public CacheFileManager(@NonNull Context context) {
        this.context = context;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
        }
    }

    @NonNull
    public Context getContext() {
        return context;
    }

    public void put(String key, String data) throws IOException {
        OutputStream cacheOutput = getCacheOutputFor(key);

        if (cacheOutput != null) {
            OutputStreamWriter osw =
                    new OutputStreamWriter(cacheOutput);
            osw.write(data);
            osw.flush();
            osw.close();
        }
    }

    public void get(final String url, final Listener listener) {
        tpe.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream cacheInput = getCacheInputFor(url);
                    InputStreamReader isr = new InputStreamReader(cacheInput);
                    final StringBuilder sb = new StringBuilder();
                    char[] buf = new char[1024];

                    while (isr.read(buf) > 0)
                        sb.append(buf);

                    isr.close();

                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onSuccess(sb.toString());
                        }
                    });
                } catch (final IOException e) {
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onError(e);
                        }
                    });
                }
            }
        });
    }

    private OutputStream getCacheOutputFor(String key) throws FileNotFoundException {
        return context.openFileOutput(md5(key), Context.MODE_PRIVATE);
    }

    private InputStream getCacheInputFor(String key) throws IOException {
        String hash = md5(key);
        try {
            return context.openFileInput(hash);
        } catch (FileNotFoundException e) {
            return context.getAssets().open("cache/" + hash);
        }
    }

    @NonNull
    private String md5(final String s) {
        if (digest != null) {
            // Create MD5 Hash
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();
        }

        return "";
    }

    public boolean has(final String key) {
        String hash = md5(key);
        final String[] files = context.fileList();
        for (String s : files)
            if (s.equals(hash))
                return true;
        return false;
    }

    public interface Listener {

        void onSuccess(@NonNull String data);

        void onError(@NonNull Exception e);
    }
}
