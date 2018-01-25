package com.cetcme.xkterminal.ActionBar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cetcme.xkterminal.MainActivity;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.SerialTest.SerialPortActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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

        // TODO: for test
        textView_location_status.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.startActivity(new Intent(mainActivity, SerialPortActivity.class));
            }
        });

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

        // TODO: for test
        textView_latitude.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.showIDCardDialog("330283198811220134", "张三", "汉", "浙江省奉化市锦屏街道凉河路x幢xxx室");
            }
        });

        // TODO: for test
        textView_longitude.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.showDangerDialog();
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

            if (jsonObject.getBoolean("gps")) {
                textView_location_status.setTextColor(0xFF2657EC);
                textView_location_status.setText("已定位");
                noGps = false;
            } else {
                textView_location_status.setTextColor(0xFFD0021B);
                textView_location_status.setText("未定位");
                noGps = true;
            }

//            int messageNumber = jsonObject.getInt("messageNumber");
//            setMessageCount(messageNumber);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private boolean noGps = true;

    class HalfTimeHandler extends Thread{
        @Override
        public void run() {
            super.run();
            while (noGps && Constant.NO_GPS_FLASH_TIME != 0) {
                try {
                    Message message = new Message();
                    message.what = 3;
                    handler.sendMessage(message);
                    Thread.sleep(Constant.NO_GPS_FLASH_TIME);
                }
                catch (Exception e) {

                }
            }
        }
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
                    long sysTime = System.currentTimeMillis();
                    CharSequence sysTimeStr = DateFormat.format("HH:mm:ss", sysTime);
                    textView_time.setText(sysTimeStr); //更新时间
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
