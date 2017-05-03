package com.agsoft.notebook.MView;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.agsoft.notebook.Bean.NoteBean;
import com.agsoft.notebook.R;
import com.iflytek.thirdparty.P;

import java.util.ArrayList;

/**
 * Created by 1 on 2016/9/20.
 */
public class MListView extends ListView {
    private static final String TAG = "loglog";
    private static final int LOAD_NUM = 3;//在这里可以设置每次刷新几条过期提醒
    private Context context;
    private View head;
    private int headHeight;
    private float startX, startY, moveX, moveY, dY;
    private LinearLayout.LayoutParams layoutParams;
    private final int REFRESHED = 0, PULL_REFRESH = 1, RELEASE_REFRESH = 2, REFRESHING = 3;
    private int refresh_state;
    private boolean delete;
    private OnLoadActionListener lisenter;
    private TextView tv_time, tv_content, tv_refresh;
    private RelativeLayout rel;

    public MListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    public MListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public MListView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    private void init() {
        head = View.inflate(context, R.layout.fragment_listhead, null);
        tv_time = (TextView) head.findViewById(R.id.tv_time);
        rel = (RelativeLayout) head.findViewById(R.id.rel);
        tv_content = (TextView) head.findViewById(R.id.tv_content);
        tv_refresh = (TextView) head.findViewById(R.id.tv_refresh);
        addHeaderView(head);
        post(new Runnable() {
            public void run() {
                headHeight = head.getHeight();
                layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
                layoutParams.topMargin = -headHeight;
                setLayoutParams(layoutParams);
            }
        });
    }

    private NoteBean noteBean;
    private boolean came = false;
    private boolean b;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = ev.getRawX();
                startY = ev.getRawY();
                aboveListener.onTouch(startX, ev);
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = ev.getRawX();
                moveY = ev.getRawY();
                if (moveX != startX) {
                    if (!came) {
                        came = true;
                        if (Math.abs(moveX - startX) > Math.abs(moveY - startY)) {//横向移动距离大于竖向移动距离 直接返回 不作处理
                            b = true;
                        }
                    }
                    if (b) {
                        aboveListener.onTouch(startX, ev);
                        return true;
                    }
                }
                if (!delete) {
                    if (getFirstVisiblePosition() == 0 && getChildAt(0).getTop() >= 0 && dY >= 0) {//此时显示的是第一个child，并且top == 0,移动距离大于0
                        if (noteBean == null) {// 每一次新的触摸事件开始，noteBean都是null，如果没有消息了，此次触摸事件全程noteBean都会为一个没有值的NoteBean
                            ArrayList<NoteBean> list = lisenter.load(1, 0);
                            //如果list没有返回内容 说明已经没有过期提醒了，不会给noteBean设值
                            if (list.size() == 0) {
                                noteBean = new NoteBean();
                                StringBuilder builder = new StringBuilder();
                                for (int i = 0; i < 10; i++) {
                                    builder.append(new String(Character.toChars(0x1F633)));
                                }
                                tv_time.setText(builder);
                                tv_content.setText(builder);
                                tv_refresh.setText("无过期提醒");
                            } else {
                                noteBean = list.get(0);
                                tv_time.setText(noteBean.getTime());
                                tv_content.setText(noteBean.getNote());
                                tv_refresh.setText("下拉查看过期提醒");
                            }
                        }
                        if (moveY - startY + dY <= 0) {
                            layoutParams.topMargin = -headHeight;
                            dY = 0;
                            startX = moveX;
                            startY = moveY;
                            return true;
                        }
                        double persent = (layoutParams.topMargin + headHeight) / (1.0 * headHeight);
                        tv_time.setAlpha((float) (persent * 0.8));
                        tv_content.setAlpha((float) (persent * 0.8));
                        tv_refresh.setAlpha((float) (persent * 0.8));
                        double v;
                        if (moveY - startY > 0) {
                            v = (moveY - startY) * (1 - persent);//每次move实际滑动的距离
                        } else {
                            v = moveY - startY;
                        }
                        layoutParams.topMargin = layoutParams.topMargin + (int) v;
                        setLayoutParams(layoutParams);
                        dY = (int) v + dY;//此次滑动的总距离
                        if (noteBean.getTime_created() != 0) {//当noteBean里没有值的时候，说明没有过期提醒，所以不需改变刷新状态
                            if (dY > headHeight / 2 && refresh_state != RELEASE_REFRESH) {
                                refresh_state = RELEASE_REFRESH;//如果滑动距离超过总距离的一半 则转为释放刷新状态，否则为下拉刷新状态
                                tv_refresh.setText("释放加载过期提醒");
                            } else if (dY <= headHeight / 2 && refresh_state != PULL_REFRESH) {
                                refresh_state = PULL_REFRESH;
                                tv_refresh.setText("下拉查看过期提醒");
                            }
                        }
                        startX = moveX;
                        startY = moveY;
                        return true;
                    }
                }
                startX = moveX;
                startY = moveY;
                break;
            case MotionEvent.ACTION_UP:
                came = false;
                startX = 0;
                startY = 0;
                if (b) {
                    aboveListener.onTouch(startX, ev);
                    b = false;
                    break;
                }
                if (dY != 0) {
                    dY = 0;//每次抬起手指 则重置此次的滑动距离
                    if (refresh_state == PULL_REFRESH) {
                        layoutParams.topMargin = -headHeight;
                        setLayoutParams(layoutParams);
                        refresh_state = REFRESHED;
                        return true;
                    } else if (refresh_state == REFRESHED) {
                        animation_up_nodata();
                        return true;
                    } else if (refresh_state == RELEASE_REFRESH) {
                        animation_up();
                        refresh_state = REFRESHING;
                        return true;
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    //有刷新内容时的动画
    private void animation_up() {
        final int topMargin = layoutParams.topMargin;
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                layoutParams.topMargin = interpolatedTime == 1 ? -headHeight : (int) (topMargin - (headHeight - tv_content.getHeight() + topMargin) * interpolatedTime);
                requestLayout();
            }
        };
        animation.setDuration(100);
        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                noteBean = null;//加载过期提醒 就把头部需要用的过期提醒置空，如果不置空 下次下拉还是会显示出这条提醒，然而实际上这条提醒已经被刷新出来了
                lisenter.load(LOAD_NUM, 1);//加载过期提醒
                refresh_state = REFRESHED;
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimation(animation);
    }

    private void animation_up_nodata() {
        noteBean = null;
        final int topMargin = layoutParams.topMargin;
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                layoutParams.topMargin = interpolatedTime == 1 ? -headHeight : (int) (topMargin - (headHeight + topMargin) * interpolatedTime);
                requestLayout();
            }
        };
        animation.setDuration(100);
        startAnimation(animation);
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public interface OnLoadActionListener {
        ArrayList<NoteBean> load(int i, int type);//type==0 是头部调用的，type==1 是加载过期提醒调用的
    }

    public void setOnLoadActionListener(OnLoadActionListener l) {
        lisenter = l;
    }

    public interface AboveOnTouchListener {
        void onTouch(float startX, MotionEvent ev);
    }

    private AboveOnTouchListener aboveListener;

    public void setMOnTouchListener(AboveOnTouchListener listener) {
        this.aboveListener = listener;
    }
}
