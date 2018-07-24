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
import com.cetcme.xkterminal.Sqlite.Bean.MessageBean;
import com.cetcme.xkterminal.Sqlite.Proxy.FriendProxy;
import com.cetcme.xkterminal.Sqlite.Proxy.InoutProxy;
import com.cetcme.xkterminal.Sqlite.Proxy.MessageProxy;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.xutils.DbManager;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import java.util.ArrayList;

/**
 * Created by qiuhong on 10/01/2018.
 */

public class MessageDetailBar extends RelativeLayout implements View.OnClickListener {

    public MainActivity mainActivity;

    private Button btn_relay;
    private Button btn_reply_resend;
    private Button btn_delete;

    public Button button_back;

    private ArrayList<Button> buttons = new ArrayList<>();
    private String status;

    public MessageDetailBar(Context context) {
        super(context);
    }

    public MessageDetailBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.bar_message_detail_view, this, true);

        bindView(view);

    }

    private void bindView(View view) {
        btn_relay        = view.findViewById(R.id.btn_relay);
        btn_reply_resend = view.findViewById(R.id.btn_reply_resend);
        btn_delete       = view.findViewById(R.id.btn_delete);

        button_back      = view.findViewById(R.id.button_back);

        buttons.add(btn_relay);
        buttons.add(btn_reply_resend);
        buttons.add(btn_delete);
        buttons.add(button_back);

        for (Button button: buttons) {
            button.setTextColor(0xFFFFFFFF);
            button.setBackgroundResource(R.drawable.button_bg_selector);
            button.setOnClickListener(this);
        }

    }

    public void setStatus(String status) {
        this.status = status;
        if (status.equals("receive")) {
            btn_reply_resend.setText("回复");
        } else {
            btn_reply_resend.setText("重发");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_relay:
                if (!checkCountLimit()) {
                    mainActivity.messageNewFragment.setTg("relay");
                    mainActivity.showSendBar();
                }
                break;
            case R.id.btn_reply_resend:
                if (!checkCountLimit()) {
                    if (status.equals("receive")) {
                        // 回复
                        mainActivity.messageNewFragment.setTg("reply");
                    } else {
                        // 重发
                        mainActivity.messageNewFragment.setTg("resend");
                    }
                    mainActivity.showSendBar();
                }
                break;
            case R.id.btn_delete:
                showDeleteDialog(mainActivity.messageId);
                break;
            case R.id.button_back:
                mainActivity.backToMessageFragment();
                break;
            default:
                break;
        }
    }

    private void showDeleteDialog(final int id) {
        new QMUIDialog.MessageDialogBuilder(mainActivity)
                .setTitle("提示")
                .setMessage("确定要删除吗？")
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(0, "删除", QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        DbManager db = MyApplication.getInstance().getDb();
                        try {
                            db.delete(MessageBean.class, WhereBuilder.b("id", "=", id));
                            Toast.makeText(mainActivity, "删除成功", Toast.LENGTH_SHORT).show();
                        } catch (DbException e) {
                            e.printStackTrace();
                        }
                        mainActivity.messageFragment.deleteMessage(mainActivity.messageIndex);
                        mainActivity.backToMessageFragment();
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private boolean checkCountLimit() {
        long count = MessageProxy.getCount(MyApplication.getInstance().getDb());
        if (count >= Constant.LIMIT_MESSAGE) {
            new QMUIDialog.MessageDialogBuilder(mainActivity)
                    .setTitle("提示")
                    .setMessage("已达到短信最大数量(" + Constant.LIMIT_MESSAGE + ")，请删除后再进行操作。")
                    .addAction("确定", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
        return count >= Constant.LIMIT_MESSAGE;
    }
}
