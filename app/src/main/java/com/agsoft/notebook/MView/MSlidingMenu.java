package com.agsoft.notebook.MView;


import android.app.Activity;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

/**
 * Created by 1 on 2016/9/23.
 */
public class MSlidingMenu {
    private static final String TAG = "loglog";
    private Activity activity;
    public View above;
    private View bottom;
    private View shadow;
    private float startX;
    private int widthPixels;
    private int shadow_width;
    private boolean isShowing;//菜单是否打开
    private final int MENU_WIDTH = 300;
    private final int MENU_HIDE_WIDTH = 150;
    private final float MENU_PERSENT = MENU_HIDE_WIDTH * 1.0f / MENU_WIDTH;
    private final int DURATION = 300;
    private GestureDetectorCompat gestureDetector;
    private MOnTouchListener mOnTouchListener;

    public MSlidingMenu(Activity activity, View menu, View main, final View shadow) {
        this.activity = activity;
        above = main;
        bottom = menu;
        this.shadow = shadow;
        widthPixels = activity.getResources().getDisplayMetrics().widthPixels;
        ViewGroup.LayoutParams layoutParams = bottom.getLayoutParams();
        layoutParams.width = MENU_WIDTH;
        shadow.post(new Runnable() {
            @Override
            public void run() {
                shadow_width = shadow.getWidth();
            }
        });
        bottom.requestLayout();
        mOnTouchListener = new MOnTouchListener();
        above.setOnTouchListener(mOnTouchListener);
        bottom.setOnTouchListener(mOnTouchListener);
        gestureDetector = new GestureDetectorCompat(activity, new MGestureDetector());
    }

    private void closeMenu() {
        isShowing = false;
        final int left = above.getLeft();
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                int dX = left - (int) (left * interpolatedTime);
                above.setLeft(dX);
                above.setRight(widthPixels + dX);
                bottom.setLeft((int) (dX * MENU_PERSENT - MENU_HIDE_WIDTH));
                bottom.setRight((int) (dX * MENU_PERSENT + widthPixels - MENU_HIDE_WIDTH));
                shadow.setLeft(dX - shadow_width);
                shadow.setRight(dX);
            }
        };
        animation.setDuration(left * DURATION / MENU_WIDTH);
        animation.setInterpolator(new DecelerateInterpolator());
        above.startAnimation(animation);
    }

    public void openMenu() {
        isShowing = true;
        final int left = above.getLeft();
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                int dX = left + (int) ((MENU_WIDTH - left) * interpolatedTime);
                above.setLeft(dX);
                above.setRight(widthPixels + dX);
                bottom.setLeft((int) (dX * MENU_PERSENT - MENU_HIDE_WIDTH));
                bottom.setRight((int) (dX * MENU_PERSENT + widthPixels - MENU_HIDE_WIDTH));
                shadow.setLeft(dX - shadow_width);
                shadow.setRight(dX);
            }
        };
        animation.setDuration((MENU_WIDTH - left) * DURATION / MENU_WIDTH);
        animation.setInterpolator(new DecelerateInterpolator());
        above.startAnimation(animation);
    }

    public boolean isMenuShowing() {
        return isShowing;
    }

    public void toggle() {
        if (isShowing) closeMenu();
        else openMenu();
    }

    public void touch(float x, MotionEvent ev) {
        startX = x;
        mOnTouchListener.onTouch(above, ev);
    }

    class MGestureDetector implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            if (motionEvent.getRawX() - motionEvent1.getRawX() < 0) openMenu();
            else closeMenu();
            return true;
        }
    }

    private int above_left, above_right, bottom_left, bottom_right, shadow_left, shadow_right;

    class MOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (gestureDetector.onTouchEvent(event)) {
                return false;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getRawX();
                    above_left = above.getLeft();
                    above_right = above.getRight();
                    bottom_left = (int) (above_left * MENU_PERSENT - MENU_HIDE_WIDTH);
                    bottom_right = (int) (above_left * MENU_PERSENT + widthPixels - MENU_HIDE_WIDTH);
                    shadow_left = above_left - shadow_width;
                    shadow_right = above_left;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int dX = (int) (event.getRawX() - startX);
                    if (above_left + dX > 0 && above_left + dX < MENU_WIDTH) {
                        above.setLeft(above_left + dX);
                        above.setRight(above_right + dX);
                        bottom.setLeft((int) (bottom_left + dX * MENU_PERSENT));
                        bottom.setRight((int) (bottom_right + dX * MENU_PERSENT));
                        shadow.setLeft(shadow_left + dX);
                        shadow.setRight(shadow_right + dX);
                    } else {
                        startX = event.getRawX();
                        above_left = above.getLeft();
                        above_right = above.getRight();
                        bottom_left = bottom.getLeft();
                        bottom_right = bottom.getRight();
                        shadow_left = shadow.getLeft();
                        shadow_right = shadow.getRight();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    Log.e(TAG, "onTouch: " + above.getLeft());
                    if (above.getLeft() == MENU_WIDTH) {
                        closeMenu();
                    } else if (above.getLeft() * 2 < MENU_WIDTH) {
                        closeMenu();
                    } else if (above.getLeft() * 2 > MENU_WIDTH) {
                        openMenu();
                    }
                    break;
            }
            return true;
        }
    }
}
