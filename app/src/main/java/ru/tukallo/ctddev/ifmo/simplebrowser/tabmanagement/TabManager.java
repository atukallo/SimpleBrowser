package ru.tukallo.ctddev.ifmo.simplebrowser.tabmanagement;

import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

/**
 * Created by Aleksandr Tukallo on 19.06.17.
 */

public class TabManager {
    private final static String TAG = "TabManager";

    private final ActiveTabs activeTabs = new ActiveTabs(); //for extracting htmls and getting views for strings

    /**
     * Add WebView to pool, where tabs are shown
     */
    public void addWebView(WebView webView) {
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        activeTabs.addWebView(webView);
    }

    /**
     * Close tab and close it from memory and from WebViews
     */
    public void close(String url) {
        activeTabs.removeCached(url);
    }

    //method check if provided url has correct syntax
    private boolean isUrlCorrect(String url, WebView webView) {
        try {
            URL u = new URL(url);
            u.toURI();
            return true;
        } catch (URISyntaxException | MalformedURLException e) {
            Log.d(TAG, "isUrlCorrect: given url is not correct: " + url);
            webView.loadData("<p>Provided URL is not correct :(</p> <p>Try other sites, such as:</p> <p> <i>https://google.com</i>, <i>https://vk.com</i> and many many many others</p>", "text/html", null);
            return false;
        }
    }

    //return view with this page (check if in cached or load from mem) or download
    public WebView open(String url) {
        Log.d(TAG, "open: current stats: \n" + activeTabs.toString());

        Log.d(TAG, "open: opening " + url);
        //if there is already a view with needed page
        if (activeTabs.isCached(url)) {
            Log.d(TAG, "open: url already opened in " + activeTabs.getWebView(url));
            WebView toUse = activeTabs.getWebView(url);
            activeTabs.updateActive(url);
            toUse.loadUrl(url);
            return toUse;
        }

        Log.d(TAG, "open: url should be loaded from the net");
        WebView toUse = activeTabs.updateMinimal();
        if (isUrlCorrect(url, toUse)) {
            toUse.loadUrl(url);
            activeTabs.addCached(toUse, url);
        }
        return toUse;
    }

    public Set<String> getActiveTabs() {
        return activeTabs.getActiveTabs();
    }

    public Set<String> getAllTabs() {
        return activeTabs.getAllTabs();
    }
}
