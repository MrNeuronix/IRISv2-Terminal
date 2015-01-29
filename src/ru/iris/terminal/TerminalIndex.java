package ru.iris.terminal;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.strumsoft.websocket.phonegap.WebSocketFactory;

public class TerminalIndex extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        WebView wv = (WebView)findViewById(R.id.webview);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
            }
        });
        wv.setWebViewClient(
                new WebViewClient(){
                    public boolean shouldOverrideUrlLoading(WebView view, String url){
                        view.loadUrl(url);
                        return false;
                    }
                });
        wv.loadUrl("file:///android_asset/www/index.html");
        wv.addJavascriptInterface(new WebSocketFactory(wv), "WebSocketFactory");

    }

}
