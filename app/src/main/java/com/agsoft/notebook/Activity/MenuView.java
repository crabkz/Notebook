package com.agsoft.notebook.Activity;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.agsoft.notebook.Bean.Extra;
import com.agsoft.notebook.Fragment.MFragment;
import com.agsoft.notebook.R;
import com.agsoft.notebook.Utils.OvalIcon;

import java.io.File;

/**
 * Created by 1 on 2016/9/23.
 */
public class MenuView extends Fragment {
    private static final String TAG = "loglog";
    private View view;
    private TextView tv_edit, tv_setting, tv_photo, tv_camera, tv_image;
    private MainActivity activity;
    private MFragment mFragment;
    private ImageView iv_icon;
    private PopupWindow popup;
    private View popup_view;
    private View popup_view1;
    private PopupWindow popup_scale;
    private ImageView iv_scale_icon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        mFragment = activity.getmFragment();
        view = View.inflate(activity, R.layout.setting_menu, null);
        init();
        return view;
    }

    public View getView() {
        return view;
    }

    private void init() {
        popup_view = View.inflate(activity, R.layout.popup_selector_icon, null);
        tv_edit = (TextView) view.findViewById(R.id.tv_edit);
        tv_setting = (TextView) view.findViewById(R.id.tv_setting);
        tv_camera = (TextView) popup_view.findViewById(R.id.tv_camera);
        tv_photo = (TextView) popup_view.findViewById(R.id.tv_photo);
        tv_image = (TextView) popup_view.findViewById(R.id.tv_image);
        iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
        boolean b = true;
        File[] files = new File(Extra.ICON_Dir).listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.getName().contains("icon")) {
                    iv_icon.setImageBitmap(OvalIcon.toRoundBitmap(BitmapFactory.decodeFile(f.getPath())));
                    b = false;
                }
            }
        }
        if (b)
            iv_icon.setImageBitmap(OvalIcon.toRoundBitmap(BitmapFactory.decodeResource(activity.getResources(), R.mipmap.demo)));
        MOnClickListener listener = new MOnClickListener();
        tv_edit.setOnClickListener(listener);
        tv_setting.setOnClickListener(listener);
        iv_icon.setOnClickListener(listener);
        tv_photo.setOnClickListener(listener);
        tv_camera.setOnClickListener(listener);
        tv_image.setOnClickListener(listener);
    }

    class MOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            File f = new File(Extra.ICON_FILE);
            File file = new File(Extra.ICON_Dir);
            switch (view.getId()) {
                case R.id.tv_edit:
                    if (!mFragment.getIsRun()) {
                        if (!mFragment.getDelete()) {
                            if (mFragment.getLv().getChildCount() != 1) {
                                activity.getMenu().toggle();
                                tv_edit.setText("完 成");
                                mFragment.animation_delete_open(1);
                            }
                        } else {
                            activity.getMenu().toggle();
                            tv_edit.setText("编 辑");
                            mFragment.animation_delete_close(mFragment.getLv().getChildCount() - 1);
                        }
                    }
                    break;
                case R.id.tv_setting:
                    activity.getMenu().toggle();
                    activity.startActivity(new Intent(activity, SettingActivity.class));
                    break;
                case R.id.iv_icon:
                    activity.getMenu().toggle();
                    popupWindow();
                    break;
                case R.id.tv_camera:
                    popup.dismiss();
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (!file.exists()) file.mkdirs();
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Extra.ICON_FILE)));
                    activity.startActivityForResult(intent, 1);
                    break;
                case R.id.tv_photo:
                    popup.dismiss();
                    if (!file.exists()) file.mkdirs();
                    Intent albumIntent = new Intent(Intent.ACTION_PICK, null);
                    albumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    activity.startActivityForResult(albumIntent, 2);
                    break;
                case R.id.tv_image:
                    popup.dismiss();
                    scaleIcon();
                    break;
                case R.id.rel:
                    popup_view1.setOnClickListener(null);
                    popup_scale.dismiss();
                    break;
            }
        }
    }

    private void popupWindow() {
        popup = new PopupWindow(activity);
        popup.setWidth(activity.getResources().getDisplayMetrics().widthPixels + 6);
        popup.setHeight(activity.getResources().getDisplayMetrics().heightPixels * 2 / 5);
        popup.setContentView(popup_view);
        popup.setAnimationStyle(R.style.PopupAnimation);
        popup.setOutsideTouchable(true);
        popup.setClippingEnabled(false);
        popup.showAtLocation(tv_edit, Gravity.BOTTOM, 0, -20);
    }

    /**
     * 查看大图
     */
    private void scaleIcon() {
//        activity.startActivity(new Intent(activity, IconActivity.class));
//        activity.overridePendingTransition(R.anim.activity_icon_enter, 0);
//        activity.finish();
        popup_view1 = View.inflate(activity, R.layout.popup_scale_icon, null);
        popup_view1.setOnClickListener(new MOnClickListener());
        iv_scale_icon = (ImageView) popup_view1.findViewById(R.id.iv_scale_icon);
        iv_scale_icon.setImageDrawable(iv_icon.getDrawable());
        popup_scale = new PopupWindow(activity);
        popup_scale.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popup_scale.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        popup_scale.setContentView(popup_view1);
        popup_scale.setBackgroundDrawable(null);
        popup_scale.setOutsideTouchable(true);
        popup_scale.setClippingEnabled(false);
        popup_scale.setAnimationStyle(R.style.ScalePopupAnimation);
        popup_scale.showAtLocation(iv_icon, Gravity.NO_GRAVITY, 0, 0);
        AnimationSet set = new AnimationSet(false);
        ScaleAnimation scale = new ScaleAnimation(1, 2, 1, 2, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        AlphaAnimation alpha = new AlphaAnimation(0, 1);
        set.setStartTime(200);
        set.addAnimation(scale);
        set.addAnimation(alpha);
        set.setFillAfter(true);
        set.setDuration(700);
        iv_scale_icon.startAnimation(set);
    }

    public TextView getTv_edit() {
        return tv_edit;
    }

    public PopupWindow getPopup() {
        return popup;
    }

    public PopupWindow getScalePopup() {
        return popup_scale;
    }

    public ImageView getIv_icon() {
        return iv_icon;
    }

    public void setTv_edit(TextView tv_edit) {
        this.tv_edit = tv_edit;
    }

}
