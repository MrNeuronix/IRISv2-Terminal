package ru.iris.terminal;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.*;

import java.util.ArrayList;

public class SpeakService extends Service {

    final String TAG = "IRIS-SpeakAndRecognition";
    private Recognizer recognizer;
    private Vocalizer vocalizer;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    final static int MSG_SPEAK = 0;

    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate");

        // Key
        SpeechKit.getInstance().configure(getBaseContext(), "5a849502-dc73-40ba-9676-f9aa3b34d4ca");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand");

        // Запускаем AVD
        Log.v(TAG, "onStartCommand: Starting AVD");
        Error error = PhraseSpotter.initialize("/data/data/ru.iris.terminal/files", new AVDListener());

        if(error != Error.ERROR_OK)
        {
            Log.v(TAG, "Error: " + error.getString());
        }
        else {
            PhraseSpotter.start();
            Log.v(TAG, "onStartCommand: Started AVD");
        }

        Log.v(TAG, "Starting Speech");
        vocalizer = ru.yandex.speechkit.Vocalizer.createVocalizer("ru-RU", "Терминал системы Ирис успешно запущен!", true);
        vocalizer.setListener(new VocalizerListener());
        vocalizer.start();

        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        PhraseSpotter.stop();
        PhraseSpotter.uninitialize();
        super.onDestroy();
    }

    ////////
    ////////
    ////////

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    /**
     * Handler of incoming messages from clients.
     * Show Toast with received string
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SPEAK:
                    vocalizer = ru.yandex.speechkit.Vocalizer.createVocalizer("ru-RU", msg.obj.toString(), true);
                    vocalizer.setListener(new VocalizerListener());
                    vocalizer.start();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    // Класс синтеза речи
    /////////////////////////////////////////////////////////////////////////////
    private class VocalizerListener implements ru.yandex.speechkit.VocalizerListener {

        final String TAG = "IRIS-Speak";

        public void onSynthesisBegin(Vocalizer vocalizer)
        {
            Log.d(TAG, "onSynthesisBegin");
        }

        public void onSynthesisDone(Vocalizer vocalizer, Synthesis synthesis)
        {
            Log.v(TAG, "onSynthesisDone()");
        }

        public void onPlayingBegin(Vocalizer vocalizer) {
            Log.v(TAG, "onPlayingBegin()");
        }

        public void onPlayingDone(Vocalizer vocalizer) {
            Log.v(TAG, "onPlayingDone()");
            SpeakService.this.vocalizer = null;
        }

        public void onVocalizerError(Vocalizer vocalizer, Error error) {
            Log.v(TAG, "onVocalizerError()");
            SpeakService.this.vocalizer = null;
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    // Класс распознавания речи
    /////////////////////////////////////////////////////////////////////////////
    private class AVDListener implements PhraseSpotterListener
    {
        private final String TAG = "IRIS-AVD";

        public void onPhraseSpotted(String phrase, int phraseid)
        {
            Log.v(TAG, "onPhraseSpotted: " + phrase + " ID: " + phraseid);
            recognizer = Recognizer.create("ru-RU", "general", new RecognitionListener());
            recognizer.setVADEnabled(true);
            recognizer.start();
        }

        public void onPhraseSpotterStarted()
        {
            Log.v(TAG, "onPhraseSpotterStarted");
        }

        public void onPhraseSpotterStopped()
        {
            Log.v(TAG, "onPhraseSpotterStopped");
        }

        public void onPhraseSpotterError(Error error)
        {
            Log.v(TAG, "onPhraseSpotterError: " + error.getString());
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    // Класс распознавания речи
    /////////////////////////////////////////////////////////////////////////////
    private class RecognitionListener implements RecognizerListener {

        private final String TAG = "IRIS-Recognition";

        public void onRecordingBegin(Recognizer r) {
            Log.v(TAG, "onRecordingBegin");
        }

        public void onRecordingDone(Recognizer r) {
            Log.v(TAG, "onRecordingDone");
        }

        public void onSoundDataRecorded(Recognizer r, byte[] data) {
            Log.v(TAG, "onSoundDataRecorded");
        }

        public void onSpeechDetected(Recognizer r) {
            Log.v(TAG, "onSpeechDetected");
        }

        public void onPowerUpdated(Recognizer r, float power) {
            Log.v(TAG, "onPowerUpdated: "+power);
        }

        public void onRecognitionDone(Recognizer r, Recognition results) {
            Log.v(TAG, "onResults: " + results.toString());

            if (results.getBestResultText().length() > 0)
                Log.v(TAG, "onResults: " + results.getBestResultText());
            else
                Log.v(TAG, "onResults: " + "no results");

            ArrayList<String> matchReports = new ArrayList<String>();

            for (RecognitionHypothesis h : results.getHypotheses()) {
                matchReports.add(h.getNormalized() + " : " + h.getConfidence());
            }

            //matchReports - содержит режультаты и точность

            recognizer = null;
        }

        public void onPartialResults(Recognizer r, Recognition results, boolean endOfUtterance) {
            Log.v(TAG, "onPartialResults: " + results.toString());
        }

        public void onError(Recognizer r, ru.yandex.speechkit.Error error) {
            Log.v(TAG, "onError: " + error.getCode() + " (" + error.getString() + ")");
            recognizer = null;
        }
    }
}