package com.cetcme.xkterminal;

import android.app.Activity;
import android.os.Handler;
import android.os.Bundle;
import android.os.Message;
import android.view.MotionEvent;
import android.widget.TextView;

import com.cetcme.xkterminal.MyClass.Constant;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class IDCardActivity extends Activity {

    private TextView name_textView;
    private TextView sex_textView;
    private TextView nation_textView;
    private TextView year_textView;
    private TextView month_textView;
    private TextView day_textView;
    private TextView address_textView;
//    private FontTextView idCard_textView;

    private TextView idCard_0_textView;
    private TextView idCard_1_textView;
    private TextView idCard_2_textView;
    private TextView idCard_3_textView;
    private TextView idCard_4_textView;
    private TextView idCard_5_textView;
    private TextView idCard_6_textView;
    private TextView idCard_7_textView;
    private TextView idCard_8_textView;
    private TextView idCard_9_textView;
    private TextView idCard_10_textView;
    private TextView idCard_11_textView;
    private TextView idCard_12_textView;
    private TextView idCard_13_textView;
    private TextView idCard_14_textView;
    private TextView idCard_15_textView;
    private TextView idCard_16_textView;
    private TextView idCard_17_textView;
    private ArrayList<TextView> idTextViewArr = new ArrayList<>();

    boolean needDismissActivity = true;
    int countDown = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idcard_new);
        bindView();

        ((MyApplication)getApplication()).idCardActivity = this;

        setData(getIntent().getExtras());


        if (Constant.IDCARD_REMAIN_TIME != 0) {
            new TimeHandler().start();
        }

    }

    class TimeHandler extends Thread {
        @Override
        public void run() {
            super.run();
            do {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
                countDown--;
                System.out.println(countDown);
            } while (countDown != 0);
            if (needDismissActivity) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onBackPressed();
                    }
                });
            }
        }
    }

    private void bindView() {
        name_textView       = findViewById(R.id.name_textView);
        sex_textView        = findViewById(R.id.sex_textView);
        nation_textView     = findViewById(R.id.nation_textView);
        year_textView       = findViewById(R.id.year_textView);
        month_textView      = findViewById(R.id.month_textView);
        day_textView        = findViewById(R.id.day_textView);
        address_textView    = findViewById(R.id.address_textView);
//        idCard_textView     = findViewById(R.id.idCard_textView);
        idCard_0_textView   = findViewById(R.id.idCard_0_textView);
        idCard_1_textView   = findViewById(R.id.idCard_1_textView);
        idCard_2_textView   = findViewById(R.id.idCard_2_textView);
        idCard_3_textView   = findViewById(R.id.idCard_3_textView);
        idCard_4_textView   = findViewById(R.id.idCard_4_textView);
        idCard_5_textView   = findViewById(R.id.idCard_5_textView);
        idCard_6_textView   = findViewById(R.id.idCard_6_textView);
        idCard_7_textView   = findViewById(R.id.idCard_7_textView);
        idCard_8_textView   = findViewById(R.id.idCard_8_textView);
        idCard_9_textView   = findViewById(R.id.idCard_9_textView);
        idCard_10_textView   = findViewById(R.id.idCard_10_textView);
        idCard_11_textView   = findViewById(R.id.idCard_11_textView);
        idCard_12_textView   = findViewById(R.id.idCard_12_textView);
        idCard_13_textView   = findViewById(R.id.idCard_13_textView);
        idCard_14_textView   = findViewById(R.id.idCard_14_textView);
        idCard_15_textView   = findViewById(R.id.idCard_15_textView);
        idCard_16_textView   = findViewById(R.id.idCard_16_textView);
        idCard_17_textView   = findViewById(R.id.idCard_17_textView);

        idTextViewArr.add(idCard_0_textView);
        idTextViewArr.add(idCard_1_textView);
        idTextViewArr.add(idCard_2_textView);
        idTextViewArr.add(idCard_3_textView);
        idTextViewArr.add(idCard_4_textView);
        idTextViewArr.add(idCard_5_textView);
        idTextViewArr.add(idCard_6_textView);
        idTextViewArr.add(idCard_7_textView);
        idTextViewArr.add(idCard_8_textView);
        idTextViewArr.add(idCard_9_textView);
        idTextViewArr.add(idCard_10_textView);
        idTextViewArr.add(idCard_11_textView);
        idTextViewArr.add(idCard_12_textView);
        idTextViewArr.add(idCard_13_textView);
        idTextViewArr.add(idCard_14_textView);
        idTextViewArr.add(idCard_15_textView);
        idTextViewArr.add(idCard_16_textView);
        idTextViewArr.add(idCard_17_textView);

    }

    public void setData(Bundle bundle) {
        countDown = Constant.IDCARD_REMAIN_TIME / 1000;
        System.out.println("set countdown " + countDown);
        String name = bundle.getString("name");
        String sex = bundle.getString("sex");
        String birthday = bundle.getString("birthday");
        String address = bundle.getString("address");
        String idCard = bundle.getString("idCard");
        String nation = bundle.getString("nation");

        name = name.replace(" ", "");

        if (name.length() == 2) {
            String xing = name.substring(0, 1);
            name = name.substring(1, 2);
            name = xing + "   " + name;
        }

        name_textView.setText(name);
        sex_textView.setText(sex);
        nation_textView.setText(nation);
        year_textView.setText(birthday.substring(0,4));
        month_textView.setText(birthday.substring(5,7));
        day_textView.setText(birthday.substring(8,10));
        address_textView.setText(address);
//        idCard_textView.setText(idCard);

        for (int i = 0; i < idCard.length(); i++) {
            String str = idCard.substring(i, i + 1);
            setIdCardNumberImg(idTextViewArr.get(i), str);
        }


    }

    private void setIdCardNumberImg(TextView textView, String str) {
        switch (str) {
            case "0":
                textView.setBackgroundResource(R.drawable.ocb_0);
                break;
            case "1":
                textView.setBackgroundResource(R.drawable.ocb_1);
                break;
            case "2":
                textView.setBackgroundResource(R.drawable.ocb_2);
                break;
            case "3":
                textView.setBackgroundResource(R.drawable.ocb_3);
                break;
            case "4":
                textView.setBackgroundResource(R.drawable.ocb_4);
                break;
            case "5":
                textView.setBackgroundResource(R.drawable.ocb_5);
                break;
            case "6":
                textView.setBackgroundResource(R.drawable.ocb_6);
                break;
            case "7":
                textView.setBackgroundResource(R.drawable.ocb_7);
                break;
            case "8":
                textView.setBackgroundResource(R.drawable.ocb_8);
                break;
            case "9":
                textView.setBackgroundResource(R.drawable.ocb_9);
                break;
            case "X":
            case "x":
            case "A":
                textView.setBackgroundResource(R.drawable.ocb_x_big);
                break;
            default:
                break;
        }
    }


    /**
     * 点击视图外 关闭窗口
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        onBackPressed();
        return true;
    }

    protected void onDestroy() {
        needDismissActivity = false;
        MainActivity.idCardDialogOpen = false;
        ((MyApplication)getApplication()).idCardActivity = null;
        super.onDestroy();
    }



}
