package ru.tukallo.ctddev.ifmo.simplebrowser.tabmanagement;

import android.webkit.WebView;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Created by Aleksandr Tukallo on 19.06.17
 */

/**
 * Class should be used to store and update loaded pages. Moreover, it manages WebViews and
 */
class ActiveTabs {
    private Map<String, WebView> cachedPages = new HashMap<>(); // url -> view
    private PriorityQueue<WebViewInfo> priorityQueue = new PriorityQueue<>();
    private Map<WebView, WebViewInfo> queueAccess = new HashMap<>();
    private Map<WebView, String> activePages = new HashMap<>();

    /**
     * Add another WebView to the pool
     *
     * @param webView to add
     */
    void addWebView(WebView webView) {
        WebViewInfo webViewInfo = new WebViewInfo(webView);
        queueAccess.put(webView, webViewInfo);
        priorityQueue.add(webViewInfo);
    }

    /**
     * Check if this page was already once loaded and is cached in one of the WebViews
     *
     * @param url url to check
     */
    boolean isCached(String url) {
        return cachedPages.containsKey(url);
    }

    /**
     * Method loads all the provided urls to WebViews
     *
     * @param urls urls to load to WebViews
     * @return random WebView to display is returned
     */
    WebView restoreFromSavedState(List<String> urls) {
        List<WebViewInfo> webViewInfos = new ArrayList<>(priorityQueue.size());
        while (priorityQueue.size() != 0) {
            webViewInfos.add(priorityQueue.poll());
        }

        int mod = urls.size() % webViewInfos.size();
        int pagesForEach = urls.size() / webViewInfos.size();
        for (int i = 0; i < webViewInfos.size(); i++) {
            WebViewInfo cur = webViewInfos.get(i);
            for (int j = 0; j < pagesForEach + (mod == 0 ? 0 : 1); j++) {
                String curUrl = urls.get(urls.size() - 1);
                urls.remove(urls.size() - 1);
                cachedPages.put(curUrl, cur.webView);
                cur.webView.loadUrl(curUrl);
                activePages.put(cur.webView, curUrl);
            }
            cur.numberCached = pagesForEach + (mod == 0 ? 0 : 1);
            mod = mod == 0 ? 0 : mod - 1;
        }

        priorityQueue.addAll(webViewInfos);
        return webViewInfos.iterator().next().webView;
    }

    /**
     * Get WebView, where provided url is cached
     *
     * @param url to find
     * @return WebView, where it is cached
     */
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
     * Method does not increment number of cached pages for given WebView, but it marks
     * url as active
     *
     * @param webView to add url to
     */
    void addCached(WebView webView, String url) {
        cachedPages.put(url, webView);
        activePages.put(webView, url);
    }

    /**
     * Method removes provided url forever and number of pages for WebView, where url is stored is decremented
     *
     * @param url to delete
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
