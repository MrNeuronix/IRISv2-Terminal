package ru.iris.terminal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.strumsoft.websocket.phonegap.WebSocketFactory;

public class TerminalIndex extends Activity {

    private final String TAG = "IRIS-Terminal";

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(TAG, "Start!");

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
        wv.loadUrl("http://192.168.6.19:9000/terminal");
        wv.addJavascriptInterface(new WebSocketFactory(wv), "WebSocketFactory");

        // Запускаем сервис речи и данные с AMQ
        Log.v(TAG, "Starting services!");
        startService(new Intent(this, SpeakService.class));
        startService(new Intent(this, AMQPService.class));
    }

    @Override
    public void onDestroy()
    {
        stopService(new Intent(this, SpeakService.class));
        stopService(new Intent(this, AMQPService.class));
    }

}
