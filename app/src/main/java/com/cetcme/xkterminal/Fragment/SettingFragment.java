package com.cetcme.xkterminal.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cetcme.xkterminal.ActionBar.TitleBar;
import com.cetcme.xkterminal.MyClass.PreferencesUtils;
import com.cetcme.xkterminal.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by qiuhong on 10/01/2018.
 */

public class SettingFragment extends Fragment {

    private TextView address_textView;
    private TextView signal_textView;
    private TextView location_freq_textView;
    private TextView gps_freq_textView;
    private TextView central_number_textView;

    private TextView location_from_textView;
    private TextView communication_from_textView;
    private TextView signal_power_textView;
    private TextView satellite_count_textView;
    private TextView broad_temp_textView;
    private TextView li_voltage_textView;
    private TextView sun_voltage_textView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_setting,container,false);

        TitleBar titleBar = view.findViewById(R.id.titleBar);
        titleBar.setTitle("北斗参数");

        address_textView            = view.findViewById(R.id.address_textView);
        signal_textView             = view.findViewById(R.id.signal_textView);
        location_freq_textView      = view.findViewById(R.id.location_freq_textView);
        gps_freq_textView           = view.findViewById(R.id.gps_freq_textView);
        central_number_textView     = view.findViewById(R.id.central_number_textView);

        location_from_textView      = view.findViewById(R.id.location_from_textView);
        communication_from_textView = view.findViewById(R.id.communication_from_textView);
        signal_power_textView       = view.findViewById(R.id.signal_power_textView);
        satellite_count_textView    = view.findViewById(R.id.satellite_count_textView);
        broad_temp_textView         = view.findViewById(R.id.broad_temp_textView);
        li_voltage_textView         = view.findViewById(R.id.li_voltage_textView);
        sun_voltage_textView        = view.findViewById(R.id.sun_voltage_textView);

        getData();

        return view;
    }

    private void getData() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("address", "168857");
            jsonObject.put("signal", "78");
            jsonObject.put("location_per", "5min");
            jsonObject.put("gps_per", "1s");
            jsonObject.put("central_number", "378378");

            address_textView        .setText(PreferencesUtils.getString(getActivity(), "myNumber"));
            signal_textView         .setText(jsonObject.getString("signal"));
            location_freq_textView  .setText(jsonObject.getString("location_per"));
            gps_freq_textView       .setText(jsonObject.getString("gps_per"));
            central_number_textView .setText(jsonObject.getString("central_number"));

            location_from_textView.setText("GPS/BD");
            communication_from_textView.setText("GPRS");
            signal_power_textView.setText("30");
            satellite_count_textView.setText("8");
            broad_temp_textView.setText("-7℃");
            li_voltage_textView.setText("4.16(96%)V");
            sun_voltage_textView.setText("3.12V");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
