package com.cetcme.xkterminal.ActionBar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cetcme.xkterminal.MainActivity;
import com.cetcme.xkterminal.R;

import java.util.ArrayList;

/**
 * Created by qiuhong on 10/01/2018.
 */

public class MessageBar extends RelativeLayout implements View.OnClickListener {

    public MainActivity mainActivity;

    private Button button_new;
    private Button button_detail;
    private Button button_relay;
    private Button button_pre;
    private Button button_next;
    private Button button_back;
    
    private ArrayList<Button> buttons = new ArrayList<>();

    public MessageBar(Context context) {
        super(context);
    }

    public MessageBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.bar_message_view, this, true);

        bindView(view);

    }

    private void bindView(View view) {
        button_new      = view.findViewById(R.id.button_new);
        button_detail   = view.findViewById(R.id.button_detail);
        button_relay    = view.findViewById(R.id.button_relay);
        button_pre      = view.findViewById(R.id.button_prev);
        button_next     = view.findViewById(R.id.button_next);
        button_back     = view.findViewById(R.id.button_back);

        buttons.add(button_new);
        buttons.add(button_detail);
        buttons.add(button_relay);
        buttons.add(button_pre);
        buttons.add(button_next);
        buttons.add(button_back);

        for (Button button: buttons) {
            button.setTextColor(0xFF000000);
            button.setBackgroundResource(R.drawable.button_bg_selector);
            button.setOnClickListener(this);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_new:
                Toast.makeText(getContext(), "button_new", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_detail:
                Toast.makeText(getContext(), "button_detail", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_relay:
                Toast.makeText(getContext(), "button_relay", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_prev:
                mainActivity.prevPage();
                break;
            case R.id.button_next:
                mainActivity.nextPage();
                break;
            case R.id.button_back:
                mainActivity.initMainFragment();
                break;
            default:
                break;
        }
    }
}
