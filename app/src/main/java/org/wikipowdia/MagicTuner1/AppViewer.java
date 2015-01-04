package org.wikipowdia.MagicTuner1;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

/****************************************
 * creates the text view
 *
 *
 *
 *
 */

public class AppViewer extends FrameLayout {
    private final TextView mTextView;
    public AppViewer(Context context) {
        this(context, null, 0);
    }
    public AppViewer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public void start() {
    }
    public AppViewer(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        LayoutInflater.from(context).inflate(R.layout.start, this);
        mTextView =  (TextView) findViewById(R.id.hello_view);
        mTextView.setText("Hello Glass!");
    }
}