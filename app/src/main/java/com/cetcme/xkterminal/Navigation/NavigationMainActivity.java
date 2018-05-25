package com.cetcme.xkterminal.Navigation;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;

import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.Sqlite.Bean.LocationBean;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ResourceUtil;
import com.joanzapata.iconify.widget.IconTextView;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;

import org.xutils.DbManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import yimamapapi.skia.M_POINT;
import yimamapapi.skia.YimaLib;

public class NavigationMainActivity extends AppCompatActivity implements SkiaDrawView.OnMapClickListener, View.OnClickListener {


    private SkiaDrawView fMainView;
    private Toast toast;

    int routeID = -1;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private IconTextView mITVMenu;
    private AppCompatButton mClearTrack;
    private LinearLayout mLlBottom;
    private DbManager db = MyApplication.getInstance().getDb();
    private int type = 0;

    private LinearLayout llNavigator;
    private Button btnNaviCancel;
    private Button btnNaviGo;
    private String routeFileName;

    private final int[] JIN_YU_AREA_COLOR = new int[]{10, 255, 20};
    private final int[] JIN_RU_AREA_COLOR = new int[]{150, 255, 20};
    private final int[] JIN_CHU_AREA_COLOR = new int[]{100, 255, 20};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//
        //设置全屏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_navigation_main);
        getSupportActionBar().hide();
        fMainView = findViewById(R.id.skiaView);

        fMainView.setOnMapClickListener(this);
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        mITVMenu = findViewById(R.id.id_menu);
        mITVMenu.setOnClickListener(this);
        mClearTrack = findViewById(R.id.clearTrack);
        mClearTrack.setOnClickListener(this);
        mLlBottom = findViewById(R.id.ly_bottom);
        mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);

        llNavigator = findViewById(R.id.ll_navigator);
        btnNaviCancel = findViewById(R.id.navi_cancel);
        btnNaviGo = findViewById(R.id.navi_go);
        btnNaviCancel.setOnClickListener(this);
        btnNaviGo.setOnClickListener(this);

        fMainView.drawBanArea(JIN_YU_AREA_COLOR[0], JIN_YU_AREA_COLOR[1], JIN_YU_AREA_COLOR[2]
                , new int[]{1211590000, 1211740000, 1211790000, 1211680000, 1211590000}
                , new int[]{278000000,278030000, 277940000, 277910000, 277920000 });
    }

    /**
     * 放大地图
     *
     * @param view
     */
    public void ZoomInClick_Event(View view) {
        System.out.println("放大");
        fMainView.mYimaLib.SetCurrentScale(fMainView.mYimaLib.GetCurrentScale() / 2);
        fMainView.postInvalidate();//刷新fMainView
    }

    /**
     * 缩小地图
     *
     * @param view
     */
    public void ZoomOutClick_Event(View view) {
        System.out.println("缩小");
        fMainView.mYimaLib.SetCurrentScale(fMainView.mYimaLib.GetCurrentScale() * 2);
        fMainView.postInvalidate();//刷新fMainView
    }

    /**
     * 取消上一个路径点
     *
     * @param view
     */
    public void PointCancel_Event(View view) {
        if (routeID != -1) {
            int count = fMainView.mYimaLib.GetRouteWayPointsCount(routeID);
            int[] ids = fMainView.mYimaLib.GetRouteWayPointsID(routeID);

            if (count == 0) {
                return;
            }
            fMainView.mYimaLib.DeleteRouteWayPoint(routeID, count - 1, 1);
            fMainView.mYimaLib.DeleteWayPoint(ids[ids.length - 1]);
            fMainView.postInvalidate();
        }
    }

    /**
     * 保存路径到文件
     *
     * @param view
     */
    public void RouteSave_Event(View view) {
        if (routeID == -1) {
            toast.setText("没有路径点");
            toast.show();
            return;
        }

        PermissionUtil.verifyStoragePermissions(this);

        File filePath = new File(Constant.ROUTE_FILE_PATH);
        if (!filePath.exists()) {
            filePath.mkdir();
        }

        long timestamp = new Date().getTime();
        boolean saveOk = fMainView.mYimaLib.SaveRoutesToFile(Constant.ROUTE_FILE_PATH + "/" + timestamp);

        if (saveOk) {
            toast.setText("保存成功: " + timestamp);
            toast.show();
            mLlBottom.setVisibility(View.GONE);
            clearRoute();
            startActivityForResult(new Intent(this, RouteListActivity.class), 0);
        }
    }

    /**
     * 打开路径清单activity
     *
     * @param view
     */
//    public void RouteList_Event(View view) {
//        startActivityForResult(new Intent(this, RouteListActivity.class), 0);
//    }

    /**
     * 打开导航activity
     *
     * @param view
     */
//    public void Navigation_Event(View view) {
//        startActivity(new Intent(this, NavigationActivity.class));
//    }


    /**
     * 清屏
     *
     * @param view
     */
    public void ClearRoute_Event(View view) {
        clearRoute();
    }

    /**
     * 将物理坐标变成整形
     *
     * @param location
     * @return
     */
    private int getLocationInt(double location) {
        return (int) (location * 10000000);
    }

    /**
     * 设置路径
     *
     * @param m_point
     */
    @Override
    public void onMapClicked(M_POINT m_point) {
        if (type == 1) {
            double x = m_point.x / 10000000f;
            double y = m_point.y / 10000000f;

            toast.setText(String.format("x: %.3f, y: %.3f", x, y));
            toast.show();

            if (routeID == -1) {
                routeID = fMainView.mYimaLib.AddRoute("航线", new int[]{}, 0, true);
                fMainView.mYimaLib.SetPointSelectJudgeDist(30, 15);
            }
            int wp = fMainView.mYimaLib.AddWayPoint(m_point.x, m_point.y, "1", 20, "1");
            int wpCount = fMainView.mYimaLib.GetRouteWayPointsCount(routeID);
            fMainView.mYimaLib.AddRouteWayPoint(routeID, wpCount, new int[]{wp}, 1);
        }
    }

    @Override
    public void onMapTouched(int action) {

    }

    /**
     * 接收RouterListActivity的返回
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != 0) {
            return;
        }
        switch (resultCode) {
            case RouteListActivity.ACTIVITY_RESULT_ROUTE_SHOW:
                type = 0;
                llNavigator.setVisibility(View.VISIBLE);
                clearRoute();
                routeFileName = data.getStringExtra("fileName");
                Log.i(TAG, "load file: start");

                fMainView.mYimaLib.AddRoutesFromFile(Constant.ROUTE_FILE_PATH + "/" + routeFileName);
                int routeCount = fMainView.mYimaLib.GetRoutesCount();
                routeID = fMainView.mYimaLib.GetRouteIDFromPos(routeCount - 1);

                Log.i(TAG, "GetRoutesCount: " + fMainView.mYimaLib.GetRoutesCount());
                Log.i(TAG, "routeID: " + routeID);
                Log.i(TAG, "load file: end");
                Log.i(TAG, "==========================");
                break;
            case RouteListActivity.ACTIVITY_RESULT_ROUTE_ADD:
                type = 1;
                clearRoute();
                mLlBottom.setVisibility(View.VISIBLE);
                break;
            case RouteListActivity.ACTIVITY_RESULT_ROUTE_NOTHING:
                type = 0;
                break;
        }
    }

    private String TAG = "debug-qh";

    private void clearRoute() {
        Log.i(TAG, "==========================");

        Log.i(TAG, "clearRoute: start");
        // 如果有路径，则清除
        if (routeID != -1) {
            Log.i(TAG, "clearRoute: " + routeID);

            int[] ids = fMainView.mYimaLib.GetRouteWayPointsID(routeID);
            fMainView.mYimaLib.DeleteRouteWayPoint(routeID, 0, ids.length); // 必须在调用DeleteWayPoint之前
            for (int id : ids) {
                fMainView.mYimaLib.DeleteWayPoint(id);
            }
        }

        fMainView.postInvalidate();
        Log.i(TAG, "clearRoute: end");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.id_menu:
                createMenu();
                break;
            case R.id.tv_own_ship_info:// 本船信息
                final SharedPreferences sp = getSharedPreferences("xkTerminal", MODE_PRIVATE);
                final View contentView = getLayoutInflater().inflate(R.layout.dialog_own_ship_info, null);
                final EditText mEt1 = contentView.findViewById(R.id.et_ship_name);
                final EditText mEt2 = contentView.findViewById(R.id.et_ship_no);
                final EditText mEt3 = contentView.findViewById(R.id.et_ship_length);
                final EditText mEt4 = contentView.findViewById(R.id.et_ship_deep);
                mEt1.setText(sp.getString("shipName", ""));
                mEt2.setText(sp.getString("shipNo", ""));
                mEt3.setText(sp.getString("shipLength", ""));
                mEt4.setText(sp.getString("shipDeep", ""));
                new AlertDialog.Builder(this)
                        .setView(contentView)
                        .setCancelable(false)
                        .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                boolean success = sp.edit()
                                        .putString("shipName", mEt1.getText().toString().trim())
                                        .putString("shipNo", mEt2.getText().toString().trim())
                                        .putString("shipLength", mEt3.getText().toString().trim())
                                        .putString("shipDeep", mEt4.getText().toString().trim())
                                        .commit();
                                if (success) {
                                    Toast.makeText(NavigationMainActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(NavigationMainActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                                }
                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                menu.dismiss();
                break;
            case R.id.tv_ship_track:// 航迹查看
                final View trackView = getLayoutInflater().inflate(R.layout.dialog_ship_track, null);
                final AppCompatEditText start = trackView.findViewById(R.id.acet_start);
                final AppCompatEditText end = trackView.findViewById(R.id.acet_end);
                start.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final TimePickerView picker = new TimePickerBuilder(NavigationMainActivity.this, new OnTimeSelectListener() {
                            @Override
                            public void onTimeSelect(Date date, View v) {
                                start.setText(sdf.format(date));
                            }
                        })
                                .setType(new boolean[]{true, true, true, true, true, true})
                                .isDialog(true)
                                .build();
                        picker.show();
                    }
                });
                end.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final TimePickerView picker = new TimePickerBuilder(NavigationMainActivity.this, new OnTimeSelectListener() {
                            @Override
                            public void onTimeSelect(Date date, View v) {
                                end.setText(sdf.format(date));
                            }
                        })
                                .setType(new boolean[]{true, true, true, true, true, true})
                                .isDialog(true)
                                .build();
                        picker.show();
                    }
                });
                new AlertDialog.Builder(this)
                        .setView(trackView)
                        .setCancelable(false)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String startTime = start.getText().toString().trim();
                                String endTime = end.getText().toString().trim();
                                if (TextUtils.isEmpty(startTime)) {
                                    Toast.makeText(NavigationMainActivity.this, "开始时间不能为空", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (TextUtils.isEmpty(endTime)) {
                                    Toast.makeText(NavigationMainActivity.this, "结束时间不能为空", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                try {
                                    List<LocationBean> list = db.selector(LocationBean.class)
                                            .where("acqtime", ">=", sdf.parse(startTime).getTime())
                                            .and("acqtime", "<=", sdf.parse(endTime).getTime())
                                            .orderBy("acqtime")
                                            .findAll();
                                    if (list == null || list.isEmpty()) {
                                        Toast.makeText(NavigationMainActivity.this, "未查询到相关轨迹信息", Toast.LENGTH_SHORT).show();
                                    } else {
                                        fMainView.AddLineLayerAndObject(list);
                                        mClearTrack.setVisibility(View.VISIBLE);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(NavigationMainActivity.this, "未查询到相关轨迹信息", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                menu.dismiss();
                break;
            case R.id.clearTrack:
                fMainView.clearTrack();
                mClearTrack.setVisibility(View.GONE);
                break;
            case R.id.tv_route:
                type = 1;
                startActivityForResult(new Intent(this, RouteListActivity.class), 0);
                menu.dismiss();
                break;
            case R.id.tv_navigator:
                startActivity(new Intent(this, NavigationActivity.class));
                menu.dismiss();
                break;
            case R.id.navi_cancel:
                llNavigator.setVisibility(View.GONE);
                clearRoute();
                startActivityForResult(new Intent(this, RouteListActivity.class), 0);
                break;
            case R.id.navi_go:
                Intent intent = new Intent( this, NavigationActivity.class);
                intent.putExtra("routeFileName", routeFileName);
                startActivity(intent);
                break;
            default:
        }
    }


    private PopupWindow menu;

    private void createMenu() {
        menu = new PopupWindow(this);
        final View contentView = getLayoutInflater().inflate(R.layout.popup_menu, null);
        buildFunction(contentView);
        menu.setOutsideTouchable(true);
        menu.setContentView(contentView);
        menu.setHeight(QMUIDisplayHelper.getScreenHeight(this));
        menu.setWidth(QMUIDisplayHelper.getScreenWidth(this) * 3 / 10);
        final View rootView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        menu.showAtLocation(rootView, Gravity.LEFT, 0, 0);
    }

    private void buildFunction(View contentView) {
        // 本船信息
        final LinearLayout tvOwnShipInfo = contentView.findViewById(R.id.tv_own_ship_info);
        tvOwnShipInfo.setOnClickListener(this);
        // 航迹
        final LinearLayout tvShipTrack = contentView.findViewById(R.id.tv_ship_track);
        tvShipTrack.setOnClickListener(this);
        // 渔区控制
        final CheckBox checkBox = contentView.findViewById(R.id.id_enable_fish_area);
        checkBox.setChecked(fMainView.getFishState());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                fMainView.changeFishState(b);
                //menu.dismiss();
            }
        });
        final LinearLayout tvRoute = contentView.findViewById(R.id.tv_route);
        tvRoute.setOnClickListener(this);
        final LinearLayout tvNavigator = contentView.findViewById(R.id.tv_navigator);
        tvNavigator.setOnClickListener(this);
    }


    // 语音合成对象
    private static SpeechSynthesizer mTts;

    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d("TAG", "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText(MyApplication.getInstance(), "初始化失败,错误码：" + code, Toast.LENGTH_SHORT).show();
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
                // play("我打开了海图导航");
            }
        }
    };


    /**
     * 参数设置
     *
     * @return
     */
    private static void setParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);

        //设置使用本地引擎
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        //设置发音人资源路径
        mTts.setParameter(ResourceUtil.TTS_RES_PATH, getResourcePath());
        //设置发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, "50");
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, "50");
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");

        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }

    //获取发音人资源路径
    private static String getResourcePath() {
        StringBuffer tempBuffer = new StringBuffer();
        //合成通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(MyApplication.getInstance(), ResourceUtil.RESOURCE_TYPE.assets, "tts/common.jet"));
        tempBuffer.append(";");
        //发音人资源
        tempBuffer.append(ResourceUtil.generateResourcePath(MyApplication.getInstance(), ResourceUtil.RESOURCE_TYPE.assets, "tts/xiaoyan.jet"));
        return tempBuffer.toString();
    }

    public static void play(String text) {
        if (mTts == null) return;
        setParam();
        int code = mTts.startSpeaking(text, new SynthesizerListener() {

            @Override
            public void onSpeakBegin() {
                //showTip("开始播放");
            }

            @Override
            public void onSpeakPaused() {
                //showTip("暂停播放");
            }

            @Override
            public void onSpeakResumed() {
                //showTip("继续播放");
            }

            @Override
            public void onBufferProgress(int percent, int beginPos, int endPos,
                                         String info) {
            }

            @Override
            public void onSpeakProgress(int percent, int beginPos, int endPos) {

            }

            @Override
            public void onCompleted(SpeechError error) {
                if (error == null) {
                    //showTip("播放完成");
                } else if (error != null) {
                    //showTip(error.getPlainDescription(true));
                }
            }

            @Override
            public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
                // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
                // 若使用本地能力，会话id为null
                //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
                //		Log.d(TAG, "session id =" + sid);
                //	}
            }
        });
//			/**
//			 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
//			 * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
//			*/
//			String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
//			int code = mTts.synthesizeToUri(text, path, mTtsListener);

        if (code != ErrorCode.SUCCESS) {
            //showTip("语音合成失败,错误码: " + code);
        }
    }

    public void cancel() {
        if (mTts == null) return;
        mTts.stopSpeaking();
    }

    public void stop() {
        if (mTts == null) return;
        mTts.pauseSpeaking();
    }

    public void resume() {
        if (mTts == null) return;
        mTts.resumeSpeaking();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mTts) {
            mTts.stopSpeaking();
            // 退出时释放连接
            mTts.destroy();
        }
    }

    public void Back_Event(View view) {
        finish();
    }
}
