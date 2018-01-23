package com.cetcme.xkterminal.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.cetcme.xkterminal.ActionBar.TitleBar;
import com.cetcme.xkterminal.MainActivity;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.R;

/**
 * Created by qiuhong on 11/01/2018.
 */

@SuppressLint("ValidFragment")
public class MessageNewFragment extends Fragment{

    public MainActivity mainActivity;

    private String tg;
    private String receive;
    private String content;
    private String time;

    private TitleBar titleBar;

    private EditText receiver_editText;
    private EditText content_editText;

    private TextView text_count_textView;
    private TextView last_send_textView;
    private TextView sender_or_receiver_textView;

    public MessageNewFragment(String tg, String receive, String content, String time) {
        this.tg = tg;
        this.receive = receive;
        this.content = content;
        this.time = time;
        Log.e("Main", "MessageFragment: " + tg );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_message_new,container,false);

        titleBar = view.findViewById(R.id.titleBar);
        receiver_editText = view.findViewById(R.id.receiver_editText);
        content_editText = view.findViewById(R.id.content_editText);

        text_count_textView = view.findViewById(R.id.text_count_textView);
        last_send_textView = view.findViewById(R.id.last_send_textView);

        sender_or_receiver_textView = view.findViewById(R.id.sender_or_receiver_textView);

        if (tg.equals("new")) {
            titleBar.setTitle("新建短信");
            text_count_textView.setText("剩余短信字数：" + (Constant.MESSAGE_CONTENT_MAX_LENGTH - content.length()));
        }
        if (tg.equals("relay")) {
            titleBar.setTitle("转发短信");
//            receiver_editText.setText(receive);
            content_editText.setText(content);
            text_count_textView.setText("剩余短信字数：" + (Constant.MESSAGE_CONTENT_MAX_LENGTH - content.length()));
        }

        if (tg.equals("detail")) {
            titleBar.setTitle("短信详情");
            receiver_editText.setText(receive);
            content_editText.setText(content);
            text_count_textView.setText("");
            receiver_editText.setEnabled(false);
            receiver_editText.setTextColor(0xFF000000);
            content_editText.setEnabled(false);
            content_editText.setTextColor(0xFF000000);

            if (mainActivity.messageListStatus.equals("receive")) sender_or_receiver_textView.setText("发件人：");
        }
        SharedPreferences sharedPreferences = mainActivity.getSharedPreferences("xkTerminal", Context.MODE_PRIVATE); //私有数据
        String lastSendTime = sharedPreferences.getString("lastSendTime", "");
        last_send_textView.setText(lastSendTime);

        content_editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (tg.equals("new")) {
                    text_count_textView.setText("剩余短信字数：" + (Constant.MESSAGE_CONTENT_MAX_LENGTH - content_editText.getText().length()));
                }
                if (tg.equals("relay")) {
                    text_count_textView.setText("剩余短信字数：" + (Constant.MESSAGE_CONTENT_MAX_LENGTH - content_editText.getText().length()));
                }
            }
        });

        return view;
    }

    public String getReceiver() {
        return receiver_editText.getText().toString();
    }

    public String getContent() {
        return content_editText.getText().toString();
    }


}
