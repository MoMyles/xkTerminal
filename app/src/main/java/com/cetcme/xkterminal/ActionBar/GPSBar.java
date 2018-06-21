package com.cetcme.xkterminal.ActionBar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cetcme.xkterminal.DataFormat.AlertFormat;
import com.cetcme.xkterminal.MainActivity;
import com.cetcme.xkterminal.MessageDialogActivity;
import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.DateUtil;
import com.cetcme.xkterminal.MyClass.PreferencesUtils;
import com.cetcme.xkterminal.R;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by qiuhong on 10/01/2018.
 */

public class GPSBar extends RelativeLayout {

    public MainActivity mainActivity;

    private TextView textView_latitude;
    private TextView textView_longitude;
    private TextView textView_speed;
    private TextView textView_heading;
    private TextView textView_location_status;
    private TextView textView_ais_status;
    private TextView textView_message;
    private TextView textView_time;

    private TextView textView_message_number;
    private TextView textView_alert;
    private TextView textView_alerting;

    private LinearLayout debug_btn_layout;

    private boolean noGps = true;
    private boolean noAisConnected = true;

    private boolean flashTextViewVisible = true;

    private boolean flashAisTextViewVisible = true;

    private static final int UPDATE_TIME = 1;
    private static final int FLASH_NO_GPS = 2;
    private static final int FLASH_NO_AIS = 3;
    private static final int UPDATE_GPS_STATUS = 4;


    // 用于关闭app
    private int clickTime = 0;

    private ArrayList<TextView> textViews = new ArrayList<>();

    private Toast newMsgToast;

    public GPSBar(Context context) {
        super(context);
    }

    public GPSBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        View view = LayoutInflater.from(context).inflate(R.layout.bar_gps_view, this, true);

        bindView(view);
        setData();

        newMsgToast = Toast.makeText(view.getContext(), "您有新的短信", Toast.LENGTH_SHORT);

        new TimeHandler().start();
    }

    private void bindView(View view) {

        debug_btn_layout = view.findViewById(R.id.debug_btn_layout);

        textView_latitude = view.findViewById(R.id.textView_latitude);
        textView_longitude = view.findViewById(R.id.textView_longitude);
        textView_speed = view.findViewById(R.id.textView_speed);
        textView_heading = view.findViewById(R.id.textView_heading);
        textView_location_status = view.findViewById(R.id.textView_location_status);
        textView_ais_status = view.findViewById(R.id.textView_ais_status);
        textView_message = view.findViewById(R.id.textView_message);
        textView_time = view.findViewById(R.id.textView_time);

        textView_message_number = view.findViewById(R.id.textView_message_number);

        textView_alert = view.findViewById(R.id.textView_alert);
        textView_alert.setVisibility(GONE);
        textView_alert.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new QMUIDialog.MessageDialogBuilder(mainActivity)
                        .setTitle("解除报警")
                        .setMessage("确定要解除吗？")
                        .addAction("取消", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                            }
                        })
                        .addAction(0, "解除", QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {

                                // 解除报警操作
                                ((MyApplication) mainActivity.getApplication()).sendBytes(AlertFormat.format("00010000", "00000000"));
//                                PreferencesUtils.putBoolean(mainActivity, "homePageAlertView", false);
                                PreferencesUtils.putBoolean(mainActivity, "flashAlert", false);
                                textView_alert.setVisibility(GONE);
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        textView_alerting = view.findViewById(R.id.textView_alerting);
        textView_alerting.setVisibility(INVISIBLE);


        textView_message.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mainActivity.fragmentName.equals("message"))
                    mainActivity.initMessageFragment("receive");
            }
        });

        textView_message_number.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mainActivity.fragmentName.equals("message"))
                    mainActivity.initMessageFragment("receive");
            }
        });

        //: for test 多次点击退出app
        textView_time.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("GPSBar", "onClick: " + clickTime);

                if (clickTime == 0) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("GPSBar", "clickTime reverse to 0 ");
                            clickTime = 0;
                        }
                    }, 1000);
                }
                clickTime++;

                if (clickTime >= 5) {
                    System.exit(0);
                }

            }
        });


        // for test串口调试界面 看是否通
//        textView_location_status.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //显示串口测试activity
//                mainActivity.startActivity(new Intent(mainActivity, SerialPortActivity.class));
//            }
//        });
/*
        // for test 测试收到新的短信息
        findViewById(R.id.sms_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 收到新短信
                SoundPlay.playMessageSound(mainActivity);

                mainActivity.addMessage("123456", "测试收到新的短消息", false);
                mainActivity.modifyGpsBarMessageCount();
                newMsgToast.show();
            }
        });

        // for test 测试打卡
        findViewById(R.id.sign_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.showIDCardDialog("33028319881122013X", "张三", "汉", "浙江省奉化市锦屏街道凉河路x幢xxx室");
            }
        });

        // for test 测试收到报警
        findViewById(R.id.alert_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.showDangerDialog();
            }
        });

        // for test 截图
        findViewById(R.id.screen_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                screenshot();
//                Intent intent = new Intent(mainActivity, TestActivity.class);
//                mainActivity.startActivity(intent);
            }
        });

        // for test 亮度
        findViewById(R.id.bright_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ScreenBrightness.modifyBrightness(mainActivity);
            }
        });

        */

        textViews.add(textView_latitude);
        textViews.add(textView_longitude);
        textViews.add(textView_speed);
        textViews.add(textView_heading);
        textViews.add(textView_location_status);
        textViews.add(textView_message);
        textViews.add(textView_time);

        for (TextView textview : textViews) {
            textview.getPaint().setFakeBoldText(true);
            textview.setTextColor(0xFF000000);
        }
    }

    private void screenshot() {

        String IMAGE_DIR = Environment.getExternalStorageDirectory() + File.separator + "Android截屏";
        System.out.println(IMAGE_DIR);
        final String SCREEN_SHOT = "screenshot.png";

        // 获取屏幕
        View dView = mainActivity.getWindow().getDecorView();
        dView.setDrawingCacheEnabled(true);
        dView.buildDrawingCache();
        Bitmap bmp = dView.getDrawingCache();

        //二次截图
//        Bitmap saveBitmap = Bitmap.createBitmap(DensityUtil.getScreenWidth(mainActivity.getApplicationContext(), mainActivity), DensityUtil.getScreenHeight(mainActivity.getApplicationContext(), mainActivity), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(saveBitmap);
//        Paint paint = new Paint();
//        canvas.drawBitmap(bmp, new Rect(0, 0, DensityUtil.getScreenWidth(mainActivity.getApplicationContext(), mainActivity), DensityUtil.getScreenHeight(mainActivity.getApplicationContext(), mainActivity)),
//                new Rect(0, 0, DensityUtil.getScreenWidth(mainActivity.getApplicationContext(), mainActivity), DensityUtil.getScreenHeight(mainActivity.getApplicationContext(), mainActivity)), paint);


        File imageDir = new File(IMAGE_DIR);
        if (!imageDir.exists()) {
            imageDir.mkdir();
        }
        String imageName = SCREEN_SHOT;
        File file = new File(imageDir, imageName);
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileOutputStream os = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.flush();
            os.close();

            //将截图保存至相册并广播通知系统刷新
            MediaStore.Images.Media.insertImage(mainActivity.getContentResolver(), file.getAbsolutePath(), imageName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setData() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("latitude", "N 30°46.225′");
            jsonObject.put("longitude", "E 120°39.510′");
            jsonObject.put("speed", "10.3Kt");
            jsonObject.put("heading", "85°");
            jsonObject.put("gps", false);
            jsonObject.put("messageNumber", 3);

            textView_latitude.setText(jsonObject.getString("latitude"));
            textView_longitude.setText(jsonObject.getString("longitude"));
            textView_speed.setText(jsonObject.getString("speed"));
            textView_heading.setText(jsonObject.getString("heading"));

//            if (jsonObject.getBoolean("gps")) {
//                textView_location_status.setTextColor(0xFF2657EC);
//                textView_location_status.setText("已定位");
//                noGps = false;
//            } else {
//                textView_location_status.setTextColor(0xFFD0021B);
//                textView_location_status.setText("未定位");
//                noGps = true;
//            }

            setGPSStatus(jsonObject.getBoolean("gps"));

//            int messageNumber = jsonObject.getInt("messageNumber");
//            setMessageCount(messageNumber);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private long lastNoGpsReportTime = 0;
    private long noGpsReportPeriod = 1000 * 60 * 10;

    public void setGPSStatus(boolean gpsStatus) {
        textView_location_status.setTextColor(gpsStatus ? 0xFF2657EC : 0xFFD0021B);
        textView_location_status.setText(gpsStatus ? "已定位" : "卫星中断");
        if (gpsStatus) textView_location_status.setVisibility(VISIBLE);
        /*
        if (!gpsStatus && (Constant.SYSTEM_DATE.getTime() - lastNoGpsReportTime) >= noGpsReportPeriod) {
            if (mainActivity != null) {
                mainActivity.showMessageDialog("未获取定位", MessageDialogActivity.TYPE_ALERT);
                MainActivity.play("未获取定位");
                lastNoGpsReportTime = Constant.SYSTEM_DATE.getTime();
            }
        }
        */
        noGps = !gpsStatus;
    }

    public void setAisStatus(boolean aisStatus) {
        textView_ais_status.setTextColor(aisStatus ? 0xFF2657EC : 0xFFD0021B);
        textView_ais_status.setText(aisStatus ? "AIS已连接" : "AIS未连接");
        if (aisStatus) textView_ais_status.setVisibility(VISIBLE);
        noAisConnected = !aisStatus;
    }

    class TimeHandler extends Thread {
        @Override
        public void run() {
            super.run();
            bootTime = Constant.SYSTEM_DATE.getTime();
            int i = 0;
            do {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {

                }


                if (i % 10 == 0) {
                    // 更新时间
                    Calendar c = Calendar.getInstance();
                    c.setTime(Constant.SYSTEM_DATE);
                    c.add(Calendar.SECOND, 1);
                    Constant.SYSTEM_DATE = c.getTime();

                    Message message = new Message();
                    message.what = UPDATE_TIME;
                    handler.sendMessage(message);

                    /*
                    // 获取位置静态变量的时间
                    Date date = MyApplication.getInstance().getCurrentLocation().getAcqtime();
                    if (date != null) {
                        long locationTime = date.getTime();
                        Message message1 = new Message();
                        message1.what = UPDATE_GPS_STATUS;
                        // 比较下 当前app时间 和 定位时间的大小 如果小于10分钟 则为true
                        boolean obj = (Constant.SYSTEM_DATE.getTime() - locationTime) <= noGpsReportPeriod;
                        message1.obj = obj;

                        // 如果未定位 10分钟之内 不提示， 10分钟之后 提示
                        if (!obj) {
                            // msg.obj = false;
                            // bootTime 为初始化时间
                            if ((Constant.SYSTEM_DATE.getTime() - bootTime) > 60 * 1000 * 10) {
                                handler.sendMessage(message1);
                            }
                        } else {
                            // msg.obj = true
                            handler.sendMessage(message1);
                        }
                    }
                    */
                }

                // 未定位闪烁
                if (Constant.NO_GPS_FLASH_TIME != 0) {
                    int noGpsFlashTime = Constant.NO_GPS_FLASH_TIME / 100;
                    if (i % noGpsFlashTime == 0) {
                        flashTextViewVisible = !flashTextViewVisible;
                        Message message = new Message();
                        message.what = FLASH_NO_GPS;
                        handler.sendMessage(message);
                    }
                }

                // 无AIS闪烁
                if (Constant.NO_AIS_FLASH_TIME != 0) {
                    int noGpsFlashTime = Constant.NO_GPS_FLASH_TIME / 100;
                    if (i % noGpsFlashTime == 0) {
                        flashAisTextViewVisible = !flashAisTextViewVisible;
                        Message message = new Message();
                        message.what = FLASH_NO_AIS;
                        handler.sendMessage(message);
                    }
                }

                if (i == 10000) i = 0;
                i++;
            } while (true);
        }
    }

    long bootTime = 0;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_TIME:
                    textView_time.setText(DateUtil.Date2String(Constant.SYSTEM_DATE, "yyyy年MM月dd日 HH:mm:ss"));
                    break;
                case FLASH_NO_GPS:
                    if (noGps) {
                        textView_location_status.setVisibility(flashTextViewVisible ? VISIBLE : INVISIBLE);
                    }
                    if (PreferencesUtils.getBoolean(mainActivity, "flashAlert", false)) {
                        textView_alert.setVisibility(flashTextViewVisible ? VISIBLE : INVISIBLE);
                    }
                    break;
                case FLASH_NO_AIS:
                    if (noAisConnected) {
                        textView_ais_status.setVisibility(flashAisTextViewVisible ? VISIBLE : INVISIBLE);
                    }
                    break;
                case UPDATE_GPS_STATUS:
                    boolean b = (boolean) msg.obj;
                    setGPSStatus(b);
                    break;
                default:
                    break;
            }
        }
    };

    public void modifyMessageCount(long count) {
        if (count != 0) {
            if (count < 100) {
                textView_message.setText("短信");
                textView_message_number.setText(count + "");
                textView_message_number.setVisibility(VISIBLE);
            } else {
                textView_message.setText("短信");
                textView_message_number.setText("..");
                textView_message_number.setVisibility(VISIBLE);
            }

        } else {
            textView_message.setText("无短信");
            textView_message_number.setText("-");
            textView_message_number.setVisibility(INVISIBLE);
        }
    }

    public void showAlerting(boolean show) {
        textView_alerting.setVisibility(show ? VISIBLE : GONE);
    }

    public void setDebugButtonLayoutShow(boolean show) {
        System.out.println((show ? "打开" : "关闭") + "DEBUG按钮组");
        debug_btn_layout.setVisibility(show ? VISIBLE : INVISIBLE);
    }

    public void cancelAlert() {
        PreferencesUtils.putBoolean(mainActivity, "flashAlert", false);
        textView_alert.setVisibility(INVISIBLE);
    }
}
