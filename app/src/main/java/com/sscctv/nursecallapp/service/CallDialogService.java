package com.sscctv.nursecallapp.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sscctv.nursecallapp.R;

import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;

import java.util.Objects;

public class CallDialogService extends IntentService {
    private static final String TAG = CallDialogService.class.getSimpleName();

    private View callView, mFocusPoeView;
    private View.OnTouchListener mViewTouchListener;
    private WindowManager mManager, sManager;
    private WindowManager.LayoutParams mParams;
    private float mTouchX, mTouchY;
    private int mViewX, mViewY;
    private boolean isMove = false;
    private ViewGroup groupCallView;

    public static Context mContext;
    private Core mCore;
    private Call mCall;
    public CallDialogService() {
        super("CallDialogService");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        Log.d(TAG, "onCreate() ");
//        poeHandler.sendEmptyMessage(0);
//        poeFocusHandlers.sendEmptyMessage(0);
        mCore = MainCallService.getCore();

        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert li != null;
        callView = li.inflate(R.layout.frag_call_dialog, null);
        mParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, 500, 0,
                WindowManager.LayoutParams.TYPE_PHONE,
                (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE),
                PixelFormat.TRANSLUCENT);
//        mParams.gravity = Gravity.END;
        mManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        assert mManager != null;
        mManager.addView(callView, mParams);
        callView.setVisibility(View.INVISIBLE);

        callView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isMove = false;
                        mTouchX = event.getRawX();
                        mTouchY = event.getRawY();
                        mViewX = mParams.x;
                        mViewY = mParams.y;
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        isMove = true;
                        int x = (int) (event.getRawX() - mTouchX);
                        int y = (int) (event.getRawY() - mTouchY);
//                        final int num = 5;
//                        if ((x > -num && x < num) && (y > -num && y < num)) {
//                            isMove = false;
//                            break;
//                        }
                        mParams.x = mViewX + x;
                        mParams.y = mViewY + y;
                        mManager.updateViewLayout(callView, mParams);
                        break;
                }
                return true;
            }


        });

    }

//    private final poe_handler poeHandler = new poe_handler(this);
//
//    private static class poe_handler extends Handler {
//        private final WeakReference<PoEIntentService> ref;
//
//        private poe_handler(PoEIntentService test) {
//            ref = new WeakReference<>(test);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//
//            PoEIntentService poEIntentService = ref.get();
//            if (poEIntentService != null) {
////                Log.d(TAG, "poe_handler: " + updateFlag);
//
//                if (updateFlag) {
//                    updateFlag = false;
//                    if (pseLevel) {
//                        mPoeLevel.setText("PoE 48V OUT");
//                    } else {
//                        mPoeLevel.setText("PoE OFF");
//                    }
//                }
//                if (voltInput) {
//                    mPoeLevel.setText(format("PoE : %02d.%02d V", mValue, sValue));
//                }
//            }
//        }
//
//    }
//
//    private final poe_focus_handler poeFocusHandlers = new poe_focus_handler(this);
//
//    private static class poe_focus_handler extends Handler {
//        private final WeakReference<PoEIntentService> ref;
//
//        private poe_focus_handler(PoEIntentService test) {
//            ref = new WeakReference<>(test);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//
//            PoEIntentService poEIntentService = ref.get();
//            if (poEIntentService != null) {
////                Log.d(TAG, "poe_focus_handler: " + updateFlag);
//                if (updateFlag) {
//                    updateFlag = false;
//                    if (pseLevel) {
//                        mFocusPoeLevel.setText("PoE : 48V OUT");
//                    } else {
//                        mFocusPoeLevel.setText("PoE : OFF");
//                    }
//                }
//                if (voltInput) {
//                    mFocusPoeLevel.setText(format("PoE : %02d.%02d V", mValue, sValue));
//                }
//            }
//        }
//
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, TAG + " Start");
        callView.setVisibility(View.VISIBLE);
//        viewTouchHandler();

        if (mCore != null) {
            for (Call call : mCore.getCalls()) {
                if (Call.State.IncomingReceived == call.getState()
                        || Call.State.IncomingEarlyMedia == call.getState()) {
                    mCall = call;
                    break;
                }
            }
        }

        TextView textView = callView.findViewById(R.id.dg_call_txt);
        textView.setText(mCall.getRemoteAddress().getUsername());

        Button button = callView.findViewById(R.id.dg_call_btn);
        button.setOnClickListener(view -> {
             Call curCall = mCore.getCurrentCall();
             curCall.pause();

            CallParams params = mCore.createCallParams(mCall);
            mCall.acceptWithParams(params);
            Log.d(TAG, "Select!!");
        });

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        Log.i(TAG, TAG + " Destroy");
        if (callView != null) {
            mManager.removeView(callView);
            callView = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }


    @SuppressLint("ClickableViewAccessibility")
    public void viewTouchHandler() {
        mViewTouchListener = (v, event) -> {
            int width = ((ViewGroup) v.getParent()).getWidth() - v.getWidth();
            int height = ((ViewGroup) v.getParent()).getHeight() - v.getHeight();

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mTouchX = event.getX();
                mTouchY = event.getY();
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                v.setX(event.getRawX() - mTouchX);
                v.setY(event.getRawY() - (mTouchY + v.getHeight()));
                //  Log.i("Tag2", "Action Down " + me.getRawX() + "," + me.getRawY());
            } else if (event.getAction() == MotionEvent.ACTION_UP) {

                if (v.getX() > width && v.getY() > height) {
                    v.setX(width);
                    v.setY(height);
                } else if (v.getX() < 0 && v.getY() > height) {
                    v.setX(0);
                    v.setY(height);
                } else if (v.getX() > width && v.getY() < 0) {
                    v.setX(width);
                    v.setY(0);
                } else if (v.getX() < 0 && v.getY() < 0) {
                    v.setX(0);
                    v.setY(0);
                } else if (v.getX() < 0 || v.getX() > width) {
                    if (v.getX() < 0) {
                        v.setX(0);
                        v.setY(event.getRawY() - mTouchY - v.getHeight());
                    } else {
                        v.setX(width);
                        v.setY(event.getRawY() - mTouchY - v.getHeight());
                    }
                } else if (v.getY() < 0 || v.getY() > height) {
                    if (v.getY() < 0) {
                        v.setX(event.getRawX() - mTouchX);
                        v.setY(0);
                    } else {
                        v.setX(event.getRawX() - mTouchX);
                        v.setY(height);
                    }
                }
            }
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    isMove = false;
//                    mTouchX = event.getRawX();
//                    mTouchY = event.getRawY();
//                    mViewX = mParams.x;
//                    mViewY = mParams.y;
//                    break;
//                case MotionEvent.ACTION_UP:
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    isMove = true;
//                    int x = (int) (event.getRawX() - mTouchX);
//                    int y = (int) (event.getRawY() - mTouchY);
//                    final int num = 5;
//                    if ((x > -num && x < num) && (y > -num && y < num)) {
//                        isMove = false;
//                        break;
//                    }
//                    mParams.x = mViewX + x;
//                    mParams.y = mViewY + y;
//                    mManager.updateViewLayout(callView, mParams);
//                    break;
//            }
            return true;
        };

        callView.setOnTouchListener(mViewTouchListener);
    }
}
