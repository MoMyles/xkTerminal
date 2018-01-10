package com.cetcme.xkterminal.ActionBar;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cetcme.xkterminal.R;

import java.util.ArrayList;

/**
 * Created by qiuhong on 10/01/2018.
 */

public class GPSBar extends RelativeLayout {

    private TextView textView_latitude;
    private TextView textView_longitude;
    private TextView textView_speed;
    private TextView textView_heading;
    private TextView textView_location_status;
    private TextView textView_message;
    private TextView textView_time;

    private ArrayList<TextView> textViews = new ArrayList<>();

    public GPSBar(Context context) {
        super(context);
    }

    public GPSBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.bar_gps_view, this, true);

        bindView(view);
        yoyo();
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

        textViews.add(textView_latitude);
        textViews.add(textView_longitude);
        textViews.add(textView_speed);
        textViews.add(textView_heading);
        textViews.add(textView_location_status);
        textViews.add(textView_message);
        textViews.add(textView_time);

        for (TextView textview: textViews) {
            textview.getPaint().setFakeBoldText(true);
            textview.setTextSize(22);
            textview.setTextColor(0xFF000000);
        }
    }

    private void yoyo() {
        textView_location_status.setTextColor(0xFFD0021B);
        textView_location_status.setText("未定位");
    }

    class TimeHandler extends Thread{
        @Override
        public void run() {
            super.run();
            do {
                try {
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                    Thread.sleep(1000);
                }
                catch (Exception e) {

                }
            } while (true);
        }
    }

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
                default:
                    break;
            }
        }
    };


}
