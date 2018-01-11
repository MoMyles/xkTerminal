package com.cetcme.xkterminal.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cetcme.xkterminal.ActionBar.TitleBar;
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_setting,container,false);

        TitleBar titleBar = view.findViewById(R.id.titleBar);
        titleBar.setTitle("北斗参数");

        address_textView        = view.findViewById(R.id.address_textView);
        signal_textView         = view.findViewById(R.id.signal_textView);
        location_freq_textView  = view.findViewById(R.id.location_freq_textView);
        gps_freq_textView       = view.findViewById(R.id.gps_freq_textView);
        central_number_textView = view.findViewById(R.id.central_number_textView);

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

            address_textView        .setText(jsonObject.getString("address"));
            signal_textView         .setText(jsonObject.getString("signal"));
            location_freq_textView  .setText(jsonObject.getString("location_per"));
            gps_freq_textView       .setText(jsonObject.getString("gps_per"));
            central_number_textView .setText(jsonObject.getString("central_number"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
