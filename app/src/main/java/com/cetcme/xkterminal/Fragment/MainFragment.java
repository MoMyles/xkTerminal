package com.cetcme.xkterminal.Fragment;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cetcme.xkterminal.DataFormat.AlertFormat;
import com.cetcme.xkterminal.Event.SmsEvent;
import com.cetcme.xkterminal.Fragment.adapter.RvShipAdapter;
import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.GPSFormatUtils;
import com.cetcme.xkterminal.MyClass.PreferencesUtils;
import com.cetcme.xkterminal.MyClass.SoundPlay;
import com.cetcme.xkterminal.Navigation.SkiaDrawView;
import com.cetcme.xkterminal.Navigation.WarnArea;
import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.Sqlite.Bean.LocationBean;
import com.cetcme.xkterminal.Sqlite.Bean.OtherShipBean;
import com.cetcme.xkterminal.Sqlite.Bean.PinBean;
import com.qiuhong.qhlibrary.QHTitleView.QHTitleView;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

import yimamapapi.skia.AisInfo;
import yimamapapi.skia.M_POINT;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by qiuhong on 10/01/2018.
 */

public class MainFragment extends Fragment implements SkiaDrawView.OnMapClickListener {

    private SkiaDrawView skiaDrawView;
    private ConstraintLayout main_layout;
    private LinearLayout alert_layout;
    private LinearLayout ll_ship_list;
    private RecyclerView rv_ships;
    private ImageView iv1;

    private Switch mSwitchYQ, mSwitchArea, mSwitchPin;

    private Button zoomOut, zoomIn;


    private TextView tv_lon;
    private TextView tv_lat;
    private TextView tv_head;
    private TextView tv_speed;

    private QMUIRoundButton alert_confirm_btn;
    private QMUIRoundButton alert_cancel_btn;
    private TextView alert_tv;

    private boolean alert_need_flash = false;
    private boolean warnArea = false;

    private final int[] JIN_YU_AREA_COLOR = new int[]{255, 0, 0};
    private final int[] JIN_RU_AREA_COLOR = new int[]{0, 255, 0};
    private final int[] JIN_CHU_AREA_COLOR = new int[]{0, 0, 255};


    private final int[] JIN_CHU_AREA_X = new int[]{(int) (122.259983 * 1e7), (int) (122.680821 * 1e7), (int) (122.675072 * 1e7), (int) (122.311725 * 1e7)};
    private final int[] JIN_CHU_AREA_Y = new int[]{(int) (28.722246 * 1e7), (int) (28.690818 * 1e7), (int) (28.199928 * 1e7), (int) (28.226411 * 1e7)};

//    private final int[] JIN_RU_AREA_X = new int[]{(int) (121.761309 * 1e7), (int) (121.949882 * 1e7), (int) (121.902739 * 1e7), (int) (121.772808 * 1e7)};
//    private final int[] JIN_RU_AREA_Y = new int[]{(int) (28.651267 * 1e7), (int) (28.660395 * 1e7), (int) (28.530491 * 1e7), (int) (28.517288 * 1e7)};
//
//    private final int[] JIN_YU_AREA_X = new int[]{(int) (121.583086 * 1e7), (int) (121.68887 * 1e7), (int) (121.676222 * 1e7), (int) (121.578486 * 1e7)};
//    private final int[] JIN_YU_AREA_Y = new int[]{(int) (28.222337 * 1e7), (int) (28.219282 * 1e7), (int) (28.159172 * 1e7), (int) (28.183627 * 1e7)};


    private DbManager db;

    //    private ArrayAdapter<String> aisInfoAdapter;
//    private ListView mLvAisInfo;
//    private final List<String> datas = new LinkedList<>();
    private RvShipAdapter rvShipAdapter;
//    private final List<OtherShipBean> datas = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_main, container, false);
        EventBus.getDefault().register(this);

        iv1 = view.findViewById(R.id.iv1);
        iv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MyApplication.currentLocation != null) {
                    skiaDrawView.mYimaLib.CenterMap(MyApplication.currentLocation.getLongitude(), MyApplication.currentLocation.getLatitude());
                    skiaDrawView.postInvalidate();
                } else {
                    SoundPlay.startAlertSound(getActivity());
                }
            }
        });

        main_layout = view.findViewById(R.id.main_layout);
        skiaDrawView = view.findViewById(R.id.skiaView);
        skiaDrawView.setOnMapClickListener(this);

        alert_layout = view.findViewById(R.id.alert_layout);
        ll_ship_list = view.findViewById(R.id.ll_ship_list);
        ViewGroup.LayoutParams lp = ll_ship_list.getLayoutParams();
        lp.width = QMUIDisplayHelper.getScreenWidth(getActivity()) * 4 / 10;
        rv_ships = view.findViewById(R.id.rv_ships);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rv_ships.setLayoutManager(linearLayoutManager);
        rv_ships.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        // datas.clear();
        rvShipAdapter = new RvShipAdapter(getActivity(), MyApplication.osbDataList, skiaDrawView);
        rv_ships.setAdapter(rvShipAdapter);

        tv_lon = view.findViewById(R.id.tv_lon);
        tv_lat = view.findViewById(R.id.tv_lat);

        tv_head = view.findViewById(R.id.tv_head);
        tv_speed = view.findViewById(R.id.tv_speed);

//        mLvAisInfo = view.findViewById(R.id.ais_info_lv);
//        aisInfoAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, datas);
//        mLvAisInfo.setAdapter(aisInfoAdapter);


        alert_confirm_btn = view.findViewById(R.id.alert_confirm_btn);
        alert_confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmAlert();
            }
        });

        alert_cancel_btn = view.findViewById(R.id.alert_cancel_btn);
        alert_cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelAlert();
            }
        });

        alert_tv = view.findViewById(R.id.alert_tv);

        zoomIn = view.findViewById(R.id.btn_zoom_in);
        zoomOut = view.findViewById(R.id.btn_zoom_out);
        zoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("放大");
                skiaDrawView.mYimaLib.SetCurrentScale(skiaDrawView.mYimaLib.GetCurrentScale() / 2);
                skiaDrawView.postInvalidate();//刷新fMainView
            }
        });
        zoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("缩小");
                skiaDrawView.mYimaLib.SetCurrentScale(skiaDrawView.mYimaLib.GetCurrentScale() * 2);
                skiaDrawView.postInvalidate();//刷新fMainView
            }
        });

        view.findViewById(R.id.app_name_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ((MainActivity)getActivity()).showShutDownHud();
//                ((MyApplication) getActivity().getApplication()).sendLightOn(true);
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        ((MyApplication) getActivity().getApplication()).sendLightOn(false);
//                    }
//                }, 2000);
            }
        });

        mSwitchYQ = view.findViewById(R.id.switch_yuqu);
        mSwitchYQ.setChecked(PreferencesUtils.getBoolean(getActivity(), "mainFrgYuqu", false));
        mSwitchArea = view.findViewById(R.id.switch_warn_area);
        mSwitchArea.setChecked(PreferencesUtils.getBoolean(getActivity(), "mainFrgWarnArea", false));
        mSwitchPin = view.findViewById(R.id.switch_pin);
//        mSwitchPin.setChecked(PreferencesUtils.getBoolean(getActivity(), "mainFrgPin", false));
        mSwitchPin.setChecked(false);

        warnArea = mSwitchArea.isChecked();
        showWarnAreas();
        mSwitchYQ.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                skiaDrawView.changeFishState(b);
                PreferencesUtils.putBoolean(getActivity(), "mainFrgYuqu", b);
            }
        });
        mSwitchArea.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferencesUtils.putBoolean(getActivity(), "mainFrgWarnArea", b);
                warnArea = b;
                showWarnAreas();
                if (MyApplication.currentLocation != null) {
                    doAreaWarning(MyApplication.currentLocation);
                }
            }
        });

        mSwitchPin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                PreferencesUtils.putBoolean(getActivity(), "mainFrgPin", b);
                if (b) {
                    showBiaoweis();
                } else {
                    skiaDrawView.clearBiaoWeiTrack();
                }
            }
        });

        db = MyApplication.getInstance().getDb();

        if (PreferencesUtils.getBoolean(getActivity(), "homePageAlertView")) {
            showAlertLayout();
        } else {
            showMainLayout();
        }

        loadOwnShipInfo();


//        LocationBean lb = new LocationBean();
//        lb.setLongitude((int) (121.768783 * 1e7));
//        lb.setLatitude((int) (28.696902 * 1e7));
//        lb.setHeading(166.1f);
//        lb.setSpeed(0.9f);
//        MyApplication.currentLocation = lb;
//        EventBus.getDefault().post(lb);


        // 作假200艘船

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Random random = new Random();
//                String points = "122.4151167,29.9224167;122.23819,29.97372;122.23819,29.97372;119.5712833,25.2664833;122.45225,29.9193333;120.6482833,27.74455;120.6589667,30.7708833;122.33397,30.01659;122.2952667,29.9439;122.23314,29.9831;122.28462,29.94163;122.2973,29.9452667;122.29934,29.94814;122.28102,29.93588;122.29934,29.94814;123.2278167,30.0992;120.6590667,30.7712167;120.6592667,30.77155;122.34565,29.9599167;122.7876,30.13737;120.6582833,30.7707667;121.9443,29.1990667;122.59087,30.13731;122.1960333,30.2475167;121.94175,28.1078167;122.3124333,29.94355;122.4485,29.9514667;122.88211,30.97533;122.8970833,29.7419;122.74859,31.3565;122.2589,29.94375;122.1357833,30.1174167;122.87455,31.2561833;122.56635,29.3400167;122.1963,29.70954;122.29483,29.94556;120.65316,31.99066;122.2780333,30.0642167;122.2771167,30.0640833;122.2779833,30.0638;122.2762333,30.0646333;122.27745,30.0635333;122.2775167,30.064;122.2772833,30.0633333;122.24391,30.07826;122.7876,30.13737;122.30535,29.94639;122.2771667,30.0638167;122.27755,30.0637333;122.37586,29.87019;122.3068333,29.9463;122.37586,29.87019;122.26764,29.94841;122.6095833,30.1931;122.2824667,30.0669333;122.31586,29.97971;123.3865833,31.0133667;121.9356,29.1846667;121.89904,29.15417;122.26046,28.89021;122.2637833,29.94455;122.27777,29.94697;122.29904,29.94659;122.26845,29.94695;122.25875,29.9469333;122.3365,29.9166667;120.6582833,30.7707667;122.3067,29.94769;122.30626,29.94695;122.26558,29.94453;122.33804,29.9853;122.0067833,30.1556833;122.2906167,29.9433167;122.2921333,29.9435833;122.00396,30.15784;122.30626,29.94695;122.3370833,29.9166667;122.29435,29.95149;122.3067,29.94769;122.3080667,29.9470167;122.32016,29.96698;122.30744,29.94948;122.2430833,29.9585167;122.30626,29.94695;122.28405,29.93665;120.6409167,27.7506167;120.65955,30.7708833;122.00615,30.1563667;121.6322333,29.8891667;120.6583167,30.7707;122.00244,30.15192;122.2778667,30.067;122.20657,29.69827;120.6341,27.7594333;120.6583167,30.7707667;120.65835,30.7704333;120.6582167,30.7706167;120.6584167,30.77015;120.6582833,30.7707667;120.6607167,30.7708167;120.6588333,30.7709;120.6588167,30.7709167;120.6588167,30.7709833;120.6587833,30.7708667;120.6591667,30.7706167;120.6588667,30.77095;120.6588,30.7709167;120.6587667,30.7708667;120.6588167,30.7709833;120.6587333,30.7709333;120.6589,30.7714833;120.6583833,30.7716833;120.6588667,30.7709667;120.6587833,30.7710167;120.6588,30.7708833;120.6588833,30.7709333;120.6595167,30.7704833;120.6582833,30.7707833;120.9478667,30.4453;120.64994,30.77205;120.6583,30.7707833;122.2433167,29.9580667;122.2672167,29.9466833;120.6583167,30.7705;120.6586333,30.77085;120.6583167,30.7707667;121.9061,29.1522667;120.6583,30.7707833;120.6583,30.7708;120.6583,30.7707833;120.55538,27.37433;120.7658,30.1051667;120.6583167,30.7707833;120.6589833,30.7705167;120.66304,30.77068;122.10649,29.19961;120.6583167,30.7707833;120.6587333,30.7708167;120.65815,30.7707333;120.65845,30.7702833;119.8379833,25.4650333;123.0494333,30.07635;120.9797,30.6003333;120.65875,30.7707;79.8655167,6.92235;121.0421667,30.5777333;120.6586333,30.7708833;120.6585,30.7706667;122.2995333,30.6591667;122.4048667,29.9496167;120.6588,30.77095;120.6588333,30.7709167;122.3126833,29.9411833;122.2580667,29.9472167;122.7876,30.13737;120.6583667,30.7708833;120.6589333,30.7710333;120.6880333,27.7582167;120.6588167,30.771;122.88211,30.97533;119.3541,39.7526667;122.1137,30.0217167;120.65835,30.7703333;122.3004,30.41638;125.38495,32.00465;122.20425,30.23865;120.6587333,30.7700167;120.65815,30.7708;122.29391,30.44883;122.2884,29.9421333;121.63525,29.88637;120.6718667,27.7318167;120.6582667,30.7707667;120.6582833,30.7707833;123.40705,30.3417333;121.4792667,31.33495;120.6588167,30.7709;120.6588,30.7708667;120.6588,30.7709667;120.6588333,30.7709333;120.6588333,30.7709667;120.6588167,30.7709167;120.6588333,30.7709333;120.6588167,30.7709;120.6588167,30.7709167;120.6588,30.7709333;120.6588333,30.7709333;120.6588333,30.7709333;120.6588,30.7709;120.65885,30.7709;120.6588,30.7709333;120.6588333,30.77095;120.6588333,30.77095;120.6588,30.7709;120.6588,30.7709167;120.6587833,30.7709;120.6588167,30.7709667;120.6588667,30.7709333;120.6588167,30.77095;120.6588333,30.77095";
//                String names = "浙瑞渔01396,浙瑞渔01395,浙瑞渔12170,浙瑞渔01256,浙瑞渔01255,浙椒渔休00003,浙岭渔运31011,浙岭渔23285,浙岭渔20356,浙岭渔运20065,浙岭渔运31012,浙岭渔78058,浙椒渔休00002,浙岭渔41019,浙岭渔78017,浙岭渔93055,浙岭渔23631,浙岭渔69085,浙岭渔68029,浙岭渔60095,浙岭渔25809,浙岭渔23233,浙岭渔69118,浙岭渔23493,浙岭渔52082,浙岭渔22868,浙岭渔60053,浙岭渔01011,浙岭渔运60098,浙岭渔21098,浙岭渔25810,浙岭渔58055,浙岭渔95018,浙岭渔92089,浙岭渔21903,浙岭渔78019,浙岭渔78018,浙岭渔20355,浙岭渔02055,浙岭渔78068,浙岭渔88038,浙岭渔69105,浙岭渔12155,浙岭渔93056,浙岭渔20531,浙岭渔02021,浙岭渔69112,浙岭渔运31078,浙岭渔运31071,浙岭渔28910,浙岭渔78099,浙岭渔58044,浙岭渔12186,浙岭渔69183,浙岭渔运10020,浙岭渔08031,浙岭渔58116,浙岭渔23725,浙岭渔02031,浙岭渔43025,浙岭渔68016,浙岭渔12192,浙岭渔28871,浙岭渔22825,浙岭渔18105,浙岭渔78051,浙岭渔28801,浙岭渔43036,浙岭渔58097,浙岭渔28872,浙岭渔28905,浙岭渔23379,浙岭渔22869,浙岭渔78015,浙岭渔27802,浙岭渔04038,浙岭渔78033,浙岭渔26908,浙岭渔23871,浙岭渔23665,浙岭渔23618,浙岭渔23872,浙岭渔23694,浙岭渔12156,浙岭渔28811,浙岭渔22823,浙岭渔23619,浙岭渔46052,浙岭渔21578,浙岭渔23851,浙岭渔01077,浙岭渔23592,浙岭渔68032,浙岭渔04008,浙岭渔23893,浙岭渔82069,浙岭渔21836,浙岭渔93022,浙岭渔94098,浙岭渔27801,浙岭渔22859,浙岭渔23281,浙岭渔23852,浙岭渔08058,浙岭渔01098,浙岭渔20365,浙岭渔68018,浙岭渔02098,浙岭渔23348,浙岭渔01088,浙岭渔68011,浙岭渔01058,浙岭渔21808,浙岭渔52092,浙岭渔29816,浙岭渔68015,浙岭渔82015,浙岭渔04018,浙岭渔45008,浙岭渔13066,浙岭渔41001,浙岭渔04059,浙岭渔运31083,浙岭渔08077,浙岭渔62035,浙岭渔28856,浙岭渔23168,浙岭渔04037,浙岭渔27885,浙岭渔21859,浙岭渔01031,浙岭渔23894,浙岭渔23605,浙岭渔00050,浙岭渔27882,浙岭渔23298,浙岭渔27858,浙岭渔23630,浙岭渔运20078,浙岭渔28881,浙岭渔28918,浙岭渔00037,浙岭渔运20033,浙岭渔94019,浙岭渔93085,浙岭渔23652,浙岭渔29725,浙岭渔29847,浙岭渔29807,浙岭渔69025,浙岭渔04058,浙岭渔23808,浙岭渔68030,浙岭渔23679,浙岭渔20263,浙岭渔29916,浙岭渔17087,浙岭渔21688,浙岭渔02009,浙岭渔26899,浙岭渔26919,浙岭渔29813,浙岭渔68001,浙岭渔46003,浙岭渔68023,浙岭渔08036,浙岭渔68003,浙岭渔29857,浙岭渔26866,浙岭渔19068,浙岭渔运31072,浙岭渔94009,浙岭渔00055,浙岭渔00089,浙岭渔23292,浙岭渔20516,浙岭渔23758,浙岭渔23757,浙岭渔41087,浙岭渔41022,浙岭渔68006,浙岭渔23726,浙岭渔41030,浙岭渔08002,浙岭渔08001,浙岭渔18028,浙岭渔00017,浙岭渔68019,浙岭渔20569,浙岭渔运31093,浙岭渔41069,浙岭渔17036,浙岭渔41068,浙岭渔20285,浙岭渔23347,浙岭渔95069,浙岭渔04069,浙岭渔68002,浙岭渔26867,浙岭渔68007";
//                String[] pointsArr = points.split(";");
//                String[] namesArr = names.split(",");
//                for (int j = 0; j < 200; j++) {
//                    try {
//                        int a = random.nextInt(10);
//                        String s = a + "";
//                        for (int i = 0; i < 8; i++) {
//                            s += random.nextInt(10);
//                        }
//                        OtherShipBean osb = new OtherShipBean();
//                        osb.setMmsi(Integer.valueOf(s));
//                        String[] point = pointsArr[j].split(",");
//                        int cog = random.nextInt(360);
//                        int sog = random.nextInt(10);
//                        int ship_id = skiaDrawView.mYimaLib.AddOtherVessel(true
//                                , (int) (Float.valueOf(point[0]) * 1e7), (int) (Float.valueOf(point[1]) * 1e7), cog, cog
//                                , 0, sog, 0);
//                        osb.setShip_id(ship_id);
//                        osb.setShip_name(namesArr[j]);
//                        db.save(osb);
//                    } catch (DbException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
//

        // 初始化本船位置

        // 初始化地图


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LocationBean lb = MyApplication.getInstance().getCurrentLocation();
                if (lb.getLatitude() != 0 && lb.getLongitude() != 0) {
                    if (myLocation == null) myLocation = new M_POINT();
                    myLocation.x = lb.getLongitude();
                    myLocation.y = lb.getLatitude();
                    setOwnShip(myLocation, lb.getHeading(), false);
                    updateShipInfo(lb);
//                    skiaDrawView.mYimaLib.CenterMap(myLocation.x, myLocation.y);
                } else {
                    // 没有位置则固定中心点 121.768783,28.696902
                    skiaDrawView.mYimaLib.CenterMap((int) (121.768783 * 1e7), (int) (28.696902 * 1e7));
                }
                skiaDrawView.mYimaLib.SetCurrentScale(8878176.0f);
                skiaDrawView.postInvalidate();
            }
        }, 200);
        return view;
    }

    @Override
    public void onResume() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                skiaDrawView.changeFishState(mSwitchYQ.isChecked());
                skiaDrawView.postInvalidate();
//                showWarnAreas();
                if (mSwitchPin.isChecked()) {
                    showBiaoweis();
                }
//                mSwitchPin.setChecked(false);
            }
        }, 50);
        super.onResume();
    }

    private void showBiaoweis() {
        try {
            List<PinBean> list = db.selector(PinBean.class).findAll();
            if (list != null && !list.isEmpty()) skiaDrawView.showBiaoWei(list);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private final List<WarnArea> areas = new ArrayList<>();
    private int curLayerPos = -1;


    private void showWarnAreas() {
        if (warnArea) {
            Integer[] geoX = new Integer[JIN_CHU_AREA_X.length];
            Integer[] geoY = new Integer[JIN_CHU_AREA_Y.length];
            for (int i = 0; i < JIN_CHU_AREA_X.length; i++) {
                geoX[i] = JIN_CHU_AREA_X[i];
                geoY[i] = JIN_CHU_AREA_Y[i];
            }
            areas.add(new WarnArea(2, JIN_CHU_AREA_COLOR[0], JIN_CHU_AREA_COLOR[1]
                    , JIN_CHU_AREA_COLOR[2], geoX, geoY));
//            geoX = new Integer[JIN_RU_AREA_X.length];
//            geoY = new Integer[JIN_RU_AREA_Y.length];
//            for (int i = 0; i < JIN_RU_AREA_X.length; i++) {
//                geoX[i] = JIN_RU_AREA_X[i];
//                geoY[i] = JIN_RU_AREA_Y[i];
//            }
//            areas.add(new WarnArea(1, JIN_RU_AREA_COLOR[0], JIN_RU_AREA_COLOR[1]
//                    , JIN_RU_AREA_COLOR[2], geoX, geoY));
//            geoX = new Integer[JIN_YU_AREA_X.length];
//            geoY = new Integer[JIN_YU_AREA_Y.length];
//            for (int i = 0; i < JIN_YU_AREA_X.length; i++) {
//                geoX[i] = JIN_YU_AREA_X[i];
//                geoY[i] = JIN_YU_AREA_Y[i];
//            }
//            areas.add(new WarnArea(0, JIN_YU_AREA_COLOR[0], JIN_YU_AREA_COLOR[1]
//                    , JIN_YU_AREA_COLOR[2], geoX, geoY));
            curLayerPos = skiaDrawView.drawBanArea(areas);
        } else {
            for (WarnArea area : areas) {
                if (area == null) continue;
                skiaDrawView.mYimaLib.tmDeleteGeoObject(curLayerPos, area.getObjCount());
            }
            skiaDrawView.mYimaLib.tmClearLayer(curLayerPos);
            skiaDrawView.mYimaLib.tmDeleteLayer(curLayerPos);
            skiaDrawView.postInvalidate();
            areas.clear();
        }
    }

    private void loadOwnShipInfo() {
        final SharedPreferences sp = getActivity().getSharedPreferences("xkTerminal", MODE_PRIVATE);
        skiaDrawView.mYimaLib.SetOwnShipBasicInfo(sp.getString("shipName", "")
                , sp.getString("shipNo", "")
                , Float.valueOf(sp.getString("shipLength", "0"))
                , Float.valueOf(sp.getString("shipDeep", "0")));
    }


    public void showMainLayout() {
        alert_need_flash = false;
        SoundPlay.stopAlertSound();
        main_layout.setVisibility(View.VISIBLE);
        alert_layout.setVisibility(View.GONE);
    }

    private void showAlertLayout() {
        main_layout.setVisibility(View.GONE);
        alert_layout.setVisibility(View.VISIBLE);
        alert_need_flash = true;
        if (Constant.ALERT_FLASH_TIME != 0) {
            alert_tv.setVisibility(View.INVISIBLE);
            new TimeHandler().start();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SmsEvent event) {
        JSONObject receiveJson = event.getReceiveJson();
        try {
            String apiType = receiveJson.getString("apiType");
            switch (apiType) {
                case "showAlertInHomePage":
                    if (alert_layout.getVisibility() == View.VISIBLE) {
                        return;
                    }
                    PreferencesUtils.putBoolean(getActivity(), "homePageAlertView", true);
                    showAlertLayout();
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void onDestroy() {
        super.onDestroy();
        alert_need_flash = false;
        EventBus.getDefault().unregister(this);
    }


    class TimeHandler extends Thread {
        @Override
        public void run() {
            super.run();
            int i = 0;
            do {

                try {
                    Thread.sleep(100);
                } catch (Exception e) {

                }

                if (Constant.ALERT_FLASH_TIME != 0) {
                    int flashTime = Constant.ALERT_FLASH_TIME / 100;
                    if (i % flashTime == 0) {
                        Message message = new Message();
                        message.what = 0;
                        handler.sendMessage(message);
                    }
                    if (i == flashTime) i = 0;
                }

                i++;
            } while (alert_need_flash);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    alert_tv.setVisibility(alert_tv.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 解除报警，用户手动解除或者北斗中心解除
     */
    public void cancelAlert() {
        new QMUIDialog.MessageDialogBuilder(getActivity())
                .setTitle("解除报警")
                .setMessage("确定要解除吗？")
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(0, "解除", QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {

                        // 解除报警操作
                        ((MyApplication) getActivity().getApplication()).sendBytes(AlertFormat.format("00010000", "00000000"));
                        PreferencesUtils.putBoolean(getActivity(), "homePageAlertView", false);
                        showMainLayout();

                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * 确认报警
     */
    private void confirmAlert() {
        PreferencesUtils.putBoolean(getActivity(), "homePageAlertView", false);
        showMainLayout();
        PreferencesUtils.putBoolean(getActivity(), "flashAlert", true);
    }


    private M_POINT myLocation;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationEvent(LocationBean locationBean) {
        LocationBean lb = (LocationBean) locationBean;
//        Log.i("TAG", "onLocationEvent: 收到自身位置");
//        Log.i("TAG", "onLocationEvent: 纬度lat:" + lb.getLatitude());
//        Log.i("TAG", "onLocationEvent: 经度lon:" + lb.getLongitude());
        if (myLocation == null) {
            myLocation = new M_POINT();
            myLocation.x = lb.getLongitude();
            myLocation.y = lb.getLatitude();
            skiaDrawView.mYimaLib.CenterMap(myLocation.x, myLocation.y);
        } else {
            myLocation.x = lb.getLongitude();
            myLocation.y = lb.getLatitude();
        }

        // 根据每次gps信息更新位置
        setOwnShip(myLocation, lb.getHeading(), false);

        // 更新框
        updateShipInfo(lb);

        doAreaWarning(lb);
    }

    /**
     * 检测报警
     *
     * @param lb
     */
    private void doAreaWarning(LocationBean lb) {
        if (warnArea) {// 开启了区域报警
            for (WarnArea area : areas) {
                List<Integer> geoX = area.getGeoX();
                List<Integer> geoY = area.getGeoY();
                int size = geoX.size();
                int[] arrX = new int[size];
                int[] arrY = new int[size];
                for (int i = 0; i < size; i++) {
                    arrX[i] = geoX.get(i);
                    arrY[i] = geoY.get(i);
                }
                boolean exists = skiaDrawView.mYimaLib.IsPointInFace(lb.getLongitude(), lb.getLatitude(),
                        arrX, arrY, size);
                switch (area.getType()) {
                    case 0:// 禁渔
                        if (exists) {
                            // 报警
                            MyApplication.getInstance().mainActivity.showMessageDialog("禁渔区域报警", 1);
                            SoundPlay.startAlertSound(MyApplication.getInstance().mainActivity);
                        }
                        break;
                    case 1:// 禁入
                        if (exists) {
                            // 报警
                            MyApplication.getInstance().mainActivity.showMessageDialog("禁入区域报警", 1);
                            SoundPlay.startAlertSound(MyApplication.getInstance().mainActivity);
                        }
                        break;
                    case 2:// 禁出
                        if (!exists) {
                            // 报警
                            MyApplication.getInstance().mainActivity.showMessageDialog("禁出区域报警", 1);
                            SoundPlay.startAlertSound(MyApplication.getInstance().mainActivity);
                        }
                        break;
                    default:
                        break;
                }
            }

        }
    }

    @Subscribe
    public void onAisEvent(AisInfo aisInfo) {
        if (aisInfo == null) return;
        int mmsi = aisInfo.mmsi;
//        try {
        //OtherShipBean osb = db.selector(OtherShipBean.class).where("mmsi", "=", mmsi).findFirst();
        OtherShipBean osb = null;
        for (OtherShipBean d : MyApplication.osbDataList) {
            if (d.getMmsi() == mmsi) {
                osb = d;
                break;
            }
        }
        if (osb != null) {
            osb.setAcq_time(Constant.SYSTEM_DATE);
            osb.setLongitude(aisInfo.longtitude);
            osb.setLatitude(aisInfo.latititude);
            osb.setCog(aisInfo.COG);
            osb.setSog(aisInfo.SOG);
        } else {
            // 不存在， 新增
            osb = new OtherShipBean();
            osb.setMmsi(mmsi);
//                ship_id = skiaDrawView.mYimaLib.AddOtherVessel(true
//                        , aisInfo.longtitude, aisInfo.latititude, aisInfo.COG, aisInfo.COG
//                        , 0, aisInfo.SOG, 0);
//                osb.setShip_id(ship_id);
            osb.setShip_name(aisInfo.shipName);
            osb.setAcq_time(Constant.SYSTEM_DATE);
            osb.setLongitude(aisInfo.longtitude);
            osb.setLatitude(aisInfo.latititude);
            osb.setCog(aisInfo.COG);
            osb.setSog(aisInfo.SOG);
            //db.saveBindingId(osb);
            MyApplication.osbDataList.add(osb);
        }
//            int ship_id = -1;
//            if (osb == null) {
//
//            } else {
//                int versselId = skiaDrawView.mYimaLib.GetOtherVesselPosOfID(osb.getShip_id());
//                if (-1 == versselId) {
//                    ship_id = skiaDrawView.mYimaLib.AddOtherVessel(true
//                            , aisInfo.longtitude, aisInfo.latititude, aisInfo.COG, aisInfo.COG
//                            , 0, aisInfo.SOG, 0);
//                    osb.setShip_id(ship_id);
//                } else {
//                    skiaDrawView.mYimaLib.SetOtherVesselCurrentInfo(versselId
//                            , aisInfo.longtitude, aisInfo.latititude, aisInfo.COG, aisInfo.COG
//                            , 0, aisInfo.SOG, 0);
//                }
//                osb.setAcq_time(Constant.SYSTEM_DATE);
//                //db.saveOrUpdate(osb);
//                // 存在， 更新信息
//                Log.e("TAG", "mmsi: " + aisInfo.mmsi+", ship_id: " + osb.getShip_id()+ ", versselId: " + versselId+ ", old : " + aisInfo.longtitude + ", " + aisInfo.latititude);
//            }
//            // 显示所有船
//            skiaDrawView.mYimaLib.SetAllOtherVesselDrawOrNot(true);
//            skiaDrawView.postInvalidate();

        if (showOtherShip) {
            updateOtherShipsInfo();
        }
//            Log.e("TAG", aisInfo.mmsi + ", " + aisInfo.longtitude + ", " + aisInfo.latititude);
//                String str = String.format("mmsi:{0},msgType:{1},shipName:{2},cog:{3},sog:{4}", aisInfo.mmsi, aisInfo.MsgType,aisInfo.shipName, aisInfo.COG
//                , aisInfo.SOG);
//                datas.add(str);
//                aisInfoAdapter.notifyDataSetChanged();
//        } catch (DbException e) {
//            e.printStackTrace();
//        }
    }

    private boolean showOtherShip = false;

    @Subscribe
    public void onOpenEvent(String action) {
        if ("openShip".equals(action)) {

            updateOtherShipsInfo();

            if (ll_ship_list.getVisibility() == View.GONE) {
                ll_ship_list.setVisibility(View.VISIBLE);
                showOtherShip = true;
            }
        } else if ("openShip2".equals(action)) {
            if (showOtherShip) {
                updateOtherShipsInfo();
            }
        } else if ("pin_map".equals(action)) {
            doBiaoWei = true;
            showOtherShip = false;
            Toast.makeText(getActivity(), "请选择标位点", Toast.LENGTH_SHORT).show();
        } else if ("pin_co".equals(action)) {
            openPinDialog(null);
        }
    }

    private void updateOtherShipsInfo() {
//        try {
//            datas.clear();
//            final SharedPreferences sp = getActivity().getSharedPreferences("xkTerminal", MODE_PRIVATE);
//            List<OtherShipBean> list = db.selector(OtherShipBean.class).where("mmsi", "<>", sp.getString("shipNo", "")).findAll();
//            if (list != null && !list.isEmpty()) {
//                datas.addAll(list);
//            }
        skiaDrawView.mYimaLib.ClearOtherVessels();
        for (OtherShipBean osb : MyApplication.osbDataList) {
            int ship_id = skiaDrawView.mYimaLib.AddOtherVessel(true
                    , osb.getLongitude(), osb.getLatitude(), osb.getCog(), osb.getCog()
                    , 0, osb.getSog(), 0);
            osb.setShip_id(ship_id);
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rvShipAdapter.notifyDataSetChanged();
            }
        });

//        } catch (DbException e) {
//            e.printStackTrace();
//        }
    }

    // 标位操作
    private boolean doBiaoWei = false;

    @Override
    public void onMapClicked(M_POINT m_point) {
        if (showOtherShip) {
            if (ll_ship_list.getVisibility() == View.VISIBLE) {
                ll_ship_list.setVisibility(View.GONE);
                showOtherShip = false;
            }
        } else if (doBiaoWei) {
            doBiaoWei = false;
            openPinDialog(m_point);
        }
    }

    /**
     * 标位对话框 输入内容
     *
     * @param m_point
     */
    private void openPinDialog(M_POINT m_point) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_biaowei, null);
        final QHTitleView title = view.findViewById(R.id.title);
        title.setTitle("标位设置");
        title.setClickCallback(new QHTitleView.ClickCallback() {
            @Override
            public void onBackClick() {
                //
            }

            @Override
            public void onRightClick() {
                //
            }
        });

        final EditText et1 = view.findViewById(R.id.et1);
        final EditText et2 = view.findViewById(R.id.et2);
        final EditText et3 = view.findViewById(R.id.et3);
        final EditText et4 = view.findViewById(R.id.et4); // 用来存显色标志 1 2 3 4隐藏

        if (m_point != null) {
            et1.setText(m_point.x / 1e7 + "");
            et2.setText(m_point.y / 1e7 + "");
        }

        final TextView tv1 = view.findViewById(R.id.tv1);
        final TextView tv2 = view.findViewById(R.id.tv2);
        final TextView tv3 = view.findViewById(R.id.tv3);
        final TextView tv4 = view.findViewById(R.id.tv4);

        final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(30, 30);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        final RelativeLayout.LayoutParams layoutParams_big = new RelativeLayout.LayoutParams(40, 40);
        layoutParams_big.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et4.setText("1");
                tv1.setLayoutParams(layoutParams_big);
                tv2.setLayoutParams(layoutParams);
                tv3.setLayoutParams(layoutParams);
                tv4.setLayoutParams(layoutParams);
            }
        });

        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et4.setText("2");
                tv1.setLayoutParams(layoutParams);
                tv2.setLayoutParams(layoutParams_big);
                tv3.setLayoutParams(layoutParams);
                tv4.setLayoutParams(layoutParams);
            }
        });

        tv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et4.setText("3");
                tv1.setLayoutParams(layoutParams);
                tv2.setLayoutParams(layoutParams);
                tv3.setLayoutParams(layoutParams_big);
                tv4.setLayoutParams(layoutParams);
            }
        });

        tv4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et4.setText("4");
                tv1.setLayoutParams(layoutParams);
                tv2.setLayoutParams(layoutParams);
                tv3.setLayoutParams(layoutParams);
                tv4.setLayoutParams(layoutParams_big);
            }
        });

        final Button b1 = view.findViewById(R.id.btn1);//确定
        final Button b2 = view.findViewById(R.id.btn2);//取消

        final AlertDialog pinDialog = new AlertDialog.Builder(getActivity()).setView(view).setCancelable(false).create();

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = et3.getText().toString();
                String lonStr = et1.getText().toString();
                String latStr = et2.getText().toString();

                try {
                    int lon = (int) (Float.parseFloat(lonStr) * 1e7);
                    int lat = (int) (Float.parseFloat(latStr) * 1e7);
                    PinBean pinBean = new PinBean();
                    pinBean.setColor(getResources().getColor(android.R.color.black));
                    pinBean.setLat(lat);
                    pinBean.setLon(lon);
                    pinBean.setName(name);
                    switch (et4.getText().toString()) {
                        case "1":
                            pinBean.setColor(getResources().getColor(android.R.color.holo_red_dark));
                            break;
                        case "2":
                            pinBean.setColor(getResources().getColor(android.R.color.holo_green_dark));
                            break;
                        case "3":
                            pinBean.setColor(getResources().getColor(android.R.color.holo_blue_dark));
                            break;
                        case "4":
                            pinBean.setColor(getResources().getColor(android.R.color.holo_orange_dark));
                            break;
                        default:
                            pinBean.setColor(getResources().getColor(android.R.color.holo_red_dark));
                            break;
                    }
                    try {
                        db.saveBindingId(pinBean);
                        pinDialog.cancel();
                        Toast.makeText(getActivity(), "标位保存成功", Toast.LENGTH_SHORT).show();
                        showBiaoweis();
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.getStackTrace();
                    Toast.makeText(getActivity(), "请输入正确内容", Toast.LENGTH_SHORT).show();
                }
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pinDialog.cancel();
            }
        });

        pinDialog.show();
    }

    @Override
    public void onMapTouched(int action) {

    }

    /**
     * 设置本船
     */
    public void setOwnShip(M_POINT m_point, float heading, boolean rotateScreen) {
//        skiaDrawView.mYimaLib.SetOwnShipBasicInfo("本船", "123456789", 100, 50);
        skiaDrawView.mYimaLib.SetOwnShipCurrentInfo(m_point.x, m_point.y, heading, 50, 50, 0, 0);
        skiaDrawView.mYimaLib.SetOwnShipShowSymbol(false, 4, true, 16, 5000000);
        if (!showOtherShip) {
            skiaDrawView.mYimaLib.RotateMapByScrnCenter(rotateScreen ? 0 - heading : 0);
            skiaDrawView.mYimaLib.CenterMap(myLocation.x, myLocation.y);
        }
        skiaDrawView.postInvalidate();

    }

    /**
     * 更新GPS状态框
     *
     * @param locationBean
     */
    private void updateShipInfo(LocationBean locationBean) {
        tv_lon.setText(GPSFormatUtils.DDtoDMS(locationBean.getLongitude() / 10000000d, true));
        tv_lat.setText(GPSFormatUtils.DDtoDMS(locationBean.getLatitude() / 10000000d, false));
        tv_head.setText(locationBean.getHeading() + "°");
        tv_speed.setText(String.format("%.1f", locationBean.getSpeed()) + "kn");
    }

}
