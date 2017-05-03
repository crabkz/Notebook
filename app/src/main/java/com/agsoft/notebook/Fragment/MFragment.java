package com.agsoft.notebook.Fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.agsoft.notebook.Bean.NoteBean;
import com.agsoft.notebook.MView.MListView;
import com.agsoft.notebook.Activity.MainActivity;
import com.agsoft.notebook.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by 1 on 2016/8/29.
 */
public class MFragment extends Fragment {
    private static final String TAG = "loglog";
    private final int DURATION_SHORT = 200;
    private final int DURATION = 400;
    private MainActivity activity;
    private LinearLayout inflate;
    private MListView lv;
    private Adapter adapter;
    private ArrayList<Integer> expandList;
    private int widthPixels;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    animation_delete_open(++k);
                    break;
                case 1:
                    animation_delete_close(--k);
                    break;
                case 2:
                    /**
                     * 打开关闭（expand(),collapse()）都会更新expandList的储值，而刷新 剩余时间 就是根据expandList是否有值去刷新
                     * 的，如果有值，再经过一系列判断确定需不需要刷新
                     */
                    if (expandList.size() == 0) {//如果没有position处于打开状态（也就是expandList==0），则停止执行以下刷新代码
                        return;
                    }
                    long startMillis = System.currentTimeMillis();
                    int first = lv.getFirstVisiblePosition();
                    int last = lv.getLastVisiblePosition();
                    for (int i : expandList) {//遍历每一个目前处于打开状态的position
                        if (i >= j) {//如果是过期提醒，则不会用去查找更新tv_surplus
                            if (i >= first && i <= last) {//如果i处于可见范围之内，则说明需要更新，不在则不更新
                                for (int j = 1; j < lv.getChildCount(); j++) {//查询每一个child的tag中的position，以此确定需要更新的child
                                    ViewHolder holder = (ViewHolder) lv.getChildAt(j).getTag();
                                    int position = (int) holder.rel.getTag();
                                    if (position == i) {//如果tag和i相同则说明此child是需要更新tv_surplus的child
                                        settv_surplus(holder.tv_surplus, i);//调用方法更新剩余时间
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    int delayed = (int) (1000 - (System.currentTimeMillis() - startMillis));//减掉中间代码的执行时间 减少误差
                    handler.sendEmptyMessageDelayed(2, delayed);
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        inflate = (LinearLayout) inflater.inflate(R.layout.fragment_action, null);
        lv = (MListView) inflate.findViewById(R.id.lv);
        lv.setMOnTouchListener(new MListView.AboveOnTouchListener() {
            @Override
            public void onTouch(float startX, MotionEvent ev) {
                activity.mSlidingMenu.touch(startX, ev);
            }
        });
        init();
        return inflate;
    }

    /**
     * 初始化数据
     */
    private int j;//当前加载到了第几个过期提醒

    private void init() {
        list = new ArrayList<>();
        expandList = new ArrayList<>();
        widthPixels = getResources().getDisplayMetrics().widthPixels;
        getAction();
        adapter = new Adapter();
        lv.setAdapter(adapter);
        lv.setOnLoadActionListener(new MOnLoadActionListener());
    }

    private void getAction() {
        list.clear();
        Cursor query = activity.db.query("clock", null, null, null, null, null, "time asc");
        while (query.moveToNext()) {
            long time_millis = query.getLong(query.getColumnIndex("time_millis"));
            long time_created = query.getLong(query.getColumnIndex("time_created"));
            String note = query.getString(query.getColumnIndex("note"));
            String time = query.getString(query.getColumnIndex("time"));
            if (time_millis < System.currentTimeMillis()) {
                ContentValues values = new ContentValues();
                values.put("note", note);
                values.put("time", time);
                values.put("time_millis", time_millis);
                values.put("time_created", time_created);
                activity.db.insert("clocked", null, values);
                activity.db.delete("clock", "_id = ?", new String[]{String.valueOf(query.getInt(query.getColumnIndex("_id")))});
            } else {
                NoteBean noteBean = new NoteBean();
                noteBean.setNote(note);
                noteBean.setTime(time);
                noteBean.setTime_millis(time_millis);
                noteBean.setTime_created(time_created);
                list.add(noteBean);
            }
        }
    }

    public void refresh() {
        getAction();
        adapter.notifyDataSetChanged();
    }

    /**
     * ListView的Adapter
     */
    private ArrayList<NoteBean> list;

    class Adapter extends BaseAdapter {
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if (view == null) {
                view = View.inflate(activity, R.layout.fragment_item, null);
                holder = new ViewHolder(view);
                holder.rel.setOnClickListener(new MOnClickListener());
                holder.iv_delete.setOnClickListener(new MOnClickListener());
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.rel.setTag(i);//将目前此holder代表的position存到tag中，以便点击到holder的时候知道点击条目的position
            holder.iv_delete.setTag(i);
            holder.tv_content_all.setText(list.get(i).getNote());
            holder.tv_content.setText(list.get(i).getNote());
            holder.tv_time.setText(settv_time(i));
            if (delete && holder.iv_delete.getVisibility() == View.GONE) {
                holder.iv_delete.setVisibility(View.VISIBLE);
            } else if (!delete && holder.iv_delete.getVisibility() == View.VISIBLE) {
                holder.iv_delete.setVisibility(View.GONE);
            }
            if (i < j) {//把加载出来的过期提醒的字体颜色修改掉 做个区分
                holder.tv_content.setTextColor(getResources().getColor(R.color.colorOrange));
            } else {
                holder.tv_content.setTextColor(Color.WHITE);
            }
            if (expandList.contains(i)) {
                settv_surplus(holder.tv_surplus, i);//刷新剩余响铃时间
                if (holder.rel_expand.getTag() == null) {
                    holder.rel_expand.setTag(true);
                    holder.rel_expand.setVisibility(View.VISIBLE);//打开前先设置为VISIBLE
                    holder.rel_expand.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                } else {
                    if (!(boolean) holder.rel_expand.getTag()) {
                        holder.rel_expand.setTag(true);
                        holder.rel_expand.setVisibility(View.VISIBLE);//打开前先设置为VISIBLE
                        holder.rel_expand.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    } else {
                        holder.rel_expand.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    }
                }
            } else {
                if (holder.rel_expand.getTag() != null) {
                    if ((boolean) holder.rel_expand.getTag()) {
                        holder.rel_expand.setTag(false);
                        holder.rel_expand.setVisibility(View.INVISIBLE);
                        holder.rel_expand.getLayoutParams().height = 0;
                    }
                }
            }
            return view;
        }
    }

    /**
     * ViewHolder
     */
    class ViewHolder {
        TextView tv_content_all;
        TextView tv_content;
        TextView tv_time;
        TextView tv_surplus;
        ImageView iv_delete;
        RelativeLayout rel_expand;
        RelativeLayout rel;

        public ViewHolder(View view) {
            tv_content_all = (TextView) view.findViewById(R.id.tv_content_all);
            tv_time = (TextView) view.findViewById(R.id.tv_time);
            tv_content = (TextView) view.findViewById(R.id.tv_content);
            tv_surplus = (TextView) view.findViewById(R.id.tv_surplus);
            iv_delete = (ImageView) view.findViewById(R.id.iv_delete);
            rel = (RelativeLayout) view.findViewById(R.id.rel);
            rel_expand = (RelativeLayout) view.findViewById(R.id.rel_expand);
        }
    }

    /**
     * 条目的点击事件
     */
    class MOnClickListener implements View.OnClickListener {
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.rel:
                    RelativeLayout rel_expand = (RelativeLayout) view.findViewById(R.id.rel_expand);
                    int i = (int) view.getTag();
                    if (expandList.contains(i)) {
                        expandList.remove((Integer) i);//加入或移除到expandList的i是需要减去head的，因为是要在adapter里用到的，而adapter里的i是不包括head的。
                        collapse(rel_expand);
                    } else {
                        expandList.add(i);
                        expand(rel_expand, i);
                    }
                    break;
                case R.id.iv_delete:
                    int position = (int) view.getTag();
                    if (position < j) {
                        activity.db.delete("clocked", "time_created = ?", new String[]{String.valueOf(list.get(position).getTime_created())});
                    } else {
                        activity.db.delete("clock", "time_created = ?", new String[]{String.valueOf(list.get(position).getTime_created())});

                    }
                    list.remove(position);
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    }


    public boolean delete;//目前是否处于删除状态 ，true 删除状态

    public void setDelete(boolean delete) {
        this.delete = delete;
        lv.setDelete(delete);
    }

    public boolean getDelete() {
        return delete;
    }

    /**
     * 删除按钮弹出的动画
     */
    private int k;//目前执行到的第几个child
    private boolean isRun;//标志此时删除动画是否在执行中
    private int measuredWidth;

    public void animation_delete_open(int i) {
        k = i;
        if (i < lv.getChildCount()) {
            if (i == 1) {
                setDelete(true);
                isRun = true;
            }
            final ImageView delete = (ImageView) lv.getChildAt(i).findViewById(R.id.iv_delete);
            ViewGroup.LayoutParams layoutParams = delete.getLayoutParams();
            if (measuredWidth == 0) {
                measuredWidth = layoutParams.width;
            }
            layoutParams.width = 0;
            delete.setVisibility(View.VISIBLE);
            Animation animation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    delete.getLayoutParams().width = interpolatedTime == 1 ? measuredWidth : (int) (measuredWidth * interpolatedTime);
                    delete.requestLayout();
                }
            };
            animation.setInterpolator(new LinearInterpolator());
            animation.setDuration(200);
            if (i == lv.getChildCount() - 1) {
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        isRun = false;
                        Log.e(TAG, "onAnimationEnd: " + isRun);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
            }
            delete.startAnimation(animation);
            handler.sendEmptyMessageDelayed(0, 20);
        }
    }

    public void animation_delete_close(final int i) {
        k = i;
        if (i >= 1) {
            if (i == lv.getChildCount() - 1) {
                setDelete(false);
                isRun = true;
            }
            final ImageView delete = (ImageView) lv.getChildAt(i).findViewById(R.id.iv_delete);
            final ViewGroup.LayoutParams layoutParams = delete.getLayoutParams();
            final int measuredWidth = layoutParams.width;
            Animation animation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    delete.getLayoutParams().width = interpolatedTime == 1 ? 0 : (int) (measuredWidth * (1 - interpolatedTime));
                    delete.requestLayout();
                }
            };
            animation.setInterpolator(new LinearInterpolator());
            animation.setDuration(200);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (i == 1)
                        isRun = false;
                    delete.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            delete.startAnimation(animation);
            handler.sendEmptyMessageDelayed(1, 20);
        } else {
            setDelete(false);
        }
    }

    /**
     * 关闭
     *
     * @param
     */

    private void collapse(final RelativeLayout rel) {
        TextView v = (TextView) rel.findViewById(R.id.tv_content_all);
        TextView tv_surplus = (TextView) rel.findViewById(R.id.tv_surplus);
        ViewGroup.LayoutParams layoutParams = tv_surplus.getLayoutParams();//得到tv_surplus的layoutParams是为了获取它的宽度从而算出tv_content_all的宽度
        rel.setTag(false);
        Paint paint = new Paint();
        paint.setTextSize(v.getTextSize());
        int text_width = (int) paint.measureText(v.getText().toString());// 得到总体长度
        int h = text_width / (widthPixels - v.getPaddingLeft() - v.getPaddingRight() - layoutParams.width) + 1;//算出字符有多少行
        float targetHeight = line_height * h + 10 + v.getPaddingTop() + v.getPaddingBottom();//根据单行高度+左右padding+10（TextView最小高度），得出最终会显示的真实高度)
        final int actualHeight = v.getHeight();//目前实际高度
        int duration;
        if (h > 2)
            duration = (int) (actualHeight / targetHeight * DURATION);
        else
            duration = (int) (actualHeight / targetHeight * DURATION_SHORT);

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) rel.setVisibility(View.INVISIBLE);
                rel.getLayoutParams().height = (int) (actualHeight * (1 - interpolatedTime * interpolatedTime));//乘以两次interpolatedTime是为了个人一种有加速度的感觉
                rel.requestLayout();
            }
        };
        //Log.e(TAG, "collapse: " + duration + "  " + actualHeight + "   " + targetHeight);
        animation.setDuration(duration);
        rel.startAnimation(animation);
    }

    /**
     * 展开
     *
     * @param v
     */
    private int line_height;

    @TargetApi(Build.VERSION_CODES.N)
    private void expand(final RelativeLayout rel, final int i) {
        TextView v = (TextView) rel.findViewById(R.id.tv_content_all);
        TextView tv_surplus = (TextView) rel.findViewById(R.id.tv_surplus);
        if (settv_surplus(tv_surplus, i)) {//如果返回值为true，说明此条打开的position是未过期提醒
            handler.removeMessages(2);
            handler.sendEmptyMessageDelayed(2, System.currentTimeMillis() % 1000);//此处是为了整秒发送handler ，最大限度保证时间的精确度
        }
        ViewGroup.LayoutParams layoutParams1 = tv_surplus.getLayoutParams();//得到tv_surplus的layoutParams是为了获取它的宽度从而算出tv_content_all的宽度
        rel.setVisibility(View.VISIBLE);//打开前先设置为VISIBLE
        Paint paint = new Paint();
        paint.setTextSize(v.getTextSize());
        int text_width = (int) paint.measureText(v.getText().toString());// 得到总体长度
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);//调用measure测量一下宽高
        ViewGroup.LayoutParams layoutParams = rel.getLayoutParams();
        int h = text_width / (widthPixels - v.getPaddingLeft() - v.getPaddingRight() - layoutParams1.width) + 1;//算出文本将会显示的行数
        line_height = v.getMeasuredHeight() - 10 - v.getPaddingTop() - v.getPaddingBottom();//得出行高
        final int targetHeight = line_height * h + 10 + v.getPaddingTop() + v.getPaddingBottom();//所需最终真实高度
        final int actualHeight = v.getHeight();//目前实际高度
        if (actualHeight <= 0) {//只有当第一次打开的时候 才需要把height设置为0，不然会闪烁一下
            layoutParams.height = 0;
        }
        rel.setTag(true);//给点击的view设置tag ，标志着当前是展开还是折叠。
        int duration;
        if (h > 2)//根据已经展开了的高度计算还需要的时间,大于两行的 基础时间就加倍
            duration = (targetHeight - actualHeight) * DURATION / targetHeight;
        else
            duration = (targetHeight - actualHeight) * DURATION_SHORT / targetHeight;
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (i == lv.getCount() - 1 - lv.getHeaderViewsCount())
                    lv.setSelection(i);
                rel.getLayoutParams().height = (int) (actualHeight + (targetHeight - actualHeight) * interpolatedTime * interpolatedTime);
                rel.requestLayout();
            }
        };
        if (duration < 0) {
            duration = 0;
        }
        animation.setDuration(duration);
        rel.startAnimation(animation);
    }

    /**
     * 计算剩余时间的文字显示
     *
     * @param tv_surplus
     * @param i
     * @return
     */
    @TargetApi(Build.VERSION_CODES.N)
    private boolean settv_surplus(TextView tv_surplus, int i) {
        long time_millis = list.get(i).getTime_millis();
        long dMillis = time_millis - System.currentTimeMillis();
        if (dMillis < 0) {
            tv_surplus.setText("已响铃");
            return false;
        } else {
            String format = new SimpleDateFormat("dd-HH-mm-ss").format(dMillis);
            String[] split = format.split("-");
            for (int j = 0; j < split.length; j++) {
                if (split[j].indexOf("0") == 0) {
                    split[j] = split[j].substring(1);
                }
            }
            if (dMillis / (1000 * 60 * 60 * 24 * 30) != 0) {
                tv_surplus.setText("大于30天后响铃");
            } else if (dMillis / (1000 * 60 * 60 * 24) != 0) {
                tv_surplus.setText(dMillis / (1000 * 60 * 60 * 24) + "天" + split[1] + "时后响铃");
            } else if (dMillis / (1000 * 60 * 60) != 0) {
                tv_surplus.setText(split[1] + "时" + split[2] + "分后响铃");
            } else if (dMillis / (1000 * 60) != 0) {
                tv_surplus.setText(split[2] + "分" + split[3] + "秒后响铃");
            } else {
                tv_surplus.setText(split[3] + "秒后响铃");
            }
            return true;
        }
    }

    private String settv_time(int position) {
        String str;
        long time_millis = list.get(position).getTime_millis();
        Calendar calendar = Calendar.getInstance();
        Calendar calendar_now = Calendar.getInstance();
        calendar.setTime(new Date(time_millis));
        calendar_now.setTime(new Date());
        switch (calendar.get(Calendar.DAY_OF_YEAR) - calendar_now.get(Calendar.DAY_OF_YEAR)) {
            case 0:
                str = "今天";
                break;
            case 1:
                str = "明天";
                break;
            case 2:
                str = "后天";
                break;
            case -1:
                str = "昨天";
                break;
            case -2:
                str = "前天";
                break;
            default:
                str = calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
                break;
        }
        str = str + " " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + (calendar.get(Calendar.MINUTE) > 10 ? calendar.get(Calendar.MINUTE) : "0" + calendar.get(Calendar.MINUTE));
        return str;
    }

    /**
     * 加载过期信息的监听
     */
    class MOnLoadActionListener implements MListView.OnLoadActionListener {
        @Override
        public ArrayList<NoteBean> load(int i, int type) {
            ArrayList<NoteBean> note = new ArrayList<>();
            Cursor cursor = activity.db.query("clocked", null, null, null, null, null, "time desc", j + "," + i);
            while (cursor.moveToNext()) {
                NoteBean noteBean = new NoteBean();
                noteBean.setTime(cursor.getString(cursor.getColumnIndex("time")));
                noteBean.setNote(cursor.getString(cursor.getColumnIndex("note")));
                noteBean.setTime_millis(cursor.getLong(cursor.getColumnIndex("time_millis")));
                noteBean.setTime_created(cursor.getLong(cursor.getColumnIndex("time_created")));
                note.add(0, noteBean);
            }
            if (type == 1) {
                j = j + note.size();
                list.addAll(0, note);
                ArrayList<Integer> expandListCopy = new ArrayList<>();
                expandListCopy.addAll(expandList);
                expandList.clear();
                for (int p = 0; p < expandListCopy.size(); p++) {
                    expandList.add(expandListCopy.get(p) + note.size());
                }
                adapter.notifyDataSetChanged();
                if (note.size() != 1) {//当只刷新了一条数据时 就不需要做回弹的动画了
                    View v = lv.getChildAt(0).findViewById(R.id.tv_content);
                    animator(v.getHeight() * (note.size() - 1));
                }
            }
            return note;
        }

        private void animator(final int i) {
            Animator animator = ObjectAnimator.ofFloat(lv, "TranslationY", -i, -i, -i, -i / 2, 0);
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(500);
            animator.start();
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
        }
    }

    public MListView getLv() {
        return lv;
    }

    public boolean getIsRun() {
        return isRun;
    }

    public void setJ(int j) {
        this.j = j;
    }

    private int sp2px(float sp) {
        final float fontScale = this.getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * fontScale + 0.5f);
    }

}
