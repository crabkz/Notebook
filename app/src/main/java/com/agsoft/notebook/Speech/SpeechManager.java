package com.agsoft.notebook.Speech;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.agsoft.notebook.Bean.TimeBean;
import com.agsoft.notebook.Utils.JsonParser;
import com.agsoft.notebook.Activity.MainActivity;
import com.agsoft.notebook.Utils.TimeManager;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by 1 on 2016/9/19.
 */
public class SpeechManager {
    private static final String TAG = "loglog";
    private SharedPreferences sp;
    private RecognizerDialog mIatDialog;
    public SpeechRecognizer mIat;
    private MainActivity activity;
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private String s;//整个会话的全部String

    public SpeechManager(MainActivity activity) {
        this.activity = activity;
        mIat = SpeechRecognizer.createRecognizer(activity, mInitListener);
        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(activity, mInitListener);
        sp = activity.getSharedPreferences("config", Activity.MODE_PRIVATE);
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };

    /**
     * UI听写监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);
        }


        //识别回调错误.
        public void onError(SpeechError error) {
            Log.e(TAG, "onError: " + error.getPlainDescription(true));
            showTip(error.getPlainDescription(true));
        }
    };

    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
            s = "";//开始说话 把s置为空
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            showTip(error.getPlainDescription(true));
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);
            if (isLast) {
                if (activity.speech == 0) {
                    activity.setTime(TimeManager.analyze(s.trim().replace("。", "")));
                    Log.e(TAG, "printResult: 输入时间");
                } else {
                    action = s;
                    Log.e(TAG, "printResult: 输入事件");
                    activity.et_newcontent.setText(activity.et_newcontent.getText().toString() + action);
                    activity.et_newcontent.setSelection(activity.et_newcontent.getText().length());
                }
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            Log.d(TAG, "返回音频数据：" + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };
    int ret = 0; // 函数调用返回值


    public void speech() {
        // FlowerCollector.onEvent(this, "iat_recognize");
        mIatResults.clear();
        setParam();
        boolean isShowDialog = sp.getBoolean("isShow", false);
        if (isShowDialog) {
            mIatDialog.setListener(mRecognizerDialogListener);
            mIatDialog.show();
            showTip("开始说话");
        } else {
            ret = mIat.startListening(mRecognizerListener);
            if (ret != ErrorCode.SUCCESS) {
                showTip("听写失败");
            }
        }
    }

    TimeBean timeBean;
    String action;

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mIatResults.put(sn, text);
        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        s = resultBuffer.toString();
        Log.e(TAG, "printResult: " + text);
    }

    /**
     * 参数设置
     *
     * @return
     */

    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        String lag = sp.getString("iat_language_preference",
                "mandarin");
        if (lag.equals("en_us")) {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        } else {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        }

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, sp.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, sp.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        if (activity.speech == 0) {
            mIat.setParameter(SpeechConstant.ASR_PTT, sp.getString("iat_punc_preference", "0"));
        } else mIat.setParameter(SpeechConstant.ASR_PTT, sp.getString("iat_punc_preference", "1"));
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    /**
     * Toast的展现
     *
     * @param str
     */
    private void showTip(final String str) {
        Toast.makeText(activity, str, Toast.LENGTH_SHORT).show();
    }
}
