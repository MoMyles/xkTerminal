package com.cetcme.xkterminal.Fragment.setting;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cetcme.xkterminal.MyClass.PreferencesUtils;
import com.cetcme.xkterminal.R;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherInfoFragment extends Fragment implements View.OnClickListener {

    @BindView(R.id.tv_wind_speed)        TextView tv_wind_speed;
    @BindView(R.id.tv_wind_head)         TextView tv_wind_head;
    @BindView(R.id.tv_wind_level)        TextView tv_wind_level;
    @BindView(R.id.tv_air_pressure)      TextView tv_air_pressure;
    @BindView(R.id.tv_wave_height)       TextView tv_wave_height;
    @BindView(R.id.tv_air_temp)          TextView tv_air_temp;

    Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather_info, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void initView() {
        String windSpeed = PreferencesUtils.getString(getActivity(), "windSpeed", "");
        tv_wind_speed.setText(windSpeed);
        tv_wind_speed.setOnClickListener(this);

        String windHead = PreferencesUtils.getString(getActivity(), "windHead", "");
        tv_wind_head.setText(windHead);
        tv_wind_head.setOnClickListener(this);

        String windLevel = PreferencesUtils.getString(getActivity(), "windLevel", "");
        tv_wind_level.setText(windLevel);
        tv_wind_level.setOnClickListener(this);

        String windPressure = PreferencesUtils.getString(getActivity(), "airPressure", "");
        tv_air_pressure.setText(windPressure);
        tv_air_pressure.setOnClickListener(this);

        String waveHeight = PreferencesUtils.getString(getActivity(), "waveHeight", "");
        tv_wave_height.setText(waveHeight);
        tv_wave_height.setOnClickListener(this);

        String airTemp = PreferencesUtils.getString(getActivity(), "airTemp", "");
        tv_air_temp.setText(airTemp);
        tv_air_temp.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_wind_speed:
                changeData("风速", "windSpeed");
                break;
            case R.id.tv_wind_head:
                changeData("风向", "windHead");
                break;
            case R.id.tv_wind_level:
                changeData("风速", "windLevel");
                break;
            case R.id.tv_air_pressure:
                changeData("气压", "airPressure");
                break;
            case R.id.tv_wave_height:
                changeData("浪高", "waveHeight");
                break;
            case R.id.tv_air_temp:
                changeData("温度", "airTemp");
                break;
        }

    }

    private void changeData(final String description, final String pName) {

        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getActivity());
        builder.setTitle("提示")
                .setPlaceholder("在此输入" + description)
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("确定", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        CharSequence text = builder.getEditText().getText();
                        if (text != null && text.length() > 0) {
                            Toast.makeText(getActivity(), description + "修改成功: " + text, Toast.LENGTH_SHORT).show();
                            PreferencesUtils.putString(getActivity(), pName, text.toString());
                            initView();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getActivity(), "请填入" + description, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }
}
