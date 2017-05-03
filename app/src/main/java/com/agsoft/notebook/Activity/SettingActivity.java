package com.agsoft.notebook.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.agsoft.notebook.Bean.Extra;
import com.agsoft.notebook.R;
import com.agsoft.notebook.Utils.OvalIcon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG ="loglog" ;
    private RelativeLayout rel_setting_background;
    private ImageView iv_setting_background;
    private View inflate;
    private int widthPixels;
    private GestureDetectorCompat gestureDetectorCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflate = View.inflate(this, R.layout.activity_setting, null);
        setContentView(inflate);
        init();
    }

    private void init() {
        rel_setting_background = (RelativeLayout) findViewById(R.id.rel_setting_background);
        iv_setting_background = (ImageView) findViewById(R.id.iv_setting_background);
        File[] files = new File(Extra.BACKGROUND_DIR).listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.getName().contains("thumbnail")) {
                    iv_setting_background.setImageBitmap(OvalIcon.toRoundBitmap(BitmapFactory.decodeFile(f.getPath())));
                }
            }
        }
        rel_setting_background.setOnClickListener(this);
        widthPixels = getResources().getDisplayMetrics().widthPixels;
        inflate.setOnTouchListener(new MOnTouchListener());
        gestureDetectorCompat = new GestureDetectorCompat(this, new MGestureDetector());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rel_setting_background:
                Intent albumIntent = new Intent(Intent.ACTION_PICK, null);
                albumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(albumIntent, 1);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    crop(uri);
                }
                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    Bitmap bitmap = BitmapFactory.decodeFile(data.getData().getPath());
                    try {
                        for (File f : new File(Extra.BACKGROUND_DIR).listFiles()) {
                            if (f.getName().contains("thumbnail")) {
                                f.delete();
                            } else if (f.getName().contains("background_")) {
                                f.delete();
                            }
                        }
                        FileOutputStream fos = new FileOutputStream(new File(Extra.THUMBNAIL_FILE));
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, fos);
                        fos.flush();
                        fos.close();
                        iv_setting_background.setImageBitmap(OvalIcon.toRoundBitmap(BitmapFactory.decodeFile(Extra.THUMBNAIL_FILE)));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    /**
     * 剪切图片
     *
     * @param uri
     */
    Uri imageUri = Uri.fromFile(new File(Extra.BACKGROUND_FILE));

    private void crop(Uri uri) {
        File file = new File(Extra.BACKGROUND_DIR);
        if (!file.exists()) file.mkdirs();
        else {
            file = new File(Extra.BACKGROUND_FILE);
            file.renameTo(new File(Extra.BACKGROUND_DIR + "/background_copy"));
        }
        // 裁剪图片意图
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // 裁剪框的比例，1：1
        intent.putExtra("aspectX", 4);
        intent.putExtra("aspectY", 7);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 400);
        intent.putExtra("outputY", 700);
        intent.putExtra("scale", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());// 图片格式
        intent.putExtra("noFaceDetection", true);// 取消人脸识别
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra("return-data", false);
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT
        startActivityForResult(intent, 2);
    }

    class MOnTouchListener implements View.OnTouchListener {
        float rawX, startX;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (gestureDetectorCompat.onTouchEvent(event)) {
                return true;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getRawX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    rawX = event.getRawX();
                    Log.e("loglog", "onTouch: " + rawX);
                    int v = (int) (rawX - startX);
                    if (v >= 0) {
                        inflate.layout(v, 0, widthPixels + v, inflate.getBottom());
                    }
                    break;
            }
            return true;
        }
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
            if (motionEvent.getRawX() - motionEvent1.getRawX() < 0) {
                animation();
            }
            return true;
        }

        private void animation() {
            AnimationSet set = new AnimationSet(true);
            TranslateAnimation translate = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
            AlphaAnimation alpha = new AlphaAnimation(1, 0.8f);
            set.addAnimation(translate);
            set.addAnimation(alpha);
            set.setInterpolator(new LinearInterpolator());
            set.setDuration(250);
            set.setFillAfter(true);
            set.setAnimationListener(new Animation.AnimationListener() {
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
            inflate.startAnimation(set);
        }
    }
}
