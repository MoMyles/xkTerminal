package com.cetcme.xkterminal.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;


import com.cetcme.xkterminal.ActionBar.TitleBar;
import com.cetcme.xkterminal.MainActivity;
import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.PreferencesUtils;
import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.RealmModels.Friend;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

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

    private Realm realm;

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

        realm = ((MyApplication) mainActivity.getApplication()).realm;

        titleBar = view.findViewById(R.id.titleBar);
        receiver_editText = view.findViewById(R.id.receiver_editText);
        content_editText = view.findViewById(R.id.content_editText);

        text_count_textView = view.findViewById(R.id.text_count_textView);
        last_send_textView = view.findViewById(R.id.last_send_textView);

        sender_or_receiver_textView = view.findViewById(R.id.sender_or_receiver_textView);

        if (tg.equals("new")) {
            titleBar.setTitle("新建短信");
            text_count_textView.setText(Constant.MESSAGE_CONTENT_MAX_LENGTH + "");
        }
        if (tg.equals("relay")) {
            titleBar.setTitle("转发短信");
//            receiver_editText.setText(receive);
            content_editText.setText(content);
            text_count_textView.setText(getRemainContentLength() + "");
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
            text_count_textView.setText(getRemainContentLength() + "");

            if (mainActivity.messageListStatus.equals("receive")) sender_or_receiver_textView.setText("发件人：");
        }

        last_send_textView.setText(PreferencesUtils.getString(getActivity(),"lastSendTime", ""));

        content_editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                modifyContentIntoLength();

                if (tg.equals("new")) {
                    text_count_textView.setText(getRemainContentLength() + "");
                }
                if (tg.equals("relay")) {
                    text_count_textView.setText(getRemainContentLength() + "");
                }
            }
        });

        // sms temp
        view.findViewById(R.id.sms_temp_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] items = getSmsTempList();
                final String[] showItems = new String[items.length];
                // 显示序号
                if (Constant.SHOW_NUMBER_MSG_TEMP_LIST) {
                    for (int i = 0; i < showItems.length; i++) {
                        showItems[i] = (i + 1) + ". " + items[i];
                    }
                }

                new QMUIDialog.CheckableDialogBuilder(getActivity())
                    .addItems(showItems, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            content_editText.setText(items[which]);
                            dialog.dismiss();
                        }
                    })
                    .show();
            }
        });


        // friend
        view.findViewById(R.id.friend_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] builtInFriendNames = mainActivity.getResources().getStringArray(R.array.friendName);
                final String[] builtInFriendNumbers = mainActivity.getResources().getStringArray(R.array.friendNumber);

                final RealmResults<Friend> friends = realm.where(Friend.class).findAll();

                // 显示序号
                final String[] showItems = new String[builtInFriendNames.length + friends.size()];
                for (int i = 0; i < showItems.length; i++) {
                    if (i < friends.size()) {
                        showItems[i] = (i + 1) + ". " + friends.get(i).getName() + "(" + friends.get(i).getNumber() + ")";
                    } else {
                        showItems[i] = (i + 1) + ". " + builtInFriendNames[i - friends.size()] + "(" + builtInFriendNumbers[i - friends.size()] + ")";

                    }
                }

                new QMUIDialog.CheckableDialogBuilder(getActivity())
                        .addItems(showItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                receiver_editText.setText(which < friends.size() ? friends.get(which).getNumber() : builtInFriendNumbers[which - friends.size()]);
                                dialog.dismiss();
                            }
                        })
                        .show();
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

    private int getRemainContentLength() {
        try {
            int length = Constant.MESSAGE_CONTENT_MAX_LENGTH - content_editText.getText().toString().getBytes("GB2312").length;
            return length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return Constant.MESSAGE_CONTENT_MAX_LENGTH;
        }
    }

    private int getCurrentContentLength() {
        try {
            return content_editText.getText().toString().getBytes("GB2312").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private String[] getSmsTempList() {
        final String[] items = getActivity().getResources().getStringArray(R.array.smsTemplate);
        String smsTempStr = PreferencesUtils.getString(getActivity(), "smsTemplate", "");
        if (smsTempStr.isEmpty()) {
            return items;
        }

        for (String s : items) {
            smsTempStr += getString(R.string.smsTemplateSeparate);
            smsTempStr += s;
        }
        return  smsTempStr.split(getString(R.string.smsTemplateSeparate));
    }

    private void modifyContentIntoLength() {
        String content = content_editText.getText().toString();
        if (getCurrentContentLength() > Constant.MESSAGE_CONTENT_MAX_LENGTH) {
            content_editText.setText(content.subSequence(0, content.length() - 1));
            content_editText.setSelection(content_editText.getText().toString().length());
            modifyContentIntoLength();
        }
    }
}
