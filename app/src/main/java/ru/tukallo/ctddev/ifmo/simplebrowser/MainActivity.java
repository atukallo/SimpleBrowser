package ru.tukallo.ctddev.ifmo.simplebrowser;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ru.tukallo.ctddev.ifmo.simplebrowser.networking.Networking;
import ru.tukallo.ctddev.ifmo.simplebrowser.tabmanagement.TabManager;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";

    private static final int POOL_SIZE = 10;
    private static final int MAX_SUGGESTIONS = 4;

    private static final String KEY_TABS = "tabs";

    private LayoutInflater layoutInflater;

    private EditText editText;
    private LinearLayout suggestions;

    private WebView currentTab;

    private LinearLayout tabsList;
    private ScrollView scrollView;

    private TabManager tabManager;

    private FrameLayout frameLayout;

    private void setFocus(View view) {
        for (int i = 0; i < frameLayout.getChildCount(); i++) {
            frameLayout.getChildAt(i).setVisibility(GONE);
        }
        view.setVisibility(VISIBLE);
    }

    private class GoOnClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            suggestions.setVisibility(GONE);
            String text = editText.getText().toString();
            Log.d(TAG, "GoOnClick: " + text);
            currentTab = tabManager.open(text);
            setFocus(currentTab);
        }
    }

    private class TabOnClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            String url = ((TextView) view).getText().toString();
            Log.d(TAG, "TabOnClick: tab with " + url + " clicked");
            currentTab = tabManager.open(url);
            setFocus(currentTab);
        }
    }

    private class CrossOnClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "CrossOnClick");

            LinearLayout linearLayout = (LinearLayout) view.getParent();
            String url = ((TextView) linearLayout.findViewById(R.id.url)).getText().toString();
            tabManager.close(url);

            updateTabList();
        }
    }

    private class TabsOnClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            suggestions.setVisibility(GONE);
            Log.d(TAG, "TabsOnClick");
            updateTabList();
            setFocus(scrollView);
        }
    }

    private class WebViewOnClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            suggestions.setVisibility(GONE);
        }
    }

    private void updateTabList() {
        tabsList.removeAllViews();
        Set<String> allTabs = tabManager.getAllTabs();
        Set<String> activeTabs = tabManager.getActiveTabs();
        for (String url : allTabs) {
            LinearLayout tab = (LinearLayout) layoutInflater.inflate(R.layout.tab, tabsList, false);
            tabsList.addView(tab);

            TextView textView = (TextView) tab.findViewById(R.id.url);
            textView.setText(url);
            textView.setOnClickListener(new TabOnClick());

            Button cross = (Button) tab.findViewById(R.id.cross);
            cross.setOnClickListener(new CrossOnClick());

            if (activeTabs.contains(url)) {
                textView.setBackgroundColor(getResources().getColor(R.color.activeTab));
            }
        }
    }

    private class SuggestionOnClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            TextView currentSugg = (TextView) view;
            editText.setText(currentSugg.getText());
            suggestions.setVisibility(INVISIBLE);
        }
    }

    private void updateSuggestionList(List<String> urls) {
        suggestions.removeAllViews();
        suggestions.setVisibility(VISIBLE);
        suggestions.bringToFront();
        Log.d(TAG, "updateSuggestionList: updating suggestions and displaying them");

        for (int i = 0; i < Math.min(urls.size(), MAX_SUGGESTIONS); i++) {
            TextView suggestion = (TextView) layoutInflater.inflate(R.layout.suggestion, suggestions, false);
            suggestion.setText(urls.get(i));
            suggestion.setOnClickListener(new SuggestionOnClick());
            suggestions.addView(suggestion);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tabManager = new TabManager();

        layoutInflater = getLayoutInflater();

        Button goButton = (Button) findViewById(R.id.go);
        goButton.setOnClickListener(new GoOnClick());

        Button tabsButton = (Button) findViewById(R.id.tabs);
        tabsButton.setOnClickListener(new TabsOnClick());

        suggestions = (LinearLayout) findViewById(R.id.suggestions);

        editText = (EditText) findViewById(R.id.editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(final CharSequence charSequence, int i, int i1, int i2) {
                Networking.getInstance(MainActivity.this).addToRequestQueue(new JsonArrayRequest(
                        Request.Method.GET,
                        "http://suggest.yandex.ru/suggest-ff.cgi?part=" + charSequence.toString(), null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                JSONArray array = response.optJSONArray(1);
                                if (array == null) {
                                    Log.w(TAG, "onResponse: got null array of suggestions");
                                } else {
                                    try {
                                        Log.d(TAG, "onResponse: got suggestions: " + array.toString());
                                        List<String> suggs = new ArrayList<String>(array.length());
                                        for (int i = 0; i < array.length(); i++) {
                                            suggs.add(array.getString(i));
                                        }
                                        updateSuggestionList(suggs);
                                    } catch (JSONException e) {
                                        Log.d(TAG, "onResponse: catched JSONException: " + e.getMessage());
                                    }
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, "onErrorResponse: error detected");
                            }
                        }
                ));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        tabsList = (LinearLayout) findViewById(R.id.tabsManager);

        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        for (int i = 0; i < POOL_SIZE; i++) {
            WebView view = new WebView(this);
            view.setOnClickListener(new WebViewOnClick());
            view.setWebViewClient(new CustomWebViewClient());

            frameLayout.addView(view);
            tabManager.addWebView(view);
            currentTab = view; //last will be chosen
        }
        frameLayout.setVisibility(VISIBLE);

        if (savedInstanceState != null) {
            tabManager.restoreFromSavedState(savedInstanceState.getStringArrayList(KEY_TABS));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> allTabs = new ArrayList<>(tabManager.getAllTabs());
        outState.putStringArrayList(KEY_TABS, allTabs);
    }
}
