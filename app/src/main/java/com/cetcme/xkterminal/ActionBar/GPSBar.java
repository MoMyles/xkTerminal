package com.cetcme.xkterminal.ActionBar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cetcme.xkterminal.DataFormat.MessageFormat;
import com.cetcme.xkterminal.MainActivity;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.DateUtil;
import com.cetcme.xkterminal.MyClass.DensityUtil;
import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.SerialTest.SerialPortActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;

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
    private TextView textView_message;
    private TextView textView_time;

    private TextView textView_message_number;

    private Realm realm;

    private boolean noGps = true;

    // 用于关闭app
    private int clickTime = 0;

    private ArrayList<TextView> textViews = new ArrayList<>();

    public GPSBar(Context context) {
        super(context);
    }

    public GPSBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        View view = LayoutInflater.from(context).inflate(R.layout.bar_gps_view, this, true);

        bindView(view);
        setData();

        new TimeHandler().start();
    }

    private void bindView(View view) {
        textView_latitude           = view.findViewById(R.id.textView_latitude);
        textView_longitude          = view.findViewById(R.id.textView_longitude);
        textView_speed              = view.findViewById(R.id.textView_speed);
        textView_heading            = view.findViewById(R.id.textView_heading);
        textView_location_status    = view.findViewById(R.id.textView_location_status);
        textView_message            = view.findViewById(R.id.textView_message);
        textView_time               = view.findViewById(R.id.textView_time);

        textView_message_number     = view.findViewById(R.id.textView_message_number);


        textView_message.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mainActivity.fragmentName.equals("message")) mainActivity.initMessageFragment("receive");
            }
        });

        textView_message_number.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mainActivity.fragmentName.equals("message")) mainActivity.initMessageFragment("receive");
            }
        });

        // TODO: for test 串口调试界面 看是否通
        textView_location_status.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //测试定位状态
//                System.out.println("textView_location_status clicked ++++++++++++++ nogps : " + noGps);
//                noGps = !noGps;
//                setGPSStatus(!noGps);

                //显示串口测试activity
                mainActivity.startActivity(new Intent(mainActivity, SerialPortActivity.class));
            }
        });

        // TODO: for test 测试收到新的短信息
        textView_heading.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 收到新短信

                mainActivity.addMessage("654321", "测试收到新的短消息");
                mainActivity.modifyGpsBarMessageCount();
                Toast.makeText(view.getContext(), "您有新的短信", Toast.LENGTH_SHORT).show();
            }
        });

        // TODO: for test 多次点击退出app
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

        // TODO: for test 测试打卡
        textView_latitude.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.showIDCardDialog("33028319881122013X", "张三", "汉", "浙江省奉化市锦屏街道凉河路x幢xxx室");
            }
        });

        // TODO: for test 测试收到报警
        textView_longitude.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.showDangerDialog();
            }
        });

        textView_speed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                screenshot();
            }
        });

        textViews.add(textView_latitude);
        textViews.add(textView_longitude);
        textViews.add(textView_speed);
        textViews.add(textView_heading);
        textViews.add(textView_location_status);
        textViews.add(textView_message);
        textViews.add(textView_time);

        for (TextView textview: textViews) {
            textview.getPaint().setFakeBoldText(true);
//            textview.setTextSize(14); //22
            textview.setTextColor(0xFF000000);
        }
    }

    private void screenshot() {

        String IMAGE_DIR = Environment.getExternalStorageDirectory() + File.separator + "Android截屏";
        System.out.println(IMAGE_DIR);
        final String SCREEN_SHOT ="screenshot.png";

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

    public void setGPSStatus(boolean gpsStatus) {
        textView_location_status.setTextColor(gpsStatus ? 0xFF2657EC : 0xFFD0021B);
        textView_location_status.setText(gpsStatus ? "已定位" : "未定位");
        if (gpsStatus) textView_location_status.setVisibility(VISIBLE);
        noGps = !gpsStatus;
    }

    class TimeHandler extends Thread{
        @Override
        public void run() {
            super.run();
            int i = 0;
            do {
                try {
                    Thread.sleep(100);
                }
                catch (Exception e) {

                }

                if (i % 10 == 0) {

                    Calendar c = Calendar.getInstance();
                    c.setTime(Constant.SYSTEM_DATE);
                    c.add(Calendar.SECOND, 1);
                    Constant.SYSTEM_DATE = c.getTime();

                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }

                if (Constant.NO_GPS_FLASH_TIME != 0) {
                    int noGpsFlashTime = Constant.NO_GPS_FLASH_TIME / 100;
                    if (noGps && i % noGpsFlashTime == 0) {
                        Message message = new Message();
                        message.what = 3;
                        handler.sendMessage(message);
                    }
                    if (i == 10 * noGpsFlashTime) i = 0;
                }

                i++;
            } while (true);
        }
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
//                    long sysTime = System.currentTimeMillis();
//                    CharSequence sysTimeStr = DateFormat.format("HH:mm:ss", sysTime);
//                    textView_time.setText(sysTimeStr); //更新时间
                    textView_time.setText(DateUtil.Date2String(Constant.SYSTEM_DATE, "HH:mm:ss"));
                    break;
                case 3:
                    textView_location_status.setVisibility(textView_location_status.getVisibility() == VISIBLE ? INVISIBLE : VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };

    public void modifyMessageCount(long count) {
        if (count != 0) {
            textView_message.setText("短信");
            textView_message_number.setText(count + "");
            textView_message_number.setVisibility(VISIBLE);
        } else {
            textView_message.setText("无短信");
            textView_message_number.setText("-");
            textView_message_number.setVisibility(INVISIBLE);
        }
    }


}
