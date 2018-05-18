package com.cetcme.xkterminal;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.cetcme.xkterminal.DataFormat.InoutFormat;
import com.cetcme.xkterminal.Event.SmsEvent;
import com.cetcme.xkterminal.Sqlite.Bean.InoutBean;
import com.cetcme.xkterminal.Sqlite.Bean.LocationBean;
import com.qiuhong.qhlibrary.QHTitleView.QHTitleView;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.ex.DbException;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewInoutActivity extends Activity {

    @BindView(R.id.qhTitleView)
    QHTitleView qhTitleView;

    @BindView(R.id.rb_in)
    RadioButton rb_in;

    @BindView(R.id.rb_out)
    RadioButton rb_out;

    @BindView(R.id.et_count)
    EditText et_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_inout);
        ButterKnife.bind(this);

        initTitleView();
    }

    private void initTitleView() {
        qhTitleView.setTitle("进出港申报");
        qhTitleView.setBackView(R.mipmap.title_icon_back_2x);
        qhTitleView.setRightView(0);
        qhTitleView.setBackgroundResource(R.drawable.top_select);
        qhTitleView.setClickCallback(new QHTitleView.ClickCallback() {
            @Override
            public void onBackClick() {
                finish();
            }

            @Override
            public void onRightClick() {
                //
            }
        });
    }


    public void postInout(View view) {
        LocationBean currentLocation = MyApplication.getInstance().getCurrentLocation();

        //TODO: 测试
        currentLocation = new LocationBean();
        currentLocation.setLongitude(1212312340);
        currentLocation.setLatitude(312300000);

        if (currentLocation == null) {
            Toast.makeText(this, "未获取到自身定位", Toast.LENGTH_SHORT).show();
            return;
        }

        int type = 0;
        if (rb_in.isChecked()) {
            type = 1;
        }
        if (rb_out.isChecked()) {
            type = 2;
        }

        String countStr = et_count.getText().toString();
        if (countStr.isEmpty()) {
            Toast.makeText(this, "请填写人数", Toast.LENGTH_SHORT).show();
            return;
        }

        int count = Integer.parseInt(countStr);
        if (count > 255) count = 255;

        if (type == 0) {
            Toast.makeText(this, "请选择类型", Toast.LENGTH_SHORT).show();
            return;
        }

        InoutBean inoutBean = new InoutBean();
        inoutBean.setType(type);
        inoutBean.setCount(count);
        int lon = currentLocation.getLongitude();
        int lat = currentLocation.getLatitude();
        inoutBean.setLon(lon);
        inoutBean.setLat(lat);
        Date now = new Date();
        inoutBean.setTime(now);
        try {
            MyApplication.getInstance().getDb().saveBindingId(inoutBean);
        } catch (DbException e) {
            e.printStackTrace();
        }

        // TODO：发送编码 北斗号要确认下
        MyApplication.getInstance().sendBytes(
                InoutFormat.format("123456", MainActivity.myNumber, type, count, lon, lat, now)
        );

        Toast.makeText(this, "申报完成",Toast.LENGTH_SHORT).show();

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("apiType", "refreshInout");
            SmsEvent smsEvent = new SmsEvent(jsonObject);
            EventBus.getDefault().post(smsEvent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        finish();

    }
}