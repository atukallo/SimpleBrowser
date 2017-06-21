package ru.tukallo.ctddev.ifmo.simplebrowser;

import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.nio.charset.StandardCharsets;

public class CustomWebViewClient extends WebViewClient {
    private static final String TAG = CustomWebViewClient.class.getCanonicalName();

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        view.loadUrl(request.getUrl().toString());
        return true;
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        Log.d(TAG, "onReceivedError: " + error.toString());
        view.loadData("Unable to load page :(", "text", StandardCharsets.UTF_8.toString());
    }
}
