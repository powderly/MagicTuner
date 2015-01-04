package org.wikipowdia.MagicTuner1;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;


import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

import java.util.HashMap;

/****************************************
 * creates the live card service intent
 * and handles the condition when it is destroyed
 *
 *
 *
 */

public class AppService extends Service {
    private static final String TAG = "AppService";
    private static final String LIVE_CARD_ID = "HelloGlass";
    private AppDrawer mCallback;
    private LiveCard2DRenderer mCallbackCanvas;
    private LiveCard mLiveCard;
    Thread pitch_detector_thread_;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null)
            createHighFrequencyLiveCardForCanvasDrawing();
        return START_STICKY;
    }

    private void createHighFrequencyLiveCardForCanvasDrawing() {
        pitch_detector_thread_ = new Thread(new PitchDetector(this, new Handler()));
        pitch_detector_thread_.start();
        mLiveCard = new LiveCard(this, LIVE_CARD_ID);
        mCallbackCanvas = new LiveCard2DRenderer();
        mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(mCallbackCanvas);
        addMenuToLiveCard();
    }

    private void addMenuToLiveCard() {
        Intent menuIntent = new Intent(this, MenuActivity.class);
        mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
        mLiveCard.publish(PublishMode.REVEAL);
    }

    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            if (mCallback != null) {
                mLiveCard.getSurfaceHolder().removeCallback(mCallback);
            }
            mLiveCard.unpublish();
            mLiveCard = null;
        }
        super.onDestroy();
    }

    public void ShowPitchDetectionResult(
            final HashMap<Double, Double> frequencies,
            final double pitch) {
        mCallbackCanvas.setDetectionResults(frequencies, pitch);
    }
}