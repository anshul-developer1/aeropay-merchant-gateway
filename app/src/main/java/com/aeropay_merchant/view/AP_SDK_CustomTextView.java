package com.aeropay_merchant.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.aeropay_merchant.R;


public class AP_SDK_CustomTextView extends AppCompatTextView {
    private String typeFace;
    private final Context context;
    private ColorStateList textColors;

    public AP_SDK_CustomTextView(Context context, String typeface) {
        super(context);
        this.context = context;
        setCTypeFace(typeface);
        textColors = getTextColors();
    }

    public AP_SDK_CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.TextView);

        String typeface = a.getString(R.styleable.TextView_ctypeface);
        setCTypeFace(typeface);
        textColors = getTextColors();
    }

    public String getCTypeFace() {
        return typeFace;
    }

    public void setCTypeFace(String tf) {
        typeFace = tf;
        if (tf != null) {
            AP_SDK_FontManager.getInstance(context).setTypeFace(this, tf);
        }
    }
}
