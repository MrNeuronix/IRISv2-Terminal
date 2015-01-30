package ru.iris.terminal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.strumsoft.websocket.phonegap.WebSocketFactory;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

public class TerminalIndex extends Activity {

    private final String TAG = "IRIS-Terminal";
    private Messenger mService = null;
    private boolean mBound;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(TAG, "Start!");

        if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("installed", false))
        {
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("installed", true).commit();
            copyAssetFolder(getAssets(), "smarthouse", "/data/data/ru.iris.terminal/files");
        }

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
        Intent speak = new Intent(this, SpeakService.class);
        Intent amq = new Intent(this, AMQPService.class);
        bindService(speak, mConnection, Context.BIND_AUTO_CREATE);
        bindService(amq, mConnection, Context.BIND_AUTO_CREATE);
        startService(speak);
        startService(amq);
        Log.v(TAG, "Starting services done!");

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                sendMessage("Говорю всякую хуйню!");
            }
        }, 10000);

        timer.schedule(new TimerTask() {

            public void run() {
                sendMessage("Говорю всякую хуйню еще раз!");
            }}, 15000 );
    }

    @Override
    public void onDestroy()
    {
        stopService(new Intent(this, SpeakService.class));
        stopService(new Intent(this, AMQPService.class));
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Log.v(TAG, "Service connected!");

            mService = new Messenger(service);
            mBound = true;
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    public void sendMessage(String text) {
        if (!mBound)
            return;
        Message msg = Message.obtain(null, SpeakService.MSG_SPEAK, text);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /// Копирование файлов с моделями речи на внутренюю флешку
    ////////////////////////////////////////////////////////

    private static boolean copyAssetFolder(AssetManager assetManager,
                                           String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            new File(toPath).mkdirs();
            boolean res = true;
            for (String file : files)
                if (file.contains("."))
                    res &= copyAsset(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
                else
                    res &= copyAssetFolder(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean copyAsset(AssetManager assetManager,
                                     String fromAssetPath, String toPath) {

        Log.v("IRIS", "Copy to " + toPath);

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

}
