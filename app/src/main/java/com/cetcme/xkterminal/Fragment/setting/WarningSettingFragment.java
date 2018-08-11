package com.cetcme.xkterminal.Fragment.setting;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.cetcme.xkterminal.MyClass.PreferencesUtils;
import com.cetcme.xkterminal.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class WarningSettingFragment extends Fragment {


    private Switch mWarn;
    private EditText mEtDistance;


    public WarningSettingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_warning_setting, container, false);
        onBindView(view);
        return view;
    }

    private void onBindView(View view) {
        mWarn = view.findViewById(R.id.switch_warn);
        mEtDistance = view.findViewById(R.id.et_warn_distance);

        mWarn.setChecked(PreferencesUtils.getBoolean(getActivity(), "warn_switch", false));
        mEtDistance.setText(PreferencesUtils.getInt(getActivity(), "warn_distance", 200)+"");

        mWarn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferencesUtils.putBoolean(getActivity(), "warn_switch", b);
            }
        });

        mEtDistance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (TextUtils.isEmpty(charSequence.toString())) return;
                PreferencesUtils.putInt(getActivity(), "warn_distance", Integer.valueOf(charSequence.toString()));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

}
