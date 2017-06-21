package ru.tukallo.ctddev.ifmo.simplebrowser.tabmanagement;

import android.webkit.WebView;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Class should be used to store and update currently cached tabs:
 * those ones, which are already loaded in webview.
 */
class ActiveTabs {
    private Map<String, WebView> cachedPages = new HashMap<>(); // url -> view
    private PriorityQueue<WebViewInfo> priorityQueue = new PriorityQueue<>();
    private Map<WebView, WebViewInfo> queueAccess = new HashMap<>();
    private Map<WebView, String> activePages = new HashMap<>();

    void addWebView(WebView webView) {
        WebViewInfo webViewInfo = new WebViewInfo(webView);
        queueAccess.put(webView, webViewInfo);
        priorityQueue.add(webViewInfo);
    }

    boolean isCached(String url) {
        return cachedPages.containsKey(url);
    }

    WebView getWebView(String url) {
        return cachedPages.get(url);
    }

    /**
     * Returns view with minimal number of cached pages and at the same time
     * increments number of cached pages there
     *
     * @return WebView with minimal number of usages
     */
    WebView updateMinimal() {
        WebViewInfo min = priorityQueue.poll();
        min.incrementUsages();
        priorityQueue.add(min);
        return min.webView;
    }

    /**
     * Makes given url active
     *
     * @param url to make active
     */
    void updateActive(String url) {
        activePages.put(cachedPages.get(url), url);
    }

    /**
     * Method does not increment
     *
     * @param webView
     * @param url
     */
    void addCached(WebView webView, String url) {
        cachedPages.put(url, webView);
        activePages.put(webView, url);
    }

    /**
     * Does decrement
     *
     * @param url
     */
    void removeCached(String url) {
        //if this url is active
        if (activePages.get(cachedPages.get(url)).equals(url)) {
            cachedPages.get(url).loadData("Tab closed, sorry :(", "text", StandardCharsets.UTF_8.toString());
            activePages.remove(cachedPages.get(url));
        }

        queueAccess.get(cachedPages.get(url)).decrementUsages();
        cachedPages.remove(url);
    }

    Set<String> getActiveTabs() {
        Set<String> active = new HashSet<>();
        for (WebView webView : activePages.keySet()) {
            active.add(activePages.get(webView));
        }
        return active;
    }

    Set<String> getAllTabs() {
        return cachedPages.keySet();
    }

    /**
     * Just for debugging and logging
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (WebViewInfo webViewInfo : priorityQueue) {
            sb.append(webViewInfo.webView.toString()).append(" with load: ").append(Integer.toString(webViewInfo.numberCached)).append("\n");
        }
        return sb.toString();
    }
}
