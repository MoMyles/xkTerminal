package com.cetcme.xkterminal.Fragment.setting;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cetcme.xkterminal.DataFormat.IDFormat;
import com.cetcme.xkterminal.Event.SmsEvent;
import com.cetcme.xkterminal.MainActivity;
import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.MyClass.AdminPswUtil;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.PreferencesUtils;
import com.cetcme.xkterminal.MyClass.widget.PswDialog;
import com.cetcme.xkterminal.R;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.xutils.view.annotation.Event;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class DBSettingFragment extends Fragment {


    @BindView(R.id.tv_address) TextView tv_address;
    @BindView(R.id.tv_voltage) TextView tv_voltage;

    Unbinder unbinder;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_db, container, false);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
//        Log.e("DBSettingFragment", "onCreateView: ");
        unbinder = ButterKnife.bind(this, view);
        initData(view);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        unbinder.unbind();
    }

    private void initData(View view) {
        tv_address.setText(PreferencesUtils.getString(getActivity(), "myNumber"));

        tv_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PswDialog pswDialog = new PswDialog(getActivity());
                pswDialog.setOnPswOkListener(new PswDialog.OnPswOkListener() {
                    @Override
                    public void onPswOk() {
                        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getActivity());
                        builder.setTitle("提示")
                                .setPlaceholder("在此输入您的北斗卡号")
                                .setInputType(InputType.TYPE_CLASS_NUMBER)
                                .addAction("取消", new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index) {
                                        dialog.dismiss();
                                    }
                                })
                                .addAction("确定", new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index) {
                                        CharSequence text = builder.getEditText().getText();
                                        if (text != null && text.length() >= 6 && text.length() < 12) {
                                            Toast.makeText(getActivity(), "北斗卡号修改成功: " + text, Toast.LENGTH_SHORT).show();
                                            ((MainActivity) getActivity()).myNumber = text.toString();
                                            PreferencesUtils.putString(getActivity(), "myNumber", text.toString());
                                            tv_address.setText(text.toString());
                                            dialog.dismiss();
                                        } else {
                                            Toast.makeText(getActivity(), "请填入正确北斗卡号", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .show();
                    }
                });
                pswDialog.show();
            }
        });

        /*
        tv_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final String code = AdminPswUtil.getCode();
                final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getActivity());
                builder.setTitle("请输入管理密码")
                        .setInputType(InputType.TYPE_CLASS_NUMBER)
                        .setPlaceholder(code)
                        .addAction("取消", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                            }
                        })
                        .addAction("确认", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                CharSequence text = builder.getEditText().getText();
                                if (text != null && text.length() > 0) {
                                    if (text.toString().toUpperCase().equals(AdminPswUtil.getAdminPsw(code))) {

                                        dialog.dismiss();

                                        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getActivity());
                                        builder.setTitle("提示")
                                                .setPlaceholder("在此输入您的北斗卡号")
                                                .setInputType(InputType.TYPE_CLASS_NUMBER)
                                                .addAction("取消", new QMUIDialogAction.ActionListener() {
                                                    @Override
                                                    public void onClick(QMUIDialog dialog, int index) {
                                                        dialog.dismiss();
                                                    }
                                                })
                                                .addAction("确定", new QMUIDialogAction.ActionListener() {
                                                    @Override
                                                    public void onClick(QMUIDialog dialog, int index) {
                                                        CharSequence text = builder.getEditText().getText();
                                                        if (text != null && text.length() >= 6 && text.length() < 12) {
                                                            Toast.makeText(getActivity(), "北斗卡号修改成功: " + text, Toast.LENGTH_SHORT).show();
                                                            ((MainActivity) getActivity()).myNumber = text.toString();
                                                            PreferencesUtils.putString(getActivity(), "myNumber", text.toString());
                                                            tv_address.setText(text.toString());
                                                            dialog.dismiss();
                                                        } else {
                                                            Toast.makeText(getActivity(), "请填入正确北斗卡号", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                })
                                                .show();
                                    } else {
                                        Toast.makeText(getActivity(), "管理密码错误", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getActivity(), "请填入内容", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .show();

            }
        });
        */

        tv_voltage.setText(MyApplication.voltage.equals("-") ? "---" : MyApplication.voltage + "V");
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String type) {
        switch (type){
            case "voltage":
                tv_voltage.setText(MyApplication.voltage.equals("-") ? "---" : MyApplication.voltage + "V");
                break;
        }
//        try {
//            String type = smsEvent.getReceiveJson().getString("apiType");
//
//            switch (type) {
//                case "voltage":
//                    double voltage = smsEvent.getReceiveJson().getDouble("voltage");
//                    tv_voltage.setText(String.format("%.2f", voltage));
//                    break;
//            }
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }
}
