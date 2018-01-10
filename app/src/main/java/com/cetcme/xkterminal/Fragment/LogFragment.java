package com.cetcme.xkterminal.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cetcme.xkterminal.ActionBar.TitleBar;
import com.cetcme.xkterminal.R;

/**
 * Created by qiuhong on 10/01/2018.
 */

public class LogFragment extends Fragment{

    private String tg;
    private TitleBar titleBar;

    public LogFragment(String tg) {
        this.tg = tg;
        Log.e("Main", "LogFragment: " + tg );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_log,container,false);
        titleBar = view.findViewById(R.id.titleBar);
        if (tg.equals("sign")) titleBar.setTitle("打卡记录");
        if (tg.equals("alert")) titleBar.setTitle("报警记录");
        return view;
    }

    public void nextPage() {
        Log.e("Main", "LogFragment: next");
    }

    public void prevPage() {
        Log.e("Main", "LogFragment: prev");
    }
}
