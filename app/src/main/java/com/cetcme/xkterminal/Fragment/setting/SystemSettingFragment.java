package com.cetcme.xkterminal.Fragment.setting;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cetcme.xkterminal.DataFormat.IDFormat;
import com.cetcme.xkterminal.DataFormat.MessageFormat;
import com.cetcme.xkterminal.DataFormat.Util.DateUtil;
import com.cetcme.xkterminal.Event.SmsEvent;
import com.cetcme.xkterminal.MainActivity;
import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.MyClass.AdminPswUtil;
import com.cetcme.xkterminal.MyClass.CommonUtil;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.DensityUtil;
import com.cetcme.xkterminal.MyClass.PreferencesUtils;
import com.cetcme.xkterminal.MyClass.widget.PswDialog;
import com.cetcme.xkterminal.Navigation.SkiaDrawView;
import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.SelfCheckActivity;
import com.cetcme.xkterminal.Sqlite.Bean.FriendBean;
import com.cetcme.xkterminal.Sqlite.Bean.GroupBean;
import com.cetcme.xkterminal.Sqlite.Proxy.FriendProxy;
import com.cetcme.xkterminal.Sqlite.Proxy.GroupProxy;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;
import org.xutils.DbManager;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.content.Context.MODE_PRIVATE;
import static com.cetcme.xkterminal.MyClass.CommonUtil.isNumber;
import static com.cetcme.xkterminal.Navigation.Constant.YIMA_WORK_PATH;

/**
 * A simple {@link Fragment} subclass.
 */
public class SystemSettingFragment extends Fragment {

    private TextView wifi_ssid_textView;
    private TextView tv_yima_serial;

    private TextView time_zone_textView;
    private Button time_zone_minus_btn;
    private Button time_zone_add_btn;

    @BindView(R.id.tv_rdss)
    TextView tv_rdss;

    @BindView(R.id.tv_self_test)
    TextView tv_self_test;

    @BindView(R.id.tv_device_id)
    TextView tv_device_id;

    @BindView(R.id.tv_yima_id)
    TextView tv_yima_id;

    // 用于添加好友内容缓存
    private String[] friend = {"", ""};

    // 用于添加分组内容缓存
    private String[] group = {"", ""};

    private MainActivity mainActivity;
    private DbManager db = MyApplication.getInstance().getDb();
    private QMUITipDialog tipDialog;

    Unbinder unbinder;

    public SystemSettingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_setting_system, container, false);
        unbinder = ButterKnife.bind(this, view);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        initView(view);
        getData();
        mainActivity = (MainActivity) getActivity();

        MyApplication.getInstance().sendBytes(IDFormat.getID());
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        unbinder.unbind();
    }

    private void initView(View view) {

        wifi_ssid_textView = view.findViewById(R.id.wifi_ssid_textView);

        wifi_ssid_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PswDialog pswDialog = new PswDialog(getActivity());
                pswDialog.setOnPswOkListener(new PswDialog.OnPswOkListener() {
                    @Override
                    public void onPswOk() {
                        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getActivity());
                        builder.setTitle("修改WIFI SSID")
                                .setPlaceholder("在此输入新的WIFI SSID")
                                .setInputType(InputType.TYPE_CLASS_TEXT)
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
                                        if (text != null && text.length() > 0) {
                                            PreferencesUtils.putString(getActivity(), "wifiSSID", text.toString());
                                            wifi_ssid_textView.setText(text);
                                            Toast.makeText(getActivity(), "新的WIFI SSID: " + text, Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                            assert getActivity() != null;
                                            ((MainActivity) getActivity()).createWifiHotspot();
                                        } else {
                                            Toast.makeText(getActivity(), "请输入新的WIFI SSID", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .show();
                    }
                });
                pswDialog.show();
            }
        });

        time_zone_textView = view.findViewById(R.id.time_zone_textView);

        time_zone_minus_btn = view.findViewById(R.id.time_zone_minus_btn);
        time_zone_add_btn = view.findViewById(R.id.time_zone_add_btn);

        int originalTimeZone = PreferencesUtils.getInt(getActivity(), "time_zone");
        if (originalTimeZone == -1) originalTimeZone = Constant.TIME_ZONE;

        modifyTimeZoneBtn(originalTimeZone);
        final int timeZone = originalTimeZone - 12;
        time_zone_textView.setText(timeZone > 0 ? " + " + timeZone : timeZone + "");

        time_zone_minus_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modifyTimeZone(false);
            }
        });
        time_zone_add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modifyTimeZone(true);
            }
        });

        // 短信模版
        view.findViewById(R.id.temp_add_textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                smsTempAdd();
            }
        });

        view.findViewById(R.id.temp_delete_textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                smsTempDelete();
            }
        });

        // 通讯录
        view.findViewById(R.id.friend_add_textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                friendAdd();
            }
        });

        view.findViewById(R.id.friend_delete_textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                friendDelete();
            }
        });

        /*
        // 用户分组
        view.findViewById(R.id.group_add_textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupAdd();
            }
        });

        view.findViewById(R.id.group_delete_textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupDelete();
            }
        });
        */
        //本船信息
        view.findViewById(R.id.own_ship_textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SharedPreferences sp = getActivity().getSharedPreferences("xkTerminal", MODE_PRIVATE);
                final View contentView = getLayoutInflater().inflate(R.layout.dialog_own_ship_info, null);
                final EditText mEt1 = contentView.findViewById(R.id.et_ship_name);
                final EditText mEt2 = contentView.findViewById(R.id.et_ship_no);
                final EditText mEt3 = contentView.findViewById(R.id.et_ship_length);
                final EditText mEt4 = contentView.findViewById(R.id.et_ship_deep);
                mEt1.setText(sp.getString("shipName", ""));
                mEt2.setText(sp.getString("shipNo", ""));
                mEt3.setText(sp.getString("shipLength", ""));
                mEt4.setText(sp.getString("shipDeep", ""));
                new AlertDialog.Builder(getActivity())
                        .setView(contentView)
                        .setCancelable(false)
                        .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String shipNo = mEt2.getText().toString().trim();
                                Pattern pattern = Pattern.compile("^\\d+$");
                                Matcher matcher = pattern.matcher(shipNo);
                                if (!matcher.find()) {
                                    Toast.makeText(getActivity(), "MMSI必须为数字编号", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                boolean success = sp.edit()
                                        .putString("shipName", mEt1.getText().toString().trim())
                                        .putString("shipNo", shipNo)
                                        .putString("shipLength", mEt3.getText().toString().trim())
                                        .putString("shipDeep", mEt4.getText().toString().trim())
                                        .commit();
                                if (success) {
                                    Toast.makeText(getActivity(), "保存成功", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), "保存失败", Toast.LENGTH_SHORT).show();
                                }
                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });
        tv_rdss.setText(PreferencesUtils.getString(getActivity(), "server_address", Constant.SERVER_BD_NUMBER));
        tv_rdss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                final boolean rdssOpen = PreferencesUtils.getBoolean(getActivity(), "rdss", false);
//
//                tipDialog = new QMUITipDialog.Builder(getContext())
//                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
//                        .setTipWord("发送中")
//                        .create();
//                tipDialog.show();
//
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        PreferencesUtils.putBoolean(getActivity(), "rdss", !rdssOpen);
//                        tv_rdss.setText("点击发送");
//                        tipDialog.dismiss();
//                    }
//                }, 3000);
                PswDialog pswDialog = new PswDialog(getActivity());
                pswDialog.setOnPswOkListener(new PswDialog.OnPswOkListener() {
                    @Override
                    public void onPswOk() {
                        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getActivity());
                        builder.setTitle("设置平台地址")
                                .setPlaceholder("请输入新的平台地址")
                                .setInputType(InputType.TYPE_CLASS_NUMBER)
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
                                            if (isNumber(text.toString())) {
                                                // 发送更改id的bytes
//                                                MyApplication.getInstance().sendBytes(IDFormat.format(text.toString()));
                                                PreferencesUtils.putString(getActivity(), "server_address", text.toString().trim());
                                                dialog.dismiss();
                                                tv_rdss.setText(PreferencesUtils.getString(getActivity(), "server_address", Constant.SERVER_BD_NUMBER));
                                            } else {
                                                Toast.makeText(getActivity(), "请输入正确的平台地址", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(getActivity(), "请填入内容", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .show();
                    }
                });
                pswDialog.show();
            }
        });

        if (PreferencesUtils.getBoolean(getActivity(), "self_check_result", false)) {
            tv_self_test.setText("开始自检(成功)");
        } else {
            tv_self_test.setText("开始自检");
        }

        tv_self_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_self_test.setText("开始自检");
               /* PreferencesUtils.putString(getActivity(), "lastSendTime", DateUtil.parseDateToString(Constant.SYSTEM_DATE, DateUtil.DatePattern.YYYYMMDDHHMMSS));
                tipDialog = new QMUITipDialog.Builder(getContext())
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                        .setTipWord("自检中")
                        .create();
                tipDialog.show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tipDialog.dismiss();
                        tipDialog = new QMUITipDialog.Builder(getContext())
                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                .setTipWord("自检成功")
                                .create();
                        tipDialog.show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tipDialog.dismiss();
                            }
                        }, 1500);
                    }
                }, 15000);*/

               startActivity(new Intent(getActivity(), SelfCheckActivity.class));

            }
        });

        tv_device_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PswDialog pswDialog = new PswDialog(getActivity());
                pswDialog.setOnPswOkListener(new PswDialog.OnPswOkListener() {
                    @Override
                    public void onPswOk() {
                        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getActivity());
                        builder.setTitle("设置终端ID")
                                .setPlaceholder("请输入新的终端ID")
                                .setInputType(InputType.TYPE_CLASS_NUMBER)
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
                                            if (isNumber(text.toString())) {
                                                // 发送更改id的bytes
                                                MyApplication.getInstance().sendBytes(IDFormat.format(text.toString()));
                                                dialog.dismiss();
                                            } else {
                                                Toast.makeText(getActivity(), "请输入正确的终端ID", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(getActivity(), "请填入内容", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .show();
                    }
                });
                pswDialog.show();
            }
        });

        view.findViewById(R.id.btn_finish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PswDialog pswDialog = new PswDialog(getActivity());
                pswDialog.setOnPswOkListener(new PswDialog.OnPswOkListener() {
                    @Override
                    public void onPswOk() {
                        System.exit(0);
                    }
                });
                pswDialog.show();
            }
        });

        tv_yima_serial = view.findViewById(R.id.tv_yima_serial);
        tv_yima_serial.setText(PreferencesUtils.getString(getActivity(), "yimaSerial"));

        tv_yima_serial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PswDialog pswDialog = new PswDialog(getActivity());
                pswDialog.setOnPswOkListener(new PswDialog.OnPswOkListener() {
                    @Override
                    public void onPswOk() {

                        String lastSendTime = PreferencesUtils.getString(getActivity(), "lastSendTime", "");
                        if (!lastSendTime.isEmpty()) {
                            Long sendDate = DateUtil.parseStringToDate(lastSendTime, DateUtil.DatePattern.YYYYMMDDHHMMSS).getTime();
                            Long now = Constant.SYSTEM_DATE.getTime();
                            if (now - sendDate <= Constant.MESSAGE_SEND_LIMIT_TIME && now - sendDate > 0) {
                                long remainSecond = (Constant.MESSAGE_SEND_LIMIT_TIME - (now - sendDate)) / 1000;
                                Toast.makeText(getActivity(), "发送时间间隔不到1分钟，请等待" + remainSecond + "秒", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        MainActivity.sendCheckAndMapMessage();
                        Toast.makeText(getActivity(), "已发送注册短信，请等待", Toast.LENGTH_SHORT).show();

                        // TODO: test
                        /*
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 注册海图
                                PreferencesUtils.putString(MyApplication.getInstance().mainActivity, "yimaSerial", "8C58-708D-1865-9674");
                                // 设置完成后提示 重新进入app
                                new QMUIDialog.MessageDialogBuilder(MyApplication.getInstance().mainActivity)
                                        .setTitle("提示")
                                        .setMessage("海图序列号设置成功，请重新打开app")
                                        .addAction("确定", new QMUIDialogAction.ActionListener() {
                                            @Override
                                            public void onClick(QMUIDialog dialog, int index) {
                                                System.exit(0);
                                            }
                                        })
                                        .show();
                            }
                        }, 5000);
                        */
                    }
                });
                pswDialog.show();

                /*
                PswDialog pswDialog = new PswDialog(getActivity());
                pswDialog.setOnPswOkListener(new PswDialog.OnPswOkListener() {
                    @Override
                    public void onPswOk() {
                        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getActivity());
                        builder.setTitle("设置海图序列号")
                                .setPlaceholder("请输入新的海图序列号")
                                .setInputType(InputType.TYPE_CLASS_TEXT)
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
                                            PreferencesUtils.putString(getActivity(), "yimaSerial", text.toString());
                                            dialog.dismiss();
                                            // 设置完成后提示 重新进入app
                                            new QMUIDialog.MessageDialogBuilder(getActivity())
                                                    .setTitle("提示")
                                                    .setMessage("海图序列号设置成功，请重新打开app")
                                                    .addAction("确定", new QMUIDialogAction.ActionListener() {
                                                        @Override
                                                        public void onClick(QMUIDialog dialog, int index) {
                                                            System.exit(0);
                                                        }
                                                    })
                                                    .show();
                                        } else {
                                            Toast.makeText(getActivity(), "请填入内容", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .show();
                    }
                });
                pswDialog.show();
                */
            }
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SmsEvent smsEvent) {
        try {
            JSONObject receiveJson = smsEvent.getReceiveJson();
            String type = receiveJson.getString("apiType");
            switch (type) {
                case "device_id":
                    String deviceId = receiveJson.getString("deviceID");
                    tv_device_id.setText(deviceId);

                    String savedDeviceId = PreferencesUtils.getString(getActivity(), "deviceID", "");
                    if (!deviceId.equals(savedDeviceId)) {
                        PreferencesUtils.putString(getActivity(), "deviceID", deviceId);
                        // 更改device_id后 更改wifi ssid
                        String newSSID = "北斗" + deviceId;
                        PreferencesUtils.putString(getActivity(), "wifiSSID", newSSID);
                        MyApplication.getInstance().mainActivity.createWifiHotspot();
                        wifi_ssid_textView.setText(newSSID);
                    }
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String str) {
        switch (str) {
            case "check_ok":
                tv_self_test.setText("自检成功");
                PreferencesUtils.putBoolean(getActivity(), "self_check_result", true);
                break;
            case "check_fail":
                tv_self_test.setText("自检失败");
                PreferencesUtils.putBoolean(getActivity(), "self_check_result", false);
                break;
        }
    }

    private void getData() {

        String ssid = PreferencesUtils.getString(getActivity(), "wifiSSID");
        if (ssid != null) {
            wifi_ssid_textView.setText(ssid);
        } else {
            wifi_ssid_textView.setText(getString(R.string.wifi_ssid));
        }

//        boolean rdssOpen = PreferencesUtils.getBoolean(getActivity(), "rdss", false);
//        tv_rdss.setText(rdssOpen ? "关闭" : "开启");

        tv_yima_id.setText(getYimaId());
        tv_yima_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qecodeDialogShow(getYimaId());
            }
        });
    }

    private void smsTempAdd() {
        long count;
        String smsTempStr = PreferencesUtils.getString(getActivity(), "smsTemplate", "");
        if (smsTempStr.isEmpty()) {
            count = 0;
        } else {
            String[] items = smsTempStr.split(getString(R.string.smsTemplateSeparate));
            count = items.length;
        }

        if (count >= Constant.LIMIT_SMS_TEMP) {
            new QMUIDialog.MessageDialogBuilder(getActivity())
                    .setTitle("提示")
                    .setMessage("已达到短信模版最大数量(" + Constant.LIMIT_SMS_TEMP + ")，请删除后再添加。")
                    .addAction("确定", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                        }
                    })
                    .show();
            return;
        }

        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getActivity());
        builder.setTitle("添加短信模版")
                .setPlaceholder("在此输入短信模版内容")
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("添加", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        CharSequence text = builder.getEditText().getText();
                        if (text != null && text.length() > 0) {

                            int contentLength = 0;
                            try {
                                contentLength = text.toString().getBytes("GBK").length;
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            if (contentLength > Constant.MESSAGE_CONTENT_MAX_LENGTH) {
                                Toast.makeText(getActivity(), "内容长度(" + contentLength + ")超出最大限制(" + Constant.MESSAGE_CONTENT_MAX_LENGTH + ")", Toast.LENGTH_SHORT).show();
                            } else {
                                String smsTempStr = PreferencesUtils.getString(getActivity(), "smsTemplate", "");
                                if (!smsTempStr.isEmpty()) {
                                    smsTempStr += getString(R.string.smsTemplateSeparate);
                                }
                                smsTempStr += text.toString();
                                PreferencesUtils.putString(getActivity(), "smsTemplate", smsTempStr);
                                Toast.makeText(getActivity(), "短信模版添加成功", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }


                        } else {
                            Toast.makeText(getActivity(), "请填入内容", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    private void smsTempDelete() {
        String smsTempStr = PreferencesUtils.getString(getActivity(), "smsTemplate", "");
        if (smsTempStr.isEmpty()) {
            Toast.makeText(getActivity(), "自定义短信模版为空", Toast.LENGTH_SHORT).show();
            return;
        }
        final String[] items = smsTempStr.split(getString(R.string.smsTemplateSeparate));
        final String[] showItems = new String[items.length];
        // 显示序号
        if (Constant.SHOW_NUMBER_MSG_TEMP_LIST) {
            for (int i = 0; i < showItems.length; i++) {
                showItems[i] = (i + 1) + ". " + items[i];
            }
        }
        final QMUIDialog.MultiCheckableDialogBuilder builder = new QMUIDialog.MultiCheckableDialogBuilder(getActivity())
                .addItems(showItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.addAction("取消", new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                dialog.dismiss();
            }
        });
        builder.addAction("删除", new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                int[] indexes = builder.getCheckedItemIndexes();
                String newSmsTempStr = "";
                for (int i = 0; i < items.length; i++) {
                    if (!CommonUtil.useLoop(indexes, i)) {
                        if (!newSmsTempStr.isEmpty()) {
                            newSmsTempStr += getString(R.string.smsTemplateSeparate);
                        }
                        newSmsTempStr += items[i];
                    }
                }
                PreferencesUtils.putString(getActivity(), "smsTemplate", newSmsTempStr);
                Toast.makeText(getActivity(), "短信模版删除成功", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void friendAdd() {

        long count = FriendProxy.getCount(MyApplication.getInstance().getDb());

        if (count >= Constant.LIMIT_FRIEND) {
            new QMUIDialog.MessageDialogBuilder(getActivity())
                    .setTitle("提示")
                    .setMessage("已达到好友最大数量(" + Constant.LIMIT_FRIEND + ")，请删除后再添加。")
                    .addAction("确定", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                        }
                    })
                    .show();
            return;
        }

        final QMUIDialog.EditTextDialogBuilder numberBuilder = new QMUIDialog.EditTextDialogBuilder(getActivity());
        numberBuilder.setTitle("添加好友")
                .setPlaceholder("在此输入好友号码")
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("确认", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        CharSequence text = numberBuilder.getEditText().getText();
                        if (text != null && text.length() > 0) {
                            if (text.length() > 12) {
                                Toast.makeText(getActivity(), "超过多大长度12", Toast.LENGTH_SHORT).show();
                            } else {

                                if (isNumber(text.toString())) {
                                    friend[1] = text.toString();
                                    mainActivity.addFriend(friend[0], friend[1]);
                                    friend[0] = "";
                                    friend[1] = "";
                                    dialog.dismiss();
                                    Toast.makeText(getActivity(), "好友添加成功", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), "请填入正确号码", Toast.LENGTH_SHORT).show();
                                }

                            }
                        } else {
                            Toast.makeText(getActivity(), "请填入号码", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        final QMUIDialog.EditTextDialogBuilder nameBuilder = new QMUIDialog.EditTextDialogBuilder(getActivity());
        nameBuilder.setTitle("添加好友")
                .setPlaceholder("在此输入好友昵称")
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("确认", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        CharSequence text = nameBuilder.getEditText().getText();
                        if (text != null && text.length() > 0) {

                            friend[0] = text.toString();
                            numberBuilder.show();
                            dialog.dismiss();

                        } else {
                            Toast.makeText(getActivity(), "请填入姓名", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();

    }

    private void friendDelete() {

        final List<FriendBean> friends = FriendProxy.getAll(db);
        if (friends == null || friends.size() == 0) {
            Toast.makeText(getActivity(), "好友列表为空", Toast.LENGTH_SHORT).show();
            return;
        }

        final String[] items = new String[friends.size()];
        for (int i = 0; i < friends.size(); i++) {
            items[i] = (i + 1) + ". " + friends.get(i).getName() + "(" + friends.get(i).getNumber() + ")";
        }
        final QMUIDialog.MultiCheckableDialogBuilder builder = new QMUIDialog.MultiCheckableDialogBuilder(getActivity())
                .addItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.addAction("取消", new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                dialog.dismiss();
            }
        });
        builder.addAction("删除", new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                int[] indexes = builder.getCheckedItemIndexes();
                for (int i = 0; i < indexes.length; i++) {
                    FriendProxy.deleteById(db, friends.get(i).getId());
                }
                Toast.makeText(getActivity(), "好友删除成功", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void groupAdd() {

        final QMUIDialog.EditTextDialogBuilder numberBuilder = new QMUIDialog.EditTextDialogBuilder(getActivity());
        numberBuilder.setTitle("添加用户分组")
                .setPlaceholder("在此输入分组编号(1到255)")
                .setInputType(InputType.TYPE_CLASS_NUMBER)
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("确认", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        CharSequence text = numberBuilder.getEditText().getText();
                        if (text != null && text.length() > 0 && text.length() <= 3 && isNumber(text.toString())) {
                            int number = Integer.parseInt(text.toString());
                            if (number < 1 || number > 255) {
                                Toast.makeText(getActivity(), "分组编号超出范围(1到255)", Toast.LENGTH_SHORT).show();
                            } else {
                                group[1] = text.toString();
                                mainActivity.addGroup(group[0], group[1]);
                                group[0] = "";
                                group[1] = "";
                                dialog.dismiss();
                                Toast.makeText(getActivity(), "分组添加成功", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), "请填入号码", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        final QMUIDialog.EditTextDialogBuilder nameBuilder = new QMUIDialog.EditTextDialogBuilder(getActivity());
        nameBuilder.setTitle("添加分组")
                .setPlaceholder("在此输入自定义分组名称")
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("确认", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        CharSequence text = nameBuilder.getEditText().getText();
                        if (text != null && text.length() > 0) {

                            group[0] = text.toString();
                            numberBuilder.show();
                            dialog.dismiss();

                        } else {
                            Toast.makeText(getActivity(), "请填入自定义分组名称", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();

    }

    private void groupDelete() {

        final List<GroupBean> groups = GroupProxy.getAll(db);
        if (groups == null || groups.size() == 0) {
            Toast.makeText(getActivity(), "分组列表为空", Toast.LENGTH_SHORT).show();
            return;
        }

        final String[] items = new String[groups.size()];
        for (int i = 0; i < groups.size(); i++) {
            items[i] = (i + 1) + ". " + groups.get(i).getName() + "(" + groups.get(i).getNumber() + ")";
        }
        final QMUIDialog.MultiCheckableDialogBuilder builder = new QMUIDialog.MultiCheckableDialogBuilder(getActivity())
                .addItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.addAction("取消", new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                dialog.dismiss();
            }
        });
        builder.addAction("删除", new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                int[] indexes = builder.getCheckedItemIndexes();
                for (int i = 0; i < indexes.length; i++) {
                    GroupProxy.deleteById(db, groups.get(i).getId());
                }
                Toast.makeText(getActivity(), "分组删除成功", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void modifyTimeZoneBtn(int zone) {
        if (zone == 0) {
            time_zone_minus_btn.setEnabled(false);
        } else if (zone == 24) {
            time_zone_add_btn.setEnabled(false);
        } else {
            time_zone_minus_btn.setEnabled(true);
            time_zone_add_btn.setEnabled(true);
        }
    }

    private void modifyTimeZone(boolean add) {
        int zone = PreferencesUtils.getInt(getActivity(), "time_zone");
        if (zone == -1) zone = Constant.TIME_ZONE;
        int del = 0;
        if (add && zone != 24) {
            // 加一个时区
            del = 1;
        }

        if (!add && zone != 0) {
            // 减一个时区
            del = -1;
        }
        if (del != 0) {
            int newZone = zone + del;
            modifyTimeZoneBtn(newZone);
            PreferencesUtils.putInt(getActivity(), "time_zone", newZone);

            // 修正时间
            long rightTime = Constant.SYSTEM_DATE.getTime() + del * 3600 * 1000;
            Date rightDate = new Date(rightTime);
            Constant.SYSTEM_DATE = rightDate;

            time_zone_textView.setText(newZone - 12 > 0 ? "+" + (newZone - 12) : "" + (newZone - 12));
        }

    }

    private boolean isNumber(String txt) {
        Pattern p = Pattern.compile("[0-9]*");
        Matcher m = p.matcher(txt);
        return m.matches();
    }

    private String getYimaId() {
        return SkiaDrawView.mYimaLib.GetDeviceIDForLicSvr();
    }

    private int getUserId() {
        return SkiaDrawView.mYimaLib.GetUsrID();
    }

    private void qecodeDialogShow(String serial) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        LayoutInflater inflater = LayoutInflater.from(getActivity());
//        View v = inflater.inflate(R.layout.dialog_qrcode, null);
//        ImageView iv_qrcode = v.findViewById(R.id.iv_qrcode);
        int width = DensityUtil.dip2px(getActivity(), 200);
        ImageView iv_qrcode = new ImageView(getActivity());
//        iv_qrcode.setMinimumWidth(width);
//        iv_qrcode.setMinimumHeight(width);
        iv_qrcode.setImageBitmap(createQRImage(serial, width, width));
        //builer.setView(v);//这里如果使用builer.setView(v)，自定义布局只会覆盖title和button之间的那部分
        final Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(iv_qrcode);//自定义布局应该在这里添加，要在dialog.show()的后面
        dialog.getWindow().setGravity(Gravity.CENTER);//可以设置显示的位置
    }

    //要转换的地址或字符串,可以是中文
    public static Bitmap createQRImage(String url, int QR_WIDTH, int QR_HEIGHT ) {
        try {//判断URL合法性
            if (url == null || "".equals(url) || url.length() < 1) {
                return null;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = null;
                bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
            //下面这里按照二维码的算法，逐个生成二维码的图片，
            //两个for循环是图片横列扫描的结果
            for (int y = 0; y < QR_HEIGHT; y++) {
                for (int x = 0; x < QR_WIDTH; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * QR_WIDTH + x] = 0xff000000;
                    }
                    else {
                        pixels[y * QR_HEIGHT + x] = 0xffffffff;
                    }
                }
            }//生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
            //显示到一个ImageView上面
            return bitmap;
        }
        catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

}
