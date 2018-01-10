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

public class MessageFragment extends Fragment{

    private String tg;
    private TitleBar titleBar;

    public MessageFragment(String tg) {
        this.tg = tg;
        Log.e("Main", "MessageFragment: " + tg );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_message,container,false);
        titleBar = view.findViewById(R.id.titleBar);
        if (tg.equals("send")) titleBar.setTitle("发件箱");
        if (tg.equals("receive")) titleBar.setTitle("收件箱");
        return view;
    }

    public void nextPage() {
        Log.e("Main", "MessageFragment: next");
    }

    public void prevPage() {
        Log.e("Main", "MessageFragment: prev");
    }
}
