package com.cetcme.xkterminal;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

import com.cetcme.xkterminal.MyClass.Constant;

public class IDCardActivity extends Activity {

    private TextView name_textView;
    private TextView sex_textView;
    private TextView birthday_textView;
    private TextView address_textView;
    private TextView idCard_textView;

    boolean closed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idcard);
        bindView();
        setDate();


        if (Constant.IDCARD_REMAIN_TIME != 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!closed) {
                        onBackPressed();
                    }
                }
            }, Constant.IDCARD_REMAIN_TIME);
        }

    }

    private void bindView() {
        name_textView       = findViewById(R.id.name_textView);
        sex_textView        = findViewById(R.id.sex_textView);
        birthday_textView   = findViewById(R.id.birthday_textView);
        address_textView    = findViewById(R.id.address_textView);
        idCard_textView     = findViewById(R.id.idCard_textView);

    }

    private void setDate() {

        String name = getIntent().getExtras().getString("name");
        String sex = getIntent().getExtras().getString("sex");
        String birthday = getIntent().getExtras().getString("birthday");
        String address = getIntent().getExtras().getString("address");
        String idCard = getIntent().getExtras().getString("idCard");

        name_textView.setText(name);
        sex_textView.setText(sex);
        birthday_textView.setText(birthday);
        address_textView.setText(address);
        idCard_textView.setText(idCard);
    }


    /**
     * 点击视图外 关闭窗口
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        closed = true;
        onBackPressed();
        return true;
    }
}
