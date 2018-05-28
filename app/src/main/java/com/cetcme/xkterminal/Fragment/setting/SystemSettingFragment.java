package com.cetcme.xkterminal.Fragment.setting;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cetcme.xkterminal.MainActivity;
import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.MyClass.CommonUtil;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.PreferencesUtils;
import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.Sqlite.Bean.FriendBean;
import com.cetcme.xkterminal.Sqlite.Bean.GroupBean;
import com.cetcme.xkterminal.Sqlite.Proxy.FriendProxy;
import com.cetcme.xkterminal.Sqlite.Proxy.GroupProxy;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 */
public class SystemSettingFragment extends Fragment {

    private TextView address_textView;
    private TextView signal_textView;
    private TextView location_freq_textView;
    private TextView gps_freq_textView;
    private TextView central_number_textView;

    private TextView location_from_textView;
    private TextView communication_from_textView;
    private TextView signal_power_textView;
    private TextView satellite_count_textView;
    private TextView broad_temp_textView;
    private TextView li_voltage_textView;
    private TextView sun_voltage_textView;

    private TextView wifi_ssid_textView;

    private TextView time_zone_textView;
    private Button time_zone_minus_btn;
    private Button time_zone_add_btn;

    // 用于添加好友内容缓存
    private String[] friend = {"", ""};

    // 用于添加分组内容缓存
    private String[] group = {"", ""};

    private MainActivity mainActivity;
    private DbManager db = MyApplication.getInstance().getDb();


    public SystemSettingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_setting_system,container,false);
        initView(view);
        getData();
        mainActivity = (MainActivity) getActivity();
        return view;
    }

    private void initView(View view) {
        address_textView            = view.findViewById(R.id.address_textView);
        signal_textView             = view.findViewById(R.id.signal_textView);
        location_freq_textView      = view.findViewById(R.id.location_freq_textView);
        gps_freq_textView           = view.findViewById(R.id.gps_freq_textView);
        central_number_textView     = view.findViewById(R.id.central_number_textView);

        location_from_textView      = view.findViewById(R.id.location_from_textView);
        communication_from_textView = view.findViewById(R.id.communication_from_textView);
        signal_power_textView       = view.findViewById(R.id.signal_power_textView);
        satellite_count_textView    = view.findViewById(R.id.satellite_count_textView);
        broad_temp_textView         = view.findViewById(R.id.broad_temp_textView);
        li_voltage_textView         = view.findViewById(R.id.li_voltage_textView);
        sun_voltage_textView        = view.findViewById(R.id.sun_voltage_textView);

        wifi_ssid_textView          = view.findViewById(R.id.wifi_ssid_textView);

        wifi_ssid_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

        time_zone_textView = view.findViewById(R.id.time_zone_textView);

        time_zone_minus_btn = view.findViewById(R.id.time_zone_minus_btn);
        time_zone_add_btn = view.findViewById(R.id.time_zone_add_btn);

        int originalTimeZone = PreferencesUtils.getInt(getActivity(), "time_zone");
        if (originalTimeZone == -1) originalTimeZone = Constant.TIME_ZONE;

        modifyTimeZoneBtn(originalTimeZone);
        int timeZone = originalTimeZone - 12;
        time_zone_textView.setText( timeZone > 0 ? " + " + timeZone : timeZone + "");

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
    }

    private void getData() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("address", "168857");
            jsonObject.put("signal", "78");
            jsonObject.put("location_per", "5min");
            jsonObject.put("gps_per", "1s");
            jsonObject.put("central_number", "378378");

            address_textView        .setText(PreferencesUtils.getString(getActivity(), "myNumber"));
            communication_from_textView.setText(PreferencesUtils.getString(getActivity(), "communication_from"));

            String ssid = PreferencesUtils.getString(getActivity(), "wifiSSID");
            if (ssid != null ) {
                wifi_ssid_textView.setText(ssid);
            } else {
                wifi_ssid_textView.setText(getString(R.string.wifi_ssid));
            }

            signal_textView         .setText(jsonObject.getString("signal"));
            location_freq_textView  .setText(jsonObject.getString("location_per"));
            gps_freq_textView       .setText(jsonObject.getString("gps_per"));
            central_number_textView .setText(jsonObject.getString("central_number"));

            location_from_textView.setText("GPS/BD");
            communication_from_textView.setText("GPRS");
            signal_power_textView.setText("30");
            satellite_count_textView.setText("8");
            broad_temp_textView.setText("-7℃");
            li_voltage_textView.setText("4.16(96%)V");
            sun_voltage_textView.setText("3.12V");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void smsTempAdd() {
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

                                if (isNumer(text.toString())) {
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
                        if (text != null && text.length() > 0 && text.length() <= 3 && isNumer(text.toString())) {
                            int number = Integer.parseInt(text.toString());
                            if (number < 1 || number > 255) {
                                Toast.makeText(getActivity(), "分组编号超出氛围(1到255)", Toast.LENGTH_SHORT).show();
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

    private boolean isNumer(String txt) {
        Pattern p = Pattern.compile("[0-9]*");
        Matcher m = p.matcher(txt);
        return m.matches();
    }

    private void editGroup() {
        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getActivity());
        builder.setTitle("用户分组")
                .setPlaceholder("在此输入您的分组编号（1 - 255）")
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
                        if (text != null && text.length() > 0) {

                            if (!isNumer(text.toString())) {
                                Toast.makeText(getActivity(), "请输入正确的分组编号", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            int group = Integer.parseInt(text.toString());
                            if (group < 1 || group > 255) {
                                Toast.makeText(getActivity(), "请输入正确的分组编号(1 - 255)", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            PreferencesUtils.putInt(getActivity(), "group", group);
                            Toast.makeText(getActivity(), "分组编号设置成功：" + group, Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(getActivity(), "输入您的分组编号（1 - 255）", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

}
