package ru.tukallo.ctddev.ifmo.simplebrowser.tabmanagement;

import android.support.annotation.NonNull;
import android.webkit.WebView;

/**
 * Created by Aleksandr Tukallo on 20.06.17.
 */

class WebViewInfo implements Comparable<WebViewInfo> {
    WebView webView;
    int numberCached;

    WebViewInfo(WebView webView) {
        this.webView = webView;
    }

    void incrementUsages() {
        numberCached++;
    }

    void decrementUsages() {
        numberCached--;
    }

    @Override
    public int compareTo(@NonNull WebViewInfo webViewInfo) {
        return numberCached - webViewInfo.numberCached;
    }

    @Override
    public int hashCode() {
        return webView.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof WebViewInfo)) {
            return false;
        }

        WebViewInfo other = (WebViewInfo) obj;
        return webView.equals(other.webView);
    }
}
