package com.example.coronavirusherdimmunity.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

@SuppressLint("AppCompatCustomView")
public class MyTextView extends TextView {

    public MyTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyTextView(Context context) {
        super(context);
        init();
    }

    public void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/SF-Compact-Display-Regular.otf");
        setTypeface(tf , Typeface.NORMAL);

        Typeface tf_bold = Typeface.createFromAsset(getContext().getAssets(), "fonts/SF-Compact-Display-Bold.otf");
        setTypeface(tf_bold , Typeface.BOLD);

    }
}