package com.cetcme.xkterminal.ActionBar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cetcme.xkterminal.R;

import java.util.ArrayList;

/**
 * Created by qiuhong on 10/01/2018.
 */

public class TitleBar  extends RelativeLayout {

    private TextView textView_title;

    public TitleBar(Context context) {
        super(context);
    }

    public TitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.bar_title_view, this, true);

        bindView(view);
    }

    private void bindView(View view) {
        textView_title = view.findViewById(R.id.textView_title);

        textView_title.getPaint().setFakeBoldText(true);
//        textView_title.setTextSize(12); //22
        textView_title.setTextColor(0xFF000000);
    }

    public void setTitle(String title) {
        textView_title.setText(title);
    }

}
