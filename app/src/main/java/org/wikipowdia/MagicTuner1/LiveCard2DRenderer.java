
package org.wikipowdia.MagicTuner1;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.View;

import com.google.android.glass.timeline.DirectRenderingCallback;
import com.mattyork.colours.Colour;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


/****************************************
 * renders graphics directly to the canvas within the live card
 * stock bouncing ball and star graphic from google example
 *
 *
 *
 */

public class LiveCard2DRenderer implements DirectRenderingCallback {
    private static final String TAG = "LiveCardRenderer";
    private static final long FRAME_TIME_MILLIS = 33; // about 30 FPS
    private SurfaceHolder mHolder;
    private boolean mPaused;
    private RenderThread mRenderThread;
    private int canvasWidth;
    private int canvasHeight;
    private int diffX = 25;
    private int incY = 1;
    private float bouncingX;
    private float bouncingY;
    private double angle;
    private Paint paint;
    private Path path;
    int[] complementaryColors;
    private GradientDrawable mDrawable;
    private HashMap<Double, Double> frequencies_;
    private double pitch_;
    private PitchDetectionRepresentation representation_;
    private Handler handler_;
    private Timer timer_;
    private final static int UI_UPDATE_MS = 100;

    private double frequencies[] = new double[] {27.50, 29.14, 30.87, // A0, A0#, B0
            32.70, 34.65, 36.71, 38.89, 41.20, 43.65, 46.25, 49.00, 51.91, 55.00, 58.27,
            61.74, // C1 - B1
            // C, C#, D, D#, E, F, F#, G, G#, A, A#, B
            65.51, 69.30, 73.42, 77.78, 82.41, 87.31, 92.50, 98.00, 103.83, 110.00, 116.54,
            123.47, // C2 - B2
            130.81, 138.59, 146.83, 155.56, 164.81, 174.61, 185.00, 196.00, 207.65, 220.00,
            233.08, 246.94, // C3 - B3
            261.63, 277.18, 293.67, 311.13, 329.63, 349.23, 369.99, 392.00, 415.30, 440.00,
            466.16, 493.88, // C4 - B4
            523.25, 554.37, 587.33, 622.25, 659.26, 698.46, 739.99, 783.99, 830.61, 880.00,
            932.33, 987.77, // C5 - B5
            1046.5, 1108.7, 1174.7, 1244.5, 1318.5, 1396.9, 1480.0, 1568.0, 1661.2, 1760.0,
            1864.7, 1975.5, // C6 - B6
            2093.0, 2217.5, 2349.3, 2489.0, 2637.0, 2793.0, 2960.0, 3136.0, 3322.4, 3520.0,
            3729.3, 3951.1, // C7 - B7
            4186.0}; // C8


    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
        complementaryColors = Colour.colorSchemeOfType(Colour.seafoamColor(), Colour.ColorScheme.ColorSchemeTriad);
        mDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, complementaryColors);
        bouncingX = canvasWidth / 2;
        bouncingY = canvasHeight / 2;
        angle = - Math.PI/4.0; //(2.0 * Math.PI) * (double) (Math.random() * 360) / 360.0;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);
        paint.setStyle(Paint.Style.STROKE);
        path = new Path();
        mHolder = holder;
        updateRendering();
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        handler_ = new Handler();
        timer_ = new Timer();
        timer_.schedule(new TimerTask() {
                            public void run() {
                                handler_.post(new Runnable() {
                                    public void run() {
                                        //invalidate();
                                    }
                                });
                            }
                        },
                UI_UPDATE_MS ,
                UI_UPDATE_MS );

    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder = null;
        updateRendering();
    }
    @Override
    public void renderingPaused(SurfaceHolder holder, boolean paused) {
        mPaused = paused;
        updateRendering();
    }

    public void setDetectionResults(final HashMap<Double, Double> frequencies, double pitch) {
        frequencies_ = frequencies;
        pitch_ = pitch;
    }
    /**
     * Start or stop rendering according to the timeline state.
     */
    private synchronized void updateRendering() {
        boolean shouldRender = (mHolder != null) && !mPaused;
        boolean rendering = mRenderThread != null;
        if (shouldRender != rendering) {
            if (shouldRender) {
                mRenderThread = new RenderThread(this);
                mRenderThread.start();
            } else {
                mRenderThread.quit();
                mRenderThread = null;
            }
        }
    }
    /**
     * Draws the view in the SurfaceHolder's canvas.
     */
    public void drawInCanvas(View view) {
        Canvas canvas;
        try {
            canvas = mHolder.lockCanvas();
        } catch (Exception e) {
            return;
        }
        if (canvas != null) {
            // just a little math to calculate the new position of the bouncing ball
            bouncingX += diffX;
            bouncingY += diffX * Math.tan(angle);
            bouncingY *= incY;
            mDrawable.setBounds(0,0,canvasWidth,canvasHeight);
            mDrawable.draw(canvas);
            //canvas.drawColor(Color.BLACK);
            canvas.drawCircle(bouncingX, bouncingY, 20, paint);
            // change the direction and/or angle if out of bounds
            if (bouncingX > canvasWidth || bouncingX < 0) {
                diffX = -diffX;
                angle = -angle;
            }
            else if (bouncingY > canvasHeight || bouncingY < 0) {
                angle = -angle;
            }
            float mid = canvasWidth / 2;
            float min = canvasHeight;
            float half = min / 2;
            mid -= half;
            paint.setStrokeWidth(min / 10);
            paint.setStyle(Paint.Style.STROKE);
            path.reset();
            paint.setStyle(Paint.Style.FILL);
            path.moveTo(mid + half * 0.5f, half * 0.84f);
            path.lineTo(mid + half * 1.5f, half * 0.84f);
            path.lineTo(mid + half * 0.68f, half * 1.45f);
            path.lineTo(mid + half * 1.0f, half * 0.5f);
            path.lineTo(mid + half * 1.32f, half * 1.45f);
            path.lineTo(mid + half * 0.5f, half * 0.84f);
            path.close();
            representation_ = new PitchDetectionRepresentation(pitch_);
            DrawCurrentFrequency(canvas, 20, 50);

            canvas.drawPath(path, paint);
            mHolder.unlockCanvasAndPost(canvas);
        }
    }
    /**
     * Redraws in the background.
     */

    private void DrawCurrentFrequency(Canvas canvas, int x, int y) {
        if (representation_ == null) {
            Paint paint = new Paint();
            paint.setARGB(255, 200, 200, 200);
            paint.setTextSize(18);
            canvas.drawText("Pull a string on your guitar.", 20, 40, paint);
            return;
        }
        final int alpha = representation_.GetAlpha();
        if (alpha == 0) return;
        Paint paint = new Paint();
        paint.setARGB(alpha, 200, 0, 0);
        paint.setTextSize(35);

        //canvas.drawText(Math.round(representation_.pitch * 10) / 10.0 + " Hz", 20, 40, paint);
        double freq = Math.round(representation_.pitch * 10) / 10.0;
        int index = -1;
        for (int i=0; i<frequencies.length-1; i++) {
            if (frequencies[i] <= freq && freq <= frequencies[i+1]) {
                if (freq-frequencies[i] <= frequencies[i+1]-freq)
                    index = i;
                else
                    index = i+1;
                break;
            } }
        if (index==-1) {
            if (freq<frequencies[0] && (frequencies[0]-freq<2.0))
                index = 0;
            else if (freq>frequencies[frequencies.length-1] && (freq-frequencies[frequencies.
                    length-1]<100.0))
                index = frequencies.length - 1;
        }
        if (index==-1)
            canvas.drawText(Math.round(representation_.pitch * 10) / 10.0 +
                    " Hz", 20, 40, paint);
        else {
            String noteString;
            if (index == 0) noteString = "A0";
            else if (index == 1) noteString = "A0 Sharp";
            else if (index == 2) noteString = "B0";
            else {
                int n = (int) ((index-3) / 12);
                int m = (int) ((index-3) % 12);
                String[] notes  = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#",
                        "A", "A#", "B"};
                noteString = notes[m];
                noteString = noteString.substring(0, 1) + (n+1) + (notes[m].length()==1?"":"#");
            }
            canvas.drawText(noteString + " - " + freq + " Hz", 20, 40, paint);
        }
    }

    private class RenderThread extends Thread {
        private boolean mShouldRun;
        LiveCard2DRenderer mRenderer;
        /**
         * Initializes the background rendering thread.
         */
        public RenderThread(LiveCard2DRenderer renderer) {
            mShouldRun = true;
            mRenderer = renderer;

        }


        /**
         * Returns true if the rendering thread should continue to run.
         *
         * @return true if the rendering thread should continue to run
         */
        private synchronized boolean shouldRun() {
            return mShouldRun;
        }
        /**
         * Requests that the rendering thread exit at the next opportunity.
         */
        public synchronized void quit() {
            mShouldRun = false;
        }
        @Override
        public void run() {
            while (shouldRun()) {
                mRenderer.drawInCanvas(null);
                SystemClock.sleep(FRAME_TIME_MILLIS);
            }
        }
    }
}