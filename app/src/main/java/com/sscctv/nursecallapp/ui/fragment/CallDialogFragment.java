package com.sscctv.nursecallapp.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.FragCallDialogBinding;

import java.util.Objects;

public class CallDialogFragment extends DialogFragment {

    private final static String TAG = CallDialogFragment.class.getSimpleName();
    private FragCallDialogBinding mBinding;
    private View mainView;
    private View.OnTouchListener mViewTouchListener;
    private float mTouchX, mTouchY;
    private int mViewX, mViewY;
    private static int mValue, sValue, mFocus;
    private boolean isMove = false;
    private WindowManager.LayoutParams mParams, sParams;
    private WindowManager mManager;
    private int width;
    private int height;
    public CallDialogFragment() {

    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.frag_call_dialog, container, false);

        Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).requestFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getDialog().getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().setCancelable(false);
        getDialog().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);


        getDialog().setOnCancelListener(dialogInterface -> {
            Log.d(TAG, "onCancel");
        });

        getDialog().setOnShowListener(dialogInterface -> {
            Log.d(TAG, "onShow");
        });

        getDialog().setOnDismissListener(dialogInterface -> {
            Log.d(TAG, "onDismiss");
        });


        mBinding.getRoot().setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTouchX = view.getX() - motionEvent.getRawX();
                    mTouchY = view.getY() - motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    view.animate()
                            .x(motionEvent.getRawX() + mTouchX)
                            .y(motionEvent.getRawY() + mTouchY)
                            .setDuration(0)
                            .start();
            }
            return true;
        });

//
//        setViewTouchListener();  mParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.WRAP_CONTENT, 0, 0,
//                WindowManager.LayoutParams.TYPE_PHONE,
//                (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE),
//                PixelFormat.TRANSLUCENT);
//        mParams.gravity = Gravity.TOP | Gravity.CENTER;
//        mManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
//        mManager.addView(mBinding.getRoot(), mParams);
        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
//        WindowManager manager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
//        Display display = manager.getDefaultDisplay();
//        Point size = new Point(width, height);
//        display.getSize(size);
//        Log.d(TAG, "X: " + size.x + " Y: " + size.y);

        Window window = getDialog().getWindow();
        window.setGravity(Gravity.TOP|Gravity.END);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 300;
        params.y = 100;
        window.setAttributes(params);

        Log.d(TAG, "Why: " + getDialog().getWindow().getAttributes().gravity);
    }
}
