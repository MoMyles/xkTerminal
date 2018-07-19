package com.cetcme.xkterminal.ActionBar;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cetcme.xkterminal.MainActivity;
import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.PreferencesUtils;
import com.cetcme.xkterminal.NewInoutActivity;
import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.Sqlite.Proxy.FriendProxy;
import com.cetcme.xkterminal.Sqlite.Proxy.InoutProxy;
import com.cetcme.xkterminal.Sqlite.Proxy.MessageProxy;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import java.util.ArrayList;

/**
 * Created by qiuhong on 10/01/2018.
 */

public class MessageBar extends RelativeLayout implements View.OnClickListener {

    public MainActivity mainActivity;

    private Button button_new;
    private Button button_detail;
    private Button button_relay;
    private Button button_prev;
    private Button button_next;
    public Button button_back;

    public boolean isInout = false;

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
        button_prev     = view.findViewById(R.id.button_prev);
        button_next     = view.findViewById(R.id.button_next);
        button_back     = view.findViewById(R.id.button_back);

        buttons.add(button_new);
        buttons.add(button_detail);
        buttons.add(button_relay);
        buttons.add(button_prev);
        buttons.add(button_next);
        buttons.add(button_back);

        button_detail.setVisibility(GONE);
        button_relay.setVisibility(GONE);

        for (Button button: buttons) {
            button.setTextColor(0xFFFFFFFF);
            button.setBackgroundResource(R.drawable.button_bg_selector);
            button.setOnClickListener(this);
//            button.setTextSize(10); //16
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_new:
                if (isInout) {
                    long count = InoutProxy.getCount(MyApplication.getInstance().getDb());
                    if (count >= Constant.LIMIT_INOUT) {
                        new QMUIDialog.MessageDialogBuilder(mainActivity)
                                .setTitle("提示")
                                .setMessage("已达到申报最大数量(" + Constant.LIMIT_INOUT+ ")，请删除后再申报。")
                                .addAction("确定", new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                        return;
                    }
                } else {
                    long count = MessageProxy.getCount(MyApplication.getInstance().getDb());
                    if (count >= Constant.LIMIT_MESSAGE) {
                        new QMUIDialog.MessageDialogBuilder(mainActivity)
                                .setTitle("提示")
                                .setMessage("已达到短信最大数量(" + Constant.LIMIT_MESSAGE + ")，请删除后再发件。")
                                .addAction("确定", new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                        return;
                    }
                }

                if (isInout) {
                    mainActivity.startActivity(new Intent(mainActivity, NewInoutActivity.class));
                } else {
                    boolean canSendSms = PreferencesUtils.getBoolean(mainActivity, "canSendSms", true);
                    if (canSendSms) {
                        mainActivity.initNewFragment("new");
                    } else {
                        Toast.makeText(mainActivity, "短信发送功能已关闭", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.button_detail:
                mainActivity.initNewFragment("detail");
                break;
            case R.id.button_relay:
                mainActivity.initNewFragment("relay");
                break;
            case R.id.button_prev:
                mainActivity.prevPage();
                break;
            case R.id.button_next:
                mainActivity.nextPage();
                break;
            case R.id.button_back:
                mainActivity.initMainFragment();
                isInout = false;
                break;
            default:
                break;
        }
    }

    public void setNextButtonEnable(boolean enable) {
        button_next.setEnabled(enable);
        if (enable) {
            button_next.setTextColor(0xFFFFFFFF);
        } else {
            button_next.setTextColor(0xFF1D274B);
        }
    }

    public void setPrevButtonEnable(boolean enable) {
        button_prev.setEnabled(enable);
        if (enable) {
            button_prev.setTextColor(0xFFFFFFFF);
        } else {
            button_prev.setTextColor(0xFF1D274B);
        }
    }

    public void setDetailAndRelayButtonEnable(boolean enable) {
        button_detail.setEnabled(enable);
        button_relay.setEnabled(enable);
        if (enable) {
            button_detail.setTextColor(0xFFFFFFFF);
            button_relay.setTextColor(0xFFFFFFFF);
        } else {
            button_detail.setTextColor(0xFF1D274B);
            button_relay.setTextColor(0xFF1D274B);
        }
    }

    public void setNewBtnVisible(boolean visible) {
        button_new.setVisibility(visible ? VISIBLE : GONE);
    }

}
