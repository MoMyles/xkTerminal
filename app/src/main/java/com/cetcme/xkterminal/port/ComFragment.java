package com.cetcme.xkterminal.port;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.cetcme.xkterminal.DataFormat.MessageFormat;
import com.cetcme.xkterminal.DataFormat.Util.ConvertUtil;
import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.PreferencesUtils;
import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.Sqlite.Bean.GPSBean;
import com.cetcme.xkterminal.Sqlite.Bean.LocationBean;
import com.cetcme.xkterminal.Sqlite.Bean.OtherShipBean;

import org.codice.common.ais.Decoder;
import org.codice.common.ais.message.Message18;
import org.codice.common.ais.message.Message19;
import org.greenrobot.eventbus.EventBus;
import org.xutils.DbManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import aisparser.Message1;
import aisparser.Message11;
import aisparser.Message14;
import aisparser.Message2;
import aisparser.Message24;
import aisparser.Message3;
import aisparser.Message4;
import aisparser.Message5;
import aisparser.Sixbit;
import aisparser.Vdm;
import android_serialport_api.SerialPort;
import yimamapapi.skia.AisInfo;

import static com.cetcme.xkterminal.MainActivity.play;

public class ComFragment extends Fragment implements View.OnClickListener {

    private ScrollView sv1;
    private TextView tv_receive, tv_status;
    private EditText et_send;
    private Spinner spinner1, spinner2, spinner3;
    private Button btn1, btn2, btn3, btn4;
    private CheckBox cb_receive, cb_send;

    private String oldMsg = "";
    private String hexMsg = "";
    private ArrayAdapter<String> adapter, adapter2, adapter3;
    private String[] paths = new String[]{"/dev/ttyS1"};
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

//    private SerialPort port1 = null;
//    private InputStream is1 = null;
//    private OutputStream os1 = null;
//    private AisReadThread aisReadThread;

    private DbManager db;

    private Handler handler;
    private int messageCount1 = 0;
    private int messageCount2 = 0;

    private String currentPath = "";
    private final Map<String, String> openMap = new HashMap<>();//存储已打开的串口
    private final ArrayList<String> openPathList = new ArrayList<>();//存储已打开的串口
    private final Map<String, AisReadThread> openAisRead = new HashMap<>();//存储已打开的串口的读取线程
    private final Map<String, OutputStream> openOutputStream = new HashMap<>();//存储已打开的串口的输出流
    private final Map<String, InputStream> openInputStream = new HashMap<>();//存储已打开的串口的输出流
    private final Map<String, SerialPort> openSeriaPort = new HashMap<>();//存储已打开的串口的输出流
    private String oldContent = "";// 存储旧内容
    private String old16Content = "";

    public static ComFragment newInstance() {

        Bundle args = new Bundle();

        ComFragment fragment = new ComFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_ports, container, false);
        db = MyApplication.getInstance().getDb();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0x1:
                        if (msg.obj != null) {
                            final byte[] str = (byte[]) msg.obj;
//                            final byte[] b = new byte[]{str};
                            handler.post(new Runnable() {
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
                            });
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
        onBindView(view);
        return view;
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
                }else {
                    oldContent = editable.toString();
                }
            }
        });
        sv1 = view.findViewById(R.id.sv1);
        spinner1 = view.findViewById(R.id.spinner1);
        spinner2 = view.findViewById(R.id.spinner2);
        spinner3 = view.findViewById(R.id.spinner3);
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
//                Log.e("TAG_PATH", currentPath);
                if (openMap.containsKey(currentPath)) {
                    String rate = openMap.get(currentPath);
                    int position = adapter2.getPosition(rate);
                    spinner2.setSelection(position);
//                    aisReadThread = openAisRead.get(currentPath);
//                    is1 = openInputStream.get(currentPath);
//                    os1 = openOutputStream.get(currentPath);
//                    port1 = openSeriaPort.get(currentPath);
                    tv_status.setBackgroundResource(R.drawable.circle_green);
                } else {
                    tv_status.setBackgroundResource(R.drawable.circle_red);
                }
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
        if (adapter3 == null) {
            adapter3 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, openPathList);
            adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }
        spinner3.setAdapter(adapter3);
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                clearMsg();
                currentPath = openPathList.get(i);
//                aisReadThread = openAisRead.get(currentPath);
//                is1 = openInputStream.get(currentPath);
//                os1 = openOutputStream.get(currentPath);
//                port1 = openSeriaPort.get(currentPath);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (openMap.containsKey(currentPath)) {
            tv_status.setBackgroundResource(R.drawable.circle_green);
        } else {
            tv_status.setBackgroundResource(R.drawable.circle_red);
        }
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
                        SerialPort port1 = open(path, Integer.valueOf(rate));
                        InputStream is1 = port1.getInputStream();
                        OutputStream os1 = port1.getOutputStream();
                        AisReadThread aisReadThread = new AisReadThread(path);
                        openMap.put(path, rate);
                        openAisRead.put(path, aisReadThread);
                        openOutputStream.put(path, os1);
                        openInputStream.put(path, is1);
                        openSeriaPort.put(path, port1);
                        aisReadThread.start();
                        currentPath = path;
                        if (adapter3 != null) {
                            openPathList.add(path);
                            adapter3.notifyDataSetChanged();
                        }
                        clearMsg();
                        Toast.makeText(getActivity(), "打开串口" + path + "成功", Toast.LENGTH_SHORT).show();
                        tv_status.setBackgroundResource(R.drawable.circle_green);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "打开串口" + path + "失败", Toast.LENGTH_SHORT).show();
                        close1(path);
                        openMap.remove(path);
                    }
                } else {
                    Toast.makeText(getActivity(), "串口" + path + "已打开", Toast.LENGTH_SHORT).show();
                    break;
                }
                break;
            case R.id.btn2:
                String path1 = (String) spinner1.getSelectedItem();
                if (!openMap.containsKey(path1)) {
                    Toast.makeText(getActivity(), "串口" + path1 + "已关闭", Toast.LENGTH_SHORT).show();
                    break;
                }
                close1(path1);
                openMap.remove(path1);
//                openAisRead.remove(path1);
//                openSeriaPort.remove(path1);
//                openInputStream.remove(path1);
//                openOutputStream.remove(path1);
//                if (currentPath.equals(path1)) {
//                    if (adapter3 != null) {
//                        openPathList.remove(path1);
//                        adapter3.notifyDataSetChanged();
//                    }
//                    clearMsg();
//                }
                tv_status.setBackgroundResource(R.drawable.circle_red);
                Toast.makeText(getActivity(), "关闭串口" + path1 + "成功", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn3:
                clearMsg();
                break;
            case R.id.btn4:// 发送
                OutputStream os1 = openOutputStream.get(currentPath);
                if (os1 == null) {
                    Toast.makeText(getActivity(), "串口未打开，请先打开串口", Toast.LENGTH_SHORT).show();
                    break;
                }
                String content = et_send.getText().toString();
                if (TextUtils.isEmpty(content)) {
//                    Toast.makeText(getActivity(), "发送内容不能为空", Toast.LENGTH_SHORT).show();
                    break;
                }
                byte[] byts = null;
                if (send16) {
                    byts = Utils.hexStr2Bytes(content.replace(" ", "").toUpperCase());
//                    byts = Utils.hexStr2Str().getBytes();
                } else {
                    byts = content.getBytes();
                }
                if (byts!=null && os1 != null) {
                    try {
                        os1.write(byts);
//                        Toast.makeText(getActivity(), "发送成功", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void clearMsg() {
        messageCount1 = 0;
        messageCount2 = 0;
        oldMsg = "";
        hexMsg = "";
        tv_receive.setText("");
//        et_send.setText("");
    }


    private SerialPort open(String com, int buadRate) throws IOException {
        if (TextUtils.isEmpty(com) || (com != null && com.length() == 0) || (buadRate == -1)) {
            throw new InvalidParameterException();
        }
        return new SerialPort(new File(com), buadRate, 0);
    }

    public void close1(String path) {
        try {
            if (openAisRead.containsKey(path)) {
                AisReadThread aisReadThread = openAisRead.get(path);
                if (aisReadThread != null) {
                    aisReadThread.interrupt();
                }
                openAisRead.remove(path);
            }
            if (openInputStream.containsKey(path)) {
                InputStream is1 = openInputStream.get(path);
                if (is1 != null) {
                    is1.close();
                }
                openInputStream.remove(is1);
            }
            if (openOutputStream.containsKey(path)) {
                OutputStream os1 = openOutputStream.get(path);
                if (os1 != null) {
                    os1.close();
                }
                openOutputStream.remove(os1);
            }
            if (openSeriaPort.containsKey(path)) {
                SerialPort port1 = openSeriaPort.get(path);
                if (port1 != null) {
                    port1.close();
                }
                openSeriaPort.remove(path);
            }
//            if (aisReadThread != null) {
//                aisReadThread.interrupt();
//                aisReadThread = null;
//            }
//            if (is1 != null) {
//                is1.close();
//                is1 = null;
//            }
//            if (os1 != null) {
//                os1.close();
//                os1 = null;
//            }
//            if (port1 != null) {
//                port1.close();
//                port1 = null;
//            }
//            try {
//                Thread.sleep(200);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            clearMsg();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    class AisReadThread extends Thread {

        private String path;

        public AisReadThread(String path) {
            this.path = path;
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    if (openInputStream == null) return;
                    InputStream is1 = openInputStream.get(path);
                    if (is1 == null) return;
                    int size = is1.available();
                    byte[] buffer = new byte[size];
                    is1.read(buffer);
                    if (size > 0) {
                        if (currentPath.equals(path)) {
                            Message msg = Message.obtain();
                            msg.what = 0x1;
                            msg.obj = buffer;
                            handler.sendMessage(msg);
                        }
                        for (int i=0;i<size;i++) {
                            onAisDataReceived(new byte[]{buffer[i]});
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }


    private final List<Byte> aisByts = new LinkedList<>();
    private String preRestStr = "";
    private final List<Map<String, Object>> headIndex = new ArrayList<>();
    private static final Vdm vdm = new Vdm();

    protected void onAisDataReceived(byte[] buffer) {
        if (buffer[0] == 33 || buffer[0] == 36) {
            aisByts.clear();
            aisByts.add(buffer[0]);
        } else {
            aisByts.add(buffer[0]);
            int len = aisByts.size();
            if (len > 5 && aisByts.get(len - 2) == 13 && aisByts.get(len - 1) == 10) {
                // \r\n 结尾
                Byte[] byts = aisByts.toArray(new Byte[len]);
                byte[] tmpByts = new byte[len];
                for (int i = 0; i < len; i++) {
                    tmpByts[i] = byts[i];
                }
                String gpsDataStr = new String(tmpByts);
                if (gpsDataStr.startsWith("$04")) {
                    String[] messageStrings = MessageFormat.unFormat(tmpByts);
                    String address = messageStrings[0];
                    String content = messageStrings[1];
                    String type = messageStrings[2];
                    int group = Integer.parseInt(messageStrings[3]);
                    int frameCount = Integer.parseInt(messageStrings[4]);
                    final String unique = ConvertUtil.rc4ToHex();
                    if (MessageFormat.MESSAGE_TYPE_TRADE.equals(type)) {
                        MyApplication.getInstance().sendBytes(MessageFormat.format(Constant.SERVER_BD_NUMBER// 蘑菇头编号
                                , content, MessageFormat.MESSAGE_TYPE_TRADE, 0, unique));
                    }
                } else {
                    headIndex.clear();
                    gpsDataStr = preRestStr + gpsDataStr;
                    len = gpsDataStr.length();
                    if (len <= 6) {
                        preRestStr = gpsDataStr;
                        return;
                    }
                    for (int i = 0; i < len - 6; i++) {
                        String headStr = gpsDataStr.substring(i, i + 6);
                        if ("!AIVDM".equals(headStr)
                                || "!AIVDO".equals(headStr)
                                || "$GPGSV".equals(headStr)) {
                            MyApplication.getInstance().oldAisReceiveTime = System.currentTimeMillis();
                            if (!MyApplication.getInstance().isAisConnected) {
                                MyApplication.getInstance().isAisConnected = true;
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        MyApplication.getInstance().mainActivity.gpsBar.setAisStatus(true);
                                        play("AIS已连接");
                                    }
                                });
                            }
                            int end = gpsDataStr.indexOf("\n", i + 1);
                            if (end != -1) {
                                String str = gpsDataStr.substring(i + 7, end + 1);
                                if (str.contains("$") || str.contains("!")) {
                                    continue;
                                }
                                //我要的头
                                Map<String, Object> map = new HashMap<>();
                                map.put("type", headStr);
                                map.put("index", i);
                                headIndex.add(map);
                            } else {
                                preRestStr = gpsDataStr.substring(i);
                            }
                        } else {
                            if (len - i < 6) {
                                preRestStr = gpsDataStr.substring(i + 1);
                            }
                        }
                    }
                    for (int i = 0; i < headIndex.size(); i++) {
                        Map<String, Object> map = headIndex.get(i);
                        int end = gpsDataStr.indexOf("\n", (Integer) map.get("index") + 1);
                        if (end != -1) {
                            String newStr = gpsDataStr.substring((Integer) map.get("index"), end + 1);
                            preRestStr = "";
                            String type = (String) map.get("type");
                            if ("!AIVDM".equals(type)
                                    || "!AIVDO".equals(type)) {
                                Log.e("TAG", "ais: " + newStr);
                                try {

                                    boolean isOwn = "!AIVDO".equals(type);
                                    int result = vdm.add(newStr);
                                    boolean isMsg5 = false;
                                    if (0 == result) {
                                        AisInfo aisInfo = new AisInfo("null");
                                        Sixbit sixbit = vdm.sixbit();
                                        Log.e("TAG", "msg: " + vdm.msgid());
                                        switch (vdm.msgid()) {
                                            case 1:
                                                Message1 message1 = new Message1();
                                                message1.parse(sixbit);
                                                aisInfo.mmsi = (int) message1.userid();
                                                aisInfo.COG = message1.cog() / 10.0f;
                                                aisInfo.SOG = message1.sog() / 10.0f;
                                                aisInfo.MsgType = 1;
                                                aisInfo.longtitude = (int) (message1.longitude() * 1.0 / 600000 * 1e7);
                                                aisInfo.latititude = (int) (message1.latitude() * 1.0 / 600000 * 1e7);
                                                break;
                                            case 2:
                                                Message2 message2 = new Message2();
                                                message2.parse(sixbit);
                                                aisInfo.mmsi = (int) message2.userid();
                                                aisInfo.COG = message2.cog() / 10.0f;
                                                aisInfo.SOG = message2.sog() / 10.0f;
                                                aisInfo.MsgType = 2;
                                                aisInfo.longtitude = (int) (message2.longitude() * 1.0 / 600000 * 1e7);
                                                aisInfo.latititude = (int) (message2.latitude() * 1.0 / 600000 * 1e7);
                                                break;
                                            case 3:
                                                Message3 message3 = new Message3();
                                                message3.parse(sixbit);
                                                aisInfo.mmsi = (int) message3.userid();
                                                aisInfo.COG = message3.cog() / 10.0f;
                                                aisInfo.SOG = message3.sog() / 10.0f;
                                                aisInfo.MsgType = 3;
                                                aisInfo.longtitude = (int) (message3.longitude() * 1.0 / 600000 * 1e7);
                                                aisInfo.latititude = (int) (message3.latitude() * 1.0 / 600000 * 1e7);
                                                break;
                                            case 4:
                                                Message4 message4 = new Message4();
                                                message4.parse(sixbit);
                                                aisInfo.mmsi = (int) message4.userid();
                                                aisInfo.MsgType = 4;
                                                aisInfo.longtitude = (int) (message4.longitude() * 1.0 / 600000 * 1e7);
                                                aisInfo.latititude = (int) (message4.latitude() * 1.0 / 600000 * 1e7);
                                                break;
                                            case 11:
                                                Message11 message11 = new Message11();
                                                message11.parse(sixbit);
                                                aisInfo.mmsi = (int) message11.userid();
                                                aisInfo.MsgType = 11;
                                                aisInfo.longtitude = (int) (message11.longitude() * 1.0 / 600000 * 1e7);
                                                aisInfo.latititude = (int) (message11.latitude() * 1.0 / 600000 * 1e7);
                                                break;
                                            case 5:
                                                isMsg5 = true;
                                                Message5 message5 = new Message5();
                                                message5.parse(sixbit);
                                                if (MyApplication.osbDataList != null && !MyApplication.osbDataList.isEmpty()) {
                                                    for (OtherShipBean osb : MyApplication.osbDataList) {
                                                        if (osb.getMmsi() == message5.userid()) {
                                                            osb.setShip_name(message5.name().replace("@", ""));
                                                            osb.setCallsign(message5.callsign());
                                                            osb.setWidth(message5.dim_port() + message5.dim_starboard());
                                                            osb.setLenght(message5.dim_bow() + message5.dim_stern());
                                                        }
                                                    }
                                                    EventBus.getDefault().post("openShip2");
                                                }
                                                break;
                                            case 24:
                                                isMsg5 = true;
                                                Message24 Message24 = new Message24();
                                                Message24.parse(sixbit);
                                                if (MyApplication.osbDataList != null && !MyApplication.osbDataList.isEmpty()) {
                                                    for (OtherShipBean osb : MyApplication.osbDataList) {
                                                        if (osb.getMmsi() == Message24.userid()) {
                                                            osb.setShip_name(Message24.name());
                                                            osb.setCallsign(Message24.callsign());
                                                            osb.setWidth(Message24.dim_port() + Message24.dim_starboard());
                                                            osb.setLenght(Message24.dim_bow() + Message24.dim_stern());
                                                        }
                                                    }
                                                    EventBus.getDefault().post("openShip2");
                                                }
                                                break;
                                            case 14:
                                                Message14 message14 = new Message14();
                                                message14.parse(sixbit);
                                                String message = message14.message();
                                                if (TextUtils.isEmpty(message)) {
                                                    message = "AIS报警";
                                                }
                                                // 暂时停用14发信息功能
//                                            MyApplication.getInstance().sendBytes(WarnFormat.format("" + message14.userid(), message));
                                                break;
                                            case 18:
                                                aisparser.Message18 message18 = new aisparser.Message18();
                                                message18.parse(sixbit);
                                                aisInfo.mmsi = (int) message18.userid();
                                                aisInfo.COG = message18.cog() / 10.0f;
                                                aisInfo.SOG = message18.sog() / 10.0f;
                                                aisInfo.MsgType = 18;
                                                aisInfo.longtitude = (int) (message18.longitude() * 1.0 / 600000 * 1e7);
                                                aisInfo.latititude = (int) (message18.latitude() * 1.0 / 600000 * 1e7);
                                                break;
                                            case 19:
                                                aisparser.Message19 message19 = new aisparser.Message19();
                                                message19.parse(sixbit);
                                                aisInfo.shipName = message19.name().replace("@", "");
                                                aisInfo.mmsi = (int) message19.userid();
                                                aisInfo.COG = message19.cog() / 10.0f;
                                                aisInfo.SOG = message19.sog() / 10.0f;
                                                aisInfo.MsgType = 19;
                                                aisInfo.longtitude = (int) (message19.longitude() * 1.0 / 600000 * 1e7);
                                                aisInfo.latititude = (int) (message19.latitude() * 1.0 / 600000 * 1e7);
                                                aisInfo.Width = message19.dim_port() + message19.dim_starboard();
                                                aisInfo.Length = message19.dim_bow() + message19.dim_stern();
                                                aisInfo.ShipType = message19.ship_type();
                                                break;
                                        }
                                        if (isMsg5) continue;
                                        int mmsi = Integer.valueOf(PreferencesUtils.getString(MyApplication.getInstance().getApplicationContext(), "shipNo", "0")).intValue();
                                        if (aisInfo.mmsi == -1) {
                                            continue;
                                        }
                                        if (isOwn) {
                                            // 本船
                                            judge18(newStr, aisInfo);
                                        } else {
                                            // 其他船
                                            if (mmsi == aisInfo.mmsi) {
                                                judge18(newStr, aisInfo);
                                            } else {
                                                EventBus.getDefault().post(aisInfo);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if ("$GPGSV".equals(type)) {
                                Log.e("TAG", "gps: " + newStr);
                                try {
                                    newStr = newStr.substring(newStr.indexOf(",") + 1, newStr.lastIndexOf("*"));
                                    boolean isDou = newStr.endsWith(",");
                                    if (isDou) {
                                        newStr += "0,";
                                    }
                                    String[] arr = newStr.split(",");
                                    for (int j = 3; j < arr.length; j += 4) {
                                        int no = Integer.valueOf(arr[j]);
                                        int yangjiao = Integer.valueOf(arr[j + 1]);
                                        int fangwei = Integer.valueOf(arr[j + 2]);
                                        int xinhao = 0;
                                        xinhao = Integer.valueOf("".equals(arr[j + 3]) ? "0" : arr[j + 3]);

                                        GPSBean bean = db.selector(GPSBean.class).where("no", "=", no).findFirst();
                                        if (bean == null) {
                                            // 不存在
                                            bean = new GPSBean();
                                            bean.setNo(no);
                                            bean.setYangjiao(yangjiao);
                                            bean.setFangwei(fangwei);
                                            bean.setXinhao(xinhao);
                                            db.saveBindingId(bean);
                                        } else {
                                            // 存在
                                            bean.setYangjiao(yangjiao);
                                            bean.setFangwei(fangwei);
                                            bean.setXinhao(xinhao);
                                            db.update(bean, "yangjiao", "fangwei", "xinhao");
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            preRestStr = gpsDataStr.substring((Integer) map.get("index"));
                            // Log.e("TAG", "pre: " + preRestStr);
                        }
                    }
                }
                aisByts.clear();
            }
        }
        if (aisByts.size() > 512) {
            aisByts.clear();
        }
    }

    private void judge18(String newStr, AisInfo aisInfo) {
        if (18 == aisInfo.MsgType
                || 19 == aisInfo.MsgType) {
            try {
                List<org.codice.common.ais.message.Message> list = new Decoder().parseString(newStr);
                if (list != null && !list.isEmpty()) {
                    for (org.codice.common.ais.message.Message m : list) {
                        if (18 == m.getMessageType()) {
                            Message18 m18 = (Message18) m;
                            LocationBean locationBean = new LocationBean();
                            locationBean.setLatitude((int) (m18.getLat() * 1e7));
                            locationBean.setLongitude((int) (m18.getLon() * 1e7));
                            locationBean.setSpeed((float) m18.getSog());
                            locationBean.setHeading((float) m18.getTrueHeading());
                            locationBean.setAcqtime(Constant.SYSTEM_DATE);
                            MyApplication.getInstance().currentLocation = locationBean;
                            EventBus.getDefault().post(locationBean);
                        } else if (19 == m.getMessageType()) {
                            Message19 m19 = (Message19) m;
                            LocationBean locationBean = new LocationBean();
                            locationBean.setLatitude((int) (m19.getLat() * 1e7));
                            locationBean.setLongitude((int) (m19.getLon() * 1e7));
                            locationBean.setSpeed((float) m19.getSog());
                            locationBean.setHeading((float) m19.getTrueHeading());
                            locationBean.setAcqtime(Constant.SYSTEM_DATE);
                            MyApplication.getInstance().currentLocation = locationBean;
                            EventBus.getDefault().post(locationBean);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            LocationBean locationBean = new LocationBean();
            locationBean.setLatitude(aisInfo.latititude);
            locationBean.setLongitude(aisInfo.longtitude);
            locationBean.setSpeed(aisInfo.SOG);
            locationBean.setHeading(aisInfo.COG);
            locationBean.setAcqtime(Constant.SYSTEM_DATE);
            MyApplication.getInstance().currentLocation = locationBean;
            EventBus.getDefault().post(locationBean);
        }
    }
}
