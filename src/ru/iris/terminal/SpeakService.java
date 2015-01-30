package ru.iris.terminal;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import ru.yandex.speechkit.*;
import ru.yandex.speechkit.Error;

import java.util.ArrayList;

public class SpeakService extends Service {

    final String TAG = "IRIS-SpeakAndRecognition";
    private Recognizer recognizer;

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
        PhraseSpotter.initialize("/smarthome", new AVDListener());
        PhraseSpotter.start();

        someTask();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        PhraseSpotter.stop();
        PhraseSpotter.uninitialize();
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onBind");
        return null;
    }

    void someTask() {
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