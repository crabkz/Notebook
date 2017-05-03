package com.agsoft.notebook.Activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.agsoft.notebook.Bean.Extra;
import com.agsoft.notebook.Bean.TimeBean;
import com.agsoft.notebook.DataBase.MDataBaseHelper;
import com.agsoft.notebook.Fragment.ActionFragment;
import com.agsoft.notebook.Fragment.MFragment;
import com.agsoft.notebook.MView.MSlidingMenu;
import com.agsoft.notebook.R;
import com.agsoft.notebook.Speech.SpeechManager;
import com.agsoft.notebook.Utils.OvalIcon;
import com.agsoft.notebook.Utils.TimeManager;
import com.iflytek.cloud.SpeechUtility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ActionFragment.OnFragmentInteractionListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Toast mToast;
    private SharedPreferences sp;
    private Button bt_date, bt_action, bt_cancel, bt_add;
    private MFragment mFragment;
    private LinearLayout new_action;
    private int widthPixels;
    private SpeechManager speechManager;
    public TextView tv_newtime, tv_title_time, tv_title_action;
    public EditText et_newcontent;
    public SQLiteDatabase db;
    private MenuView menuView;
    private View main_view;
    private View shadow;
    private View menu;
    public View main;
    public MSlidingMenu mSlidingMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main_view = View.inflate(this, R.layout.activity_main, null);
        setContentView(main_view);
        SpeechUtility.createUtility(this, "appid=57d79505");
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        bt_action = (Button) findViewById(R.id.bt_action);
        bt_date = (Button) findViewById(R.id.bt_date);
        bt_cancel = (Button) findViewById(R.id.bt_cancel);
        bt_add = (Button) findViewById(R.id.bt_add);
        new_action = (LinearLayout) findViewById(R.id.new_action);
        tv_newtime = (TextView) findViewById(R.id.tv_newtime);
        tv_title_time = (TextView) findViewById(R.id.tv_title_time);
        tv_title_action = (TextView) findViewById(R.id.tv_title_action);
        et_newcontent = (EditText) findViewById(R.id.et_newcontent);
        menu = findViewById(R.id.menu);
        main = findViewById(R.id.main);
        shadow = findViewById(R.id.shadow);
        sp = getSharedPreferences("config", Activity.MODE_PRIVATE);
        initDataBase();
        init();
        initFragment();
    }

    private void init() {
        ContentValues values = new ContentValues();
        values.put("time", "TIME");
        values.put("time_millis", new Date().getTime() + 1000000);
        values.put("time_created", System.currentTimeMillis());
        values.put("note", "与子周爷爷的热水瓶记得还给他。");
        db.insert("clock", null, values);
        values = new ContentValues();
        values.put("time", "TIME");
        values.put("time_millis", new Date().getTime() + 1000000);
        values.put("time_created", System.currentTimeMillis());
        values.put("note", "特蕾莎·梅18日突然宣布将要求提前举行国会大选。英国去年6月公投决定离开欧洲联盟(EU)，使亲欧派选民心烦意乱，反对党自由民主党(Liberal Democrats)盼能趁势出线，全力反对让英国离开欧洲单一市场的“硬脱欧”。");
        db.insert("clock", null, values);
        widthPixels = getResources().getDisplayMetrics().widthPixels;
        mFragment = new MFragment();
        menuView = new MenuView();
        speechManager = new SpeechManager(this);
        bt_action.setOnClickListener(this);
        bt_date.setOnClickListener(this);
        bt_cancel.setOnClickListener(this);
        bt_add.setOnClickListener(this);
        new_action.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        mSlidingMenu = new MSlidingMenu(this, menu, main, shadow);
    }

    private void initFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fl, mFragment);
        ft.replace(R.id.menu, menuView);
        ft.commit();
    }

    private void initDataBase() {
        db = new MDataBaseHelper(this, "notebook").getWritableDatabase();
    }

    @Override
    protected void onResume() {
        // 开放统计 移动数据统计分析
//        FlowerCollector.onResume(this);
//        FlowerCollector.onPageStart(TAG);
        File file = new File(Extra.BACKGROUND_DIR);
        if (!file.exists()) file.mkdirs();
        File[] files = new File(Extra.BACKGROUND_DIR).listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.getName().contains("background")) {
                    main.setBackground(Drawable.createFromPath(f.getPath()));
                }
            }
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        // 开放统计 移动数据统计分析
//        FlowerCollector.onPageEnd(TAG);
//        FlowerCollector.onPause(this);
        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        // 退出时释放连接
        speechManager.mIat.cancel();
        speechManager.mIat.destroy();
        db.close();
    }

    public int speech;//当前输入的是什么内容 0是时间，1是事件

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_date:
                speech = 0;
                speechManager.speech();
                tv_title_time.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_title_action.setTextColor(getResources().getColor(R.color.colorText));
                showNewAction();
                break;
            case R.id.bt_action:
                speech = 1;
                speechManager.speech();
                tv_title_time.setTextColor(getResources().getColor(R.color.colorText));
                tv_title_action.setTextColor(getResources().getColor(R.color.colorPrimary));
                showNewAction();
                break;
            case R.id.bt_add:
                if (timeBean == null) {
                    showTip("请输入时间");
                    return;
                }
                if (TextUtils.isEmpty(et_newcontent.getText().toString())) {
                    showTip("请输入内容");
                    return;
                }
                ContentValues values = new ContentValues();
                values.put("time", timeBean.getTime());
                values.put("time_millis", timeBean.getTime_millis());
                values.put("time_created", System.currentTimeMillis());
                values.put("note", et_newcontent.getText().toString());
                db.insert("clock", null, values);
                cancelOrCreateNewAction();
                TimeManager.setAlarm(this, timeBean.getTime_millis(), (Long) values.get("time_created"));
                mFragment.setJ(0);//添加了新事件 把此前刷新到过期事件的条目的计数给置为0，因为添加新事件会把之前刷出来的过期事件给移除掉listview
                mFragment.refresh();
                break;
            case R.id.bt_cancel:
                cancelOrCreateNewAction();
                break;
        }
    }

    /**
     * 新事件的弹窗出现
     */
    private void showNewAction() {
        if (new_action.getVisibility() == View.VISIBLE) return;
        new_action.setVisibility(View.VISIBLE);
        PropertyValuesHolder holder1 = PropertyValuesHolder.ofFloat("alpha", 0, 0.96f);
        PropertyValuesHolder holder2 = PropertyValuesHolder.ofFloat("TranslationX", -widthPixels, 0);
        Animator animator = ObjectAnimator.ofPropertyValuesHolder(new_action, holder1, holder2);
        animator.setDuration(300);
        animator.setInterpolator(new OvershootInterpolator(1));
        animator.start();
    }

    /**
     * 新事件的弹窗消失
     */
    private void cancelOrCreateNewAction() {
        speechManager.mIat.stopListening();
//        boolean destroy = speechManager.mIat.destroy();
//        Log.e(TAG, "cancelOrCreateNewAction: "+destroy);
        PropertyValuesHolder holder1 = PropertyValuesHolder.ofFloat("alpha", 0.97f, 0);
        PropertyValuesHolder holder2 = PropertyValuesHolder.ofFloat("TranslationX", 0, -widthPixels);
        Animator animator = ObjectAnimator.ofPropertyValuesHolder(new_action, holder1, holder2);
        animator.setDuration(200);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                tv_newtime.setText("");
                et_newcontent.setText("");
                tv_title_time.setTextColor(getResources().getColor(R.color.colorText));
                tv_title_action.setTextColor(getResources().getColor(R.color.colorText));
                new_action.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private TimeBean timeBean;

    public void setTime(TimeBean timeBean) {
        if (timeBean == null) {
            showTip("输入时间错误或已过期");
            return;
        }
        this.timeBean = timeBean;
        tv_newtime.setText(timeBean.getTime());
    }

    public MFragment getmFragment() {
        return mFragment;
    }

    public MSlidingMenu getMenu() {
        return mSlidingMenu;
    }

    @Override
    public void onBackPressed() {
        if (menuView.getScalePopup() != null && menuView.getScalePopup().isShowing()) {
            menuView.getScalePopup().dismiss();
        } else if (menuView.getPopup() != null && menuView.getPopup().isShowing()) {
            menuView.getPopup().dismiss();
        } else if (mSlidingMenu.isMenuShowing()) {
            mSlidingMenu.toggle();
        } else if (new_action.getVisibility() == View.VISIBLE) {
            cancelOrCreateNewAction();
        } else if (mFragment.getDelete()) {
            if (!mFragment.getIsRun()) {
                menuView.getTv_edit().setText("编 辑");
                mFragment.animation_delete_close(mFragment.getLv().getChildCount() - 1);
            }
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Toast的展现
     *
     * @param str
     */
    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        File file_icon = new File(Extra.ICON_FILE);
        File dir_icon = new File(Extra.ICON_Dir);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    crop(Uri.fromFile(file_icon));
                }
                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    crop(data.getData());
                }
                break;
            case 3:
                if (resultCode == RESULT_OK) {
                    Bitmap bitmap = data.getParcelableExtra("data");
                    menuView.getIv_icon().setImageBitmap(OvalIcon.toRoundBitmap(bitmap));
                    if (dir_icon.listFiles() != null) {
                        for (File f : dir_icon.listFiles()) {
                            if (f.getName().contains("icon")) f.delete();
                        }
                    }
                    try {
                        FileOutputStream fos = new FileOutputStream(file_icon);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 剪切图片
     *
     * @param uri
     */
    private void crop(Uri uri) {
        // 裁剪图片意图
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // 裁剪框的比例，1：1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 340);
        intent.putExtra("outputY", 340);

        intent.putExtra("outputFormat", "JPEG");// 图片格式
//        intent.putExtra("noFaceDetection", true);// 取消人脸识别
        intent.putExtra("return-data", true);
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT
        startActivityForResult(intent, 3);
    }

}
