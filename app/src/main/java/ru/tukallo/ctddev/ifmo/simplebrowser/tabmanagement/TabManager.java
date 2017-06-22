package ru.tukallo.ctddev.ifmo.simplebrowser.tabmanagement;

import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * Created by Aleksandr Tukallo on 19.06.17.
 */

/**
 * This is the main class to monitor and manage WebViews and loaded pages. Consider using
 * method <code>open</code> to load url or to retrieve it from cache. Caching is done
 * automatically inside the class
 */
public class TabManager {
    private final static String TAG = "TabManager";

    private final ActiveTabs activeTabs = new ActiveTabs(); //for extracting htmls and getting views for strings

    /**
     * Add WebView to pool, where tabs are shown
     *
     * @param webView is a WebView to add
     */
    public void addWebView(WebView webView) {
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        activeTabs.addWebView(webView);
    }

    /**
     * Method should be called, when app is being restored from SavedState. Provided urls are
     * loaded to WebViews to continue working with them
     *
     * @param urls are urls to load
     * @return random WebView to display is returned
     */
    public WebView restoreFromSavedState(List<String> urls) {
        return activeTabs.restoreFromSavedState(urls);
    }

    /**
     * Close tab and delete it from WebViews
     *
     * @param url to delete
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

    /**
     * Method opens given url if possible in any WebView and returns that WebView.
     * Url is either downloaded or retrieved from cache.
     *
     * @param url is a url to open
     * @return WebView with given url is returned
     */
    public WebView open(String url) {
        Log.d(TAG, "open: current stats: \n" + activeTabs.toString());

        Log.d(TAG, "open: opening " + url);
        //if there is already a view with needed page
        if (activeTabs.isCached(url)) {
            Log.d(TAG, "open: url already opened in " + activeTabs.getWebView(url));
            WebView toUse = activeTabs.getWebView(url);
            if (!activeTabs.isActive(url)) {
                activeTabs.updateActive(url);
                toUse.loadUrl(url);
            }
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

    /**
     * Get set with urls, which are currently displayed in WebViews
     *
     * @return set with strings
     */
    public Set<String> getActiveTabs() {
        return activeTabs.getActiveTabs();
    }

    /**
     * Get set with all the cached urls
     *
     * @return set with all the urls
     */
    public Set<String> getAllTabs() {
        return activeTabs.getAllTabs();
    }
}
