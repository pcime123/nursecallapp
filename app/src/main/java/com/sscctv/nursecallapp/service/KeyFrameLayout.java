package com.sscctv.nursecallapp.service;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.sscctv.nursecallapp.ui.activity.MainActivity;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;

public class KeyFrameLayout extends ConstraintLayout {
    private MyKeyEventCallbackListener myKeyEventCallbackListener;
    private Context mContext;
    public KeyFrameLayout(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public KeyFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

    }

    public KeyFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;

    }

    public interface MyKeyEventCallbackListener {
        void onKeyEvent(KeyEvent event);
    }

    public void setMyKeyEventCallbackListener(MyKeyEventCallbackListener callback) {
        this.myKeyEventCallbackListener = callback;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d("KeyFrame", "KeyEvent: " + event);
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    if (myKeyEventCallbackListener != null)
                        myKeyEventCallbackListener.onKeyEvent(event);
                    break;
                case KeyEvent.KEYCODE_CALL:
                    NurseCallUtils.startNewIntent(mContext, MainActivity.class);
                    break;
                default:
            }
        }

        return super.dispatchKeyEvent(event);
    }
}

