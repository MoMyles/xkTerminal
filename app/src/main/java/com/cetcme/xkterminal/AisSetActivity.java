package com.cetcme.xkterminal;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.cetcme.xkterminal.Fragment.setting.WarningSettingFragment;

import butterknife.Unbinder;

public class AisSetActivity extends FragmentActivity {

    private WarningSettingFragment warningSettingFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ais_set);

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        warningSettingFragment = new WarningSettingFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fl_content, warningSettingFragment, null);
        ft.commit();
    }
}
