package com.cetcme.xkterminal.MyClass.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cetcme.xkterminal.MyClass.AdminPswUtil;
import com.cetcme.xkterminal.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 用法：

 PswDialog pswDialog = new PswDialog(getActivity());
 pswDialog.setOnPswOkListener(new PswDialog.OnPswOkListener() {
    @Override
    public void onPswOk() {

    }
 });
 pswDialog.show();

 */

public class PswDialog extends Dialog implements View.OnClickListener{

    @BindView(R.id.tv_1)
    TextView tv_1;
    @BindView(R.id.tv_2)
    TextView tv_2;
    @BindView(R.id.tv_3)
    TextView tv_3;
    @BindView(R.id.tv_4)
    TextView tv_4;
    @BindView(R.id.tv_5)
    TextView tv_5;
    @BindView(R.id.tv_6)
    TextView tv_6;
    @BindView(R.id.tv_7)
    TextView tv_7;
    @BindView(R.id.tv_8)
    TextView tv_8;
    @BindView(R.id.tv_9)
    TextView tv_9;
    @BindView(R.id.tv_0)
    TextView tv_0;

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.tv_psw)
    TextView tv_psw;
    @BindView(R.id.tv_del)
    TextView tv_del;
    @BindView(R.id.tv_back)
    TextView tv_back;

    private String code;
    private String psw;

    private OnPswOkListener onPswOkListener;
    private Context context;

    public PswDialog(@NonNull Context context) {
        super(context, R.style.myDialogActivityStyle);
        this.context = context;
    }

    public PswDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    protected PswDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_admin_psw);
        ButterKnife.bind(this);
        code = AdminPswUtil.getCode();
        psw = AdminPswUtil.getAdminPsw(code);
        initView();
    }

    private void initView() {
        tv_title.setText("请输入管理密码(" + code + ")");
        tv_psw.setText("");

        tv_0.setOnClickListener(this);
        tv_1.setOnClickListener(this);
        tv_2.setOnClickListener(this);
        tv_3.setOnClickListener(this);
        tv_4.setOnClickListener(this);
        tv_5.setOnClickListener(this);
        tv_6.setOnClickListener(this);
        tv_7.setOnClickListener(this);
        tv_8.setOnClickListener(this);
        tv_9.setOnClickListener(this);
        tv_del.setOnClickListener(this);
        tv_back.setOnClickListener(this);
    }

    public void setOnPswOkListener(OnPswOkListener onPswOkListener) {
        this.onPswOkListener = onPswOkListener;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_0:
                btnClicked(0);
                break;
            case R.id.tv_1:
                btnClicked(1);
                break;
            case R.id.tv_2:
                btnClicked(2);
                break;
            case R.id.tv_3:
                btnClicked(3);
                break;
            case R.id.tv_4:
                btnClicked(4);
                break;
            case R.id.tv_5:
                btnClicked(5);
                break;
            case R.id.tv_6:
                btnClicked(6);
                break;
            case R.id.tv_7:
                btnClicked(7);
                break;
            case R.id.tv_8:
                btnClicked(8);
                break;
            case R.id.tv_9:
                btnClicked(9);
                break;
            case R.id.tv_del:
                btnClicked(-1);
                break;
            case R.id.tv_back:
                this.dismiss();
                break;
        }
    }

    private void btnClicked(int num) {
        String currPsw = tv_psw.getText().toString();

        // 删除
        if (num == -1) {
            if (!currPsw.equals("")) {
                String newPsw = currPsw.substring(0, currPsw.length() - 1);
                tv_psw.setText(newPsw);
                tv_psw.setTextColor(context.getResources().getColor(R.color.check_success));
            }
            return;
        }

        if (currPsw.length() <= 5) {
            String newPsw = currPsw + num;
            tv_psw.setText(newPsw);
            if (newPsw.length() == 6) {
                if (newPsw.equals(psw)) {
                    onPswOkListener.onPswOk();
                    dismiss();
                } else {
                    tv_psw.setTextColor(context.getResources().getColor(R.color.check_fail));
                    Toast.makeText(context, "密码错误", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            tv_psw.setTextColor(context.getResources().getColor(R.color.check_success));
            tv_psw.setText(num + "");
        }
    }

    public interface OnPswOkListener {
        void onPswOk();
    }

}
