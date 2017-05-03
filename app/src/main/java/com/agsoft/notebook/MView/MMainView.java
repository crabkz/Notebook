package com.agsoft.notebook.MView;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by 1 on 2016/10/8.
 */
public class MMainView extends PercentRelativeLayout {
    private static final String TAG = "loglog";
    private float startX, startY, moveX, moveY;

    public MMainView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e(TAG, "onTouchEvent: MMAINVIEW");
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    private boolean dispatch = true;
    private boolean isgoing = false;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = ev.getX();
                startY = ev.getY();
                Log.e(TAG, "dispatchTouchEvent:  ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e(TAG, "dispatchTouchEvent: " + ev.getX() + "    " + moveX);
                if (!isgoing) {
                    moveX = ev.getX();
                    moveY = ev.getY();
                    if ((int) moveX != (int) startX && (int) moveY != (int) startY) {
                        isgoing = true;
                        if (Math.abs(moveX - startX) > Math.abs(moveY - startY)) {//横向移动距离大于竖向移动距离 直接返回 不作处理
                            dispatch = false;
                            return dispatch;
                        }
                    }
                }
                if (!dispatch)
                    listener.onTouch(startX, ev);
                break;
            case MotionEvent.ACTION_UP:
                Log.e(TAG, "dispatchTouchEvent: ACTION_UP");
                final boolean j = dispatch;
                dispatch = true;
                isgoing = false;
                if (!j) {
                    listener.onTouch(startX, ev);
                    return j;
                }
        }
        return super.dispatchTouchEvent(ev);
    }

    interface MOnTouchListener {
        void onTouch(float startX, MotionEvent ev);
    }

    private MOnTouchListener listener;

    public void setMOnTouchListener(MOnTouchListener listener) {
        this.listener = listener;
    }

}
