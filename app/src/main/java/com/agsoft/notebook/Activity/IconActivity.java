package com.agsoft.notebook.Activity;

import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.agsoft.notebook.Bean.Extra;
import com.agsoft.notebook.R;
import com.agsoft.notebook.Utils.OvalIcon;

import java.io.File;

public class IconActivity extends AppCompatActivity {

    private ImageView iv_scale_icon;
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = View.inflate(this, R.layout.activity_icon, null);
        setContentView(view);
        iv_scale_icon = (ImageView) findViewById(R.id.iv_scale_icon);
        boolean b = true;
        File[] files = new File(Extra.ICON_Dir).listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.getName().contains("icon")) {
                    iv_scale_icon.setImageBitmap(OvalIcon.toRoundBitmap(BitmapFactory.decodeFile(f.getPath())));
                    b = false;
                }
            }
        }
        if (b)
            iv_scale_icon.setImageBitmap(OvalIcon.toRoundBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.demo)));
        iv_scale_icon.post(new Runnable() {
            @Override
            public void run() {
                animation_enter();
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation anim = AnimationUtils.loadAnimation(IconActivity.this, R.anim.scale_popup_exit);
                view.startAnimation(anim);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        finish();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
            }
        });
    }

    private void animation_enter() {
        AnimationSet set = new AnimationSet(false);
        ScaleAnimation scale = new ScaleAnimation(1, 2, 1, 2, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        AlphaAnimation alpha = new AlphaAnimation(0, 1);
        set.setStartTime(200);
        set.addAnimation(scale);
        set.addAnimation(alpha);
        set.setFillAfter(true);
        set.setDuration(700);
        iv_scale_icon.startAnimation(set);
        alpha.setDuration(250);
        alpha.setStartTime(250);
        view.startAnimation(alpha);
    }
}
