package com.cetcme.xkterminal.port;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.R;
import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class USBFragment2 extends Fragment implements View.OnClickListener {

    private ScrollView sv1;
    private TextView tv_receive, tv_status;
    private EditText et_send;
    private Spinner spinner1, spinner2;
    private Button btn1, btn2, btn3, btn4;
    private CheckBox cb_receive, cb_send;

    private String oldMsg = "";
    private String hexMsg = "";
    private ArrayAdapter<String> adapter, adapter2;
    private String[] paths = new String[]{"1", "2", "3", "4"};
    private String[] ports = new String[]{"110",
            "300",
            "600",
            "1200",
            "2400",
            "4800",
            "9600",
            "14400",
            "19200",
            "38400",
            "56000",
            "57600",
            "115200",
            "128000",
            "256000"};

    private boolean recive16 = false;
    private boolean send16 = false;

    private Handler handler;

    public static String currentPath = "";

    private final Map<String, USBInfo> openMap = MyApplication.getInstance().openMap;

    private String oldContent = "";// 存储旧内容
    private String old16Content = "";

    private static D2xxManager ftdid2xx = MyApplication.getInstance().ftdid2xx;

    public static USBFragment2 newInstance() {

        Bundle args = new Bundle();

        USBFragment2 fragment = new USBFragment2();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (ftdid2xx != null) {
            ftdid2xx.createDeviceInfoList(getActivity());
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_ports, container, false);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0x1:
                        if (msg.obj != null) {
                            final byte[] str = (byte[]) msg.obj;
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    hexMsg += Utils.byte2HexStr(str) + " ";
                                    oldMsg += new String(str);
                                    handler.sendEmptyMessage(0x2);
                                    if (oldMsg.length() > 2000) {
                                        oldMsg = "";
                                        hexMsg = "";
                                    }
                                }
                            }).start();
                        }
                        break;
                    case 0x2:
                        if (recive16) {
                            tv_receive.setText(hexMsg);
                        } else {
                            tv_receive.setText(oldMsg);
                        }
                        sv1.fullScroll(ScrollView.FOCUS_DOWN);
                        break;
                }
            }
        };
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        onBindView(view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(USBEvent usbEvent) {
        Message message = Message.obtain();
        message.what = usbEvent.getWhat();
        message.obj = usbEvent.getMessage();
        if (handler != null) {
            handler.sendMessage(message);
        }
    }

    private void onBindView(View view) {
        tv_receive = view.findViewById(R.id.tv_receive);
        tv_status = view.findViewById(R.id.tv_status);
        et_send = view.findViewById(R.id.et_send);
        et_send.addTextChangedListener(new TextWatcher() {
            private boolean isHefa = true;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                Log.e("TAG_PORT_SEND", charSequence.toString());
                if (send16) {
                    old16Content = charSequence.toString();
                } else {
                    oldContent = charSequence.toString();
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Pattern pattern = Pattern.compile("[^0-9A-Fa-f\\s]+");
                String str = charSequence.toString();
                if (send16) {
                    Matcher matcher = pattern.matcher(str);
                    if (matcher.find()) {
                        // et_send.setText(old16Content);
                        isHefa = false;
                        Toast.makeText(getActivity(), "只能输入[0-9A-Fa-f]字符", Toast.LENGTH_SHORT).show();
                    } else {
                        isHefa = true;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (send16) {
                    if (isHefa) {
                        old16Content = editable.toString();
                    } else {
                        et_send.setText(old16Content);
                    }
                } else {
                    oldContent = editable.toString();
                }
            }
        });
        sv1 = view.findViewById(R.id.sv1);
        spinner1 = view.findViewById(R.id.spinner1);
        spinner2 = view.findViewById(R.id.spinner2);
        btn1 = view.findViewById(R.id.btn1);
        btn1.setOnClickListener(this);
        btn2 = view.findViewById(R.id.btn2);
        btn2.setOnClickListener(this);
        btn3 = view.findViewById(R.id.btn3);
        btn3.setOnClickListener(this);
        btn4 = view.findViewById(R.id.btn4);
        btn4.setOnClickListener(this);
        cb_receive = view.findViewById(R.id.cb_receive);
        cb_send = view.findViewById(R.id.cb_send);
        cb_receive.setChecked(recive16);
        cb_send.setChecked(send16);
        cb_receive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                recive16 = b;
                handler.sendEmptyMessage(0x2);
            }
        });
        cb_send.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                send16 = b;
                if (send16) {
                    et_send.setText(Utils.str2HexStr(oldContent));
                } else {
                    et_send.setText(Utils.hexStr2Str(old16Content.replace(" ", "").toUpperCase()));
                }
            }
        });


        if (adapter == null) {
            adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, paths);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }
        spinner1.setAdapter(adapter);
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                clearMsg();
                currentPath = (String) spinner1.getSelectedItem();
                if (openMap.containsKey(currentPath)) {
                    USBInfo usbInfo = openMap.get(currentPath);
                    String rate = usbInfo.getBaudrate();
                    int position = adapter2.getPosition(rate);
                    spinner2.setSelection(position);
                }
                changeStatus();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if (adapter2 == null) {
            adapter2 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, ports);
            adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }
        spinner2.setAdapter(adapter2);
        spinner2.setSelection(6);
        spinner2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    final String path = (String) spinner1.getSelectedItem();
                    if (openMap.containsKey(path)) {
                        Toast.makeText(getActivity(), "串口" + path + "已打开，请先关闭", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
                return false;
            }
        });
        currentPath = (String) spinner1.getSelectedItem();
        changeStatus();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn1:
                // 打开串口
                String path = (String) spinner1.getSelectedItem();
                String rate = (String) spinner2.getSelectedItem();
                if (!openMap.containsKey(path)) {
                    try {
                        currentPath = path;
                        MyApplication.getInstance().connectFunction(path, rate);
                        clearMsg();
                        tv_status.setBackgroundResource(R.drawable.circle_green);
                        Toast.makeText(getActivity(), "打开串口" + path + "成功", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "打开串口" + path + "失败", Toast.LENGTH_SHORT).show();
                        MyApplication.getInstance().disconnectFunction(path);
                    }
                } else {
                    Toast.makeText(getActivity(), "串口" + path + "已打开", Toast.LENGTH_SHORT).show();
                    break;
                }
                break;
            case R.id.btn2:
                if (!openMap.containsKey(currentPath)) {
                    Toast.makeText(getActivity(), "串口" + currentPath + "未打开", Toast.LENGTH_SHORT).show();
                    break;
                }
                MyApplication.getInstance().disconnectFunction(currentPath);
                if (currentPath.equals(currentPath)) {
                    clearMsg();
                }
                tv_status.setBackgroundResource(R.drawable.circle_red);
                Toast.makeText(getActivity(), "关闭串口" + currentPath + "成功", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn3:
                clearMsg();
                break;
            case R.id.btn4:// 发送
                if (TextUtils.isEmpty(currentPath)) {
                    Toast.makeText(getActivity(), "串口未打开，请先打开串口", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (!openMap.containsKey(currentPath)) break;
                USBInfo usbInfo = openMap.get(currentPath);
                FT_Device ftDevice = usbInfo.getFtDevice();
                String content = et_send.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    break;
                }
                byte[] byts = null;
                if (send16) {
                    byts = Utils.hexStr2Bytes(content.replace(" ", "").toUpperCase());
                } else {
                    byts = content.getBytes();
                }
                if (byts != null && ftDevice != null) {
                    try {
                        ftDevice.write(byts, byts.length);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void clearMsg() {
        oldMsg = "";
        hexMsg = "";
        tv_receive.setText("");
    }

    private void changeStatus() {
        if (tv_status != null) {
            if (openMap.containsKey(currentPath)) {

                tv_status.setBackgroundResource(R.drawable.circle_green);
            } else {
                tv_status.setBackgroundResource(R.drawable.circle_red);
            }
        }
    }
}
