package com.cetcme.xkterminal.Fragment.setting;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cetcme.xkterminal.Event.SmsEvent;
import com.cetcme.xkterminal.MainActivity;
import com.cetcme.xkterminal.MyClass.PreferencesUtils;
import com.cetcme.xkterminal.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.xutils.view.annotation.Event;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class DBSettingFragment extends Fragment {


    @BindView(R.id.tv_address) TextView tv_address;
    @BindView(R.id.tv_voltage) TextView tv_voltage;

    Unbinder unbinder;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_db, container, false);
        EventBus.getDefault().register(this);
        unbinder = ButterKnife.bind(this, view);
        initData(view);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        unbinder.unbind();
    }

    private void initData(View view) {
        tv_address.setText(PreferencesUtils.getString(getActivity(), "myNumber"));

        double voltage = ((MainActivity)getActivity()).voltage;
        tv_voltage.setText(String.format("%.2f", voltage));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SmsEvent smsEvent) {
        try {
            String type = smsEvent.getReceiveJson().getString("apiType");

            switch (type) {
                case "voltage":
                    double voltage = smsEvent.getReceiveJson().getDouble("voltage");
                    tv_voltage.setText(String.format("%.2f", voltage));
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
