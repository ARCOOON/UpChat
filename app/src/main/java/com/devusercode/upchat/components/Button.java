package com.devusercode.upchat.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.devusercode.upchat.R;

public class Button extends AppCompatButton {
    private float cornerRadius;
    private float elevation;

    public Button(@NonNull Context context) {
        super(context);
        init(null);
    }

    public Button(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public Button(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @SuppressLint("CustomViewStyleable")
    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.RoundedButton);
            cornerRadius = typedArray.getDimension(R.styleable.RoundedButton_cornerRadius, 0);
            elevation = typedArray.getDimension(R.styleable.RoundedButton_elevation, 0);
            typedArray.recycle();
        }

        GradientDrawable shapeDrawable = new GradientDrawable();
        shapeDrawable.setCornerRadius(cornerRadius);
        setBackground(shapeDrawable);
        setElevation(elevation);
    }
}
