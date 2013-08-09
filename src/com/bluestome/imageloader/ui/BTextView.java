
package com.bluestome.imageloader.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

public class BTextView extends TextView {
    private static final String NAMESPACE = "http://www.ywlx.net/apk/res/easymobi";
    private static final String ATTR_ROTATE = "rotate";
    private static final int DEFAULTVALUE_DEGREES = 0;

    private int degrees;

    public BTextView(Context context, AttributeSet attrs) {
        super(context);
        degrees = attrs.getAttributeIntValue(NAMESPACE, ATTR_ROTATE, DEFAULTVALUE_DEGREES);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.rotate(degrees, getMeasuredWidth() / 2, getMeasuredHeight() / 2);
        super.onDraw(canvas);
    }
}
