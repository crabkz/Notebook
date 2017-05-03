package com.agsoft.notebook.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.agsoft.notebook.Bean.TimeBean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by 1 on 2016/9/14.
 */
public class TimeManager {
    private static final String TAG = "loglog";
    private static ArrayList<Pattern> list1 = new ArrayList();
    private static ArrayList<Pattern> list2 = new ArrayList();
    private static ArrayList<Pattern> list3 = new ArrayList();
    private static ArrayList<Pattern> list4 = new ArrayList();
    private static ArrayList<Pattern> list5 = new ArrayList();

    static {

        list1.add(Pattern.compile("^[0-9]{1,2}月[0-9]{1,2}日"));
        list1.add(Pattern.compile("^[0-9]{1,2}日"));
        list1.add(Pattern.compile("^[0-9]{1,2}月[0-9]{1,2}号"));
        list1.add(Pattern.compile("^[0-9]{1,2}号"));

        list2.add(Pattern.compile("^(上午|早晨|早上|凌晨|早)"));
        list2.add(Pattern.compile("^(下午|傍晚|晚上|中午|晚)"));

        list3.add(Pattern.compile("^(今天)"));
        list3.add(Pattern.compile("^(今儿)"));
        list3.add(Pattern.compile("^(明天)"));
        list3.add(Pattern.compile("^(明儿)"));
        list3.add(Pattern.compile("^(后天)"));
        list3.add(Pattern.compile("^(大后天)"));

        list4.add(Pattern.compile("^[0-9一二两三四五六七八九十零]{1,2}[点.:：钟]{1,2}$"));
        list4.add(Pattern.compile("^[0-9]{1,2}[点.:：][0-9半整]{1,2}分{0,1}$"));

        list5.add(Pattern.compile("^[0-9]*天{0,1}[0-9]*(小时|时|个小时){0,1}[0-9]*(分|分钟){0,1}(之后|以后){0,1}$"));
    }

    public static TimeBean analyze(String str) {
        str = stringPrase(str);
        boolean hasD;
        boolean hasH;
        boolean hasM;
        int dd = -1;
        int hh = -1;
        int mm = -1;
        String[] dS;
        String[] hS;
        String[] mS;

        String m = null;
        String d;
        int t = -1;//上午还是下午呢
        int j = -1;//今天明天后天呢
        int pattern = -1;
        int year = 2016;
        int month = -1;
        int date = -1;
        int hour = -1;
        int minute = -1;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if (list5.get(0).matcher(str).find()) {
            str = str.replace("个小时", "时").replace("小时", "时").replace("分钟", "分").replace("之后", "").replace("以后", "").trim();
            hasD = str.contains("天");
            hasH = str.contains("时");
            hasM = str.contains("分");
            if (hasD) {
                dS = str.split("天");
                dd = Integer.parseInt(dS[0]);
                if (hasH) {
                    hS = dS[1].split("时");
                    hh = Integer.parseInt(hS[0]);
                    if (hasM) {
                        mS = hS[1].split("分");
                        mm = Integer.parseInt(mS[0]);
                    }
                } else {
                    if (hasM) {
                        mS = dS[1].split("分");
                        mm = Integer.parseInt(mS[0]);
                    }
                }
            } else {
                if (hasH) {
                    hS = str.split("时");
                    hh = Integer.parseInt(hS[0]);
                    if (hasM) {
                        mS = hS[1].split("分");
                        mm = Integer.parseInt(mS[0]);
                    }
                } else {
                    if (hasM) {
                        mS = str.split("分");
                        mm = Integer.parseInt(mS[0]);
                    }
                }
            }
            Log.e(TAG, "analyze: " + dd + "  " + hh + "  " + mm);
        } else {
            for (int i = 0; i < list1.size(); i++) {
                if (list1.get(i).matcher(str).find()) {
                    pattern = i;
                    break;
                }
            }
            if (pattern != -1) {
                if (pattern < 2) {
                    m = str.substring(0, str.indexOf("日"));
                    str = str.substring(str.indexOf("日") + 1);
                } else {
                    m = str.substring(0, str.indexOf("号"));
                    str = str.substring(str.indexOf("号") + 1);
                }
//------------------------------检测上中午----------------------------------
                pattern = -1;
                for (int i = 0; i < list2.size(); i++) {
                    if (list2.get(i).matcher(str).find()) {
                        pattern = i;
                        break;
                    }
                }
                if (pattern != -1) {
                    t = pattern;
//--------------------------------检测时间----------------------------------------
                    str = str.substring(2);
                    d = timeCheck(str);
                } else {
                    d = timeCheck(str);
                }
            } else {//如果没有月日
//-----------------------------检测今天明天后天----------------------------------
                for (int i = 0; i < list3.size(); i++) {
                    if (list3.get(i).matcher(str).find()) {
                        pattern = i;
                        break;
                    }
                }
                if (pattern != -1) {
                    j = pattern;
                    if (j == 5) {
                        str = str.substring(3);
                    } else {
                        str = str.substring(2);
                    }
                }
//-------------------------------检测上中午----------------------------------
                pattern = -1;
                for (int i = 0; i < list2.size(); i++) {
                    if (list2.get(i).matcher(str).find()) {
                        pattern = i;
                        break;
                    }
                }
                if (pattern != -1) {
                    t = pattern;
//--------------------------------检测时间----------------------------------------
                    str = str.substring(2);
                    d = timeCheck(str);
                } else {
                    d = timeCheck(str);
                }
            }
            if (TextUtils.isEmpty(m) && TextUtils.isEmpty(d) && t == -1 && j == -1) {//什么信息都没有 则返回null
                return null;
            }
            if (m != null) {
                if (m.contains("月")) {
                    String[] split_m = m.split("月");
                    month = Integer.parseInt(split_m[0]) - 1;
                    date = Integer.parseInt(split_m[1]);
                } else {
                    date = Integer.parseInt(m);
                }
            } else {
                switch (j) {
                    case 2:
                        date = calendar.get(Calendar.DAY_OF_MONTH) + 1;
                        break;
                    case 3:
                        date = calendar.get(Calendar.DAY_OF_MONTH) + 1;
                        break;
                    case 4:
                        date = calendar.get(Calendar.DAY_OF_MONTH) + 2;
                        break;
                    case 5:
                        date = calendar.get(Calendar.DAY_OF_MONTH) + 3;
                        break;
                }
            }
//----------------------------------------------------------------------------------------
            if (d != null) {
                if (d.contains(":")) {
                    String[] split_d = d.split(":");
                    hour = Integer.parseInt(split_d[0]);
                    minute = Integer.parseInt(split_d[1]);
                } else {
                    hour = Integer.parseInt(d);
                    minute = 0;
                }
            }
            switch (t) {
                case -1://只说了具体天数没说时间设置为早上九点
                    if (hour == -1) {//具体时间则自动设置晨早上九点
                        hour = 9;
                        minute = 0;
                    }
                    break;
                case 0:
                    if (hour == -1) {//只说了上午没说具体时间则自动设置晨早上九点
                        hour = 9;
                        minute = 0;
                    }
                    break;
                case 1:
                    if (hour == -1) {
                        hour = 14;
                        minute = 0;
                    } else {
                        hour = hour + 12;
                    }
                    break;
            }
        }

        if (mm != -1 || hh != -1 || dd != -1) {
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH) + (dd == -1 ? 0 : dd), calendar.get(Calendar.HOUR_OF_DAY) + (hh == -1 ? 0 : hh), calendar.get(Calendar.MINUTE) + (mm == -1 ? 0 : mm));
        } else {
            calendar.set(calendar.get(Calendar.YEAR), month == -1 ? calendar.get(Calendar.MONTH) : month, date == -1 ? calendar.get(Calendar.DAY_OF_MONTH) : date, hour, minute, 0);
        }
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            return null;
        }
        Log.e(TAG, "analyze: " + new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(calendar.getTime()));
        TimeBean timeBean = new TimeBean();
        timeBean.setTime(new SimpleDateFormat("MM-dd HH:mm").format(calendar.getTime()));
        timeBean.setTime_millis(calendar.getTimeInMillis());
        return timeBean;
    }

    private static String timeCheck(String str) {
        int pattern = -1;
        String d = null;
        for (int i = 0; i < list4.size(); i++) {
            if (list4.get(i).matcher(str).find()) {
                pattern = i;
            }
        }
        if (pattern != -1) {//齐全
            if (pattern == 0) {
                d = str;
            } else {
                d = str.replace('分', ' ').replace("半", "30").replace("整", "00").trim();
            }
        }
        return d;
    }

    public static void setAlarm(Context context, long time_millis, long time_created) {
        Intent intent = new Intent("action_alarm");
        intent.putExtra("time_created", time_created);
        PendingIntent sender = PendingIntent.getBroadcast(context, (int) (time_created % 1000000), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time_millis, sender);
    }

    private static String stringPrase(String str) {
        return str.replace("点", "").replace("钟", "").replace(":", "").replace("：", "").replace(".", "")
                .replace("三十", "30").replace("四十", "40").replace("五十", "50").replace("六十", "60")
                .replace("七十", "70").replace("八十", "80").replace("九十", "90")
                .replace("二十", "20").replace("十九", "19").replace("十八", "18").replace("十七", "17")
                .replace("十六", "16").replace("十五", "15").replace("十四", "14").replace("十三", "13")
                .replace("十二", "12").replace("十一", "24").replace("十", "10").replace('零', '0')
                .replace('一', '1').replace('二', '2').replace('两', '2').replace('三', '3').replace('四', '4')
                .replace('五', '5').replace('六', '6').replace('七', '7').replace('八', '8')
                .replace('九', '9').trim();
    }
}
