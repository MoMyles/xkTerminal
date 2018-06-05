package com.cetcme.xkterminal.Navigation;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.Sqlite.Bean.LocationBean;
import com.joanzapata.iconify.widget.IconTextView;
import com.qiuhong.qhlibrary.QHTitleView.QHTitleView;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import yimamapapi.skia.M_POINT;

public class NavigationMainActivity extends AppCompatActivity implements SkiaDrawView.OnMapClickListener, View.OnClickListener {


    private SkiaDrawView fMainView;
    private Toast toast;

    int routeID = -1;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private IconTextView mITVMenu;
    private AppCompatButton mClearTrack;
    private AppCompatButton mListTrack;
    private LinearLayout mLlBottom;
    private DbManager db = MyApplication.getInstance().getDb();
    private int type = 0;

    private LinearLayout llNavigator;
    private Button btnNaviCancel;
    private Button btnNaviGo;
    private String routeFileName;

    private static final int REQUSET_CODE_HANGXIAN = 0;
    private static final int REQUSET_CODE_HANGJI = 1;
    private final List<LocationBean> hangjiList = new ArrayList<>();

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
        mListTrack = findViewById(R.id.listTrack);
        mListTrack.setOnClickListener(this);
        mLlBottom = findViewById(R.id.ly_bottom);


        llNavigator = findViewById(R.id.ll_navigator);
        btnNaviCancel = findViewById(R.id.navi_cancel);
        btnNaviGo = findViewById(R.id.navi_go);
        btnNaviCancel.setOnClickListener(this);
        btnNaviGo.setOnClickListener(this);

        // 显示本船位置
        LocationBean currentLocation = MyApplication.getInstance().getCurrentLocation();
        setOwnShip(currentLocation, currentLocation.getHeading(), false);
    }

    @Override
    protected void onResume() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fMainView.postInvalidate();
            }
        }, 10);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (routeID != -1) {
            int[] ids = fMainView.mYimaLib.GetRouteWayPointsID(routeID);
            fMainView.mYimaLib.DeleteRouteWayPoint(routeID, 0, ids.length); // 必须在调用DeleteWayPoint之前
            for (int id : ids) {
                fMainView.mYimaLib.DeleteWayPoint(id);
            }
        }
        fMainView.mYimaLib.RotateMapByScrnCenter(0);
        fMainView.clearTrack();
        super.onDestroy();
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

        long timestamp = com.cetcme.xkterminal.MyClass.Constant.SYSTEM_DATE.getTime();
        boolean saveOk = fMainView.mYimaLib.SaveRoutesToFile(Constant.ROUTE_FILE_PATH + "/" + timestamp);

        if (saveOk) {
            toast.setText("保存成功: " + timestamp);
            toast.show();
            mLlBottom.setVisibility(View.GONE);
            clearRoute();
            startActivityForResult(new Intent(this, RouteListActivity.class), REQUSET_CODE_HANGXIAN);
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


    private M_POINT point = new M_POINT();

    /**
     * 添加路径点
     *
     * @param view
     */
    public void AddRouteWp_Event(View view) {

        final QMUIDialog.EditTextDialogBuilder latBuilder = new QMUIDialog.EditTextDialogBuilder(NavigationMainActivity.this);
        latBuilder.setTitle("添加路径点")
                .setPlaceholder("在此输入路径点纬度")
                .setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL)
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("确定", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        CharSequence text = latBuilder.getEditText().getText();
                        if (text != null && text.length() > 0) {
                            try {
                                point.y = (int) (Float.parseFloat(text.toString()) * 10000000);
                                if (point.y > 90 * 10000000 || point.y < -90 * 10000000) {
                                    Toast.makeText(NavigationMainActivity.this, "请填入正确路径点纬度", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                dialog.dismiss();
                                addPointOnMap(point);
                            } catch (NumberFormatException e) {
                                e.getStackTrace();
                                Toast.makeText(NavigationMainActivity.this, "请填入正确路径点纬度", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(NavigationMainActivity.this, "请填入路径点纬度", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        final QMUIDialog.EditTextDialogBuilder lonBuilder = new QMUIDialog.EditTextDialogBuilder(NavigationMainActivity.this);
        lonBuilder.setTitle("添加路径点")
                .setPlaceholder("在此输入路径点经度")
                .setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL)
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("确定", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        CharSequence text = lonBuilder.getEditText().getText();
                        if (text != null && text.length() > 0) {
                            try {
                                dialog.dismiss();
                                point.x = (int) (Float.parseFloat(text.toString()) * 10000000);
                                if (point.x > 180 * 10000000 || point.x < -180 * 10000000) {
                                    Toast.makeText(NavigationMainActivity.this, "请填入正确路径点经度", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                latBuilder.show();
                            } catch (NumberFormatException e) {
                                e.getStackTrace();
                                Toast.makeText(NavigationMainActivity.this, "请填入正确路径点经度", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(NavigationMainActivity.this, "请填入路径点经度", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
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
            addPointOnMap(m_point);
        }
    }

    private void addPointOnMap(M_POINT m_point) {
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
        fMainView.postInvalidate();
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

        switch (requestCode) {
            case 0: // 航线
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
                break;
            case 1: // 航迹
                switch (resultCode) {
                    case RouteListActivity.ACTIVITY_RESULT_ROUTE_SHOW:
                        String navtime = data.getStringExtra("navtime");
                        try {
                            List<LocationBean> list = db.selector(LocationBean.class)
                                    .where("navtime", "=", navtime)
                                    .orderBy("acqtime")
                                    .findAll();
                            hangjiList.clear();
                            if (list == null || list.isEmpty()) {
                                Toast.makeText(NavigationMainActivity.this, "未查询到相关轨迹信息", Toast.LENGTH_SHORT).show();
                            } else {
                                hangjiList.addAll(list);
                                fMainView.AddLineLayerAndObject(list);
                                mClearTrack.setVisibility(View.VISIBLE);
                                mListTrack.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(NavigationMainActivity.this, "未查询到相关轨迹信息", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
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

                Intent hangjiIntent = new Intent(NavigationMainActivity.this, RouteListActivity.class);
                hangjiIntent.putExtra("tag", "航迹");
                startActivityForResult(hangjiIntent, REQUSET_CODE_HANGJI);

                /*
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
                                        //保存文件
                                        String data = "";
                                        for (LocationBean lb : list) {
                                            data += lb.toString();
                                        }
                                        FileUtil.saveHangjiFile(startTime.replace("/", "-").replace(":", "-") + ".txt", data);
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
                */
                break;
            case R.id.clearTrack:
                fMainView.clearTrack();
                mClearTrack.setVisibility(View.GONE);
                mListTrack.setVisibility(View.GONE);
                break;
            case R.id.listTrack:
                final View content = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_route_list, null);
                final QHTitleView qhTitleView = content.findViewById(R.id.qhTitleView);
                qhTitleView.setTitle("查看航迹列表");
                final ListView listView = content.findViewById(R.id.listView);
                HangjiAdapter adapter = new HangjiAdapter(this, hangjiList);
                listView.setAdapter(adapter);
                final AlertDialog alertDialog = new AlertDialog.Builder(this).setView(content).create();
                alertDialog.getWindow().setLayout(QMUIDisplayHelper.getScreenWidth(this) * 8 / 10,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                alertDialog.show();
                break;
            case R.id.tv_route:
                type = 1;
                startActivityForResult(new Intent(this, RouteListActivity.class), REQUSET_CODE_HANGXIAN);
                menu.dismiss();
                break;
            case R.id.tv_navigator:
                startActivity(new Intent(this, NavigationActivity.class));
                menu.dismiss();
                break;
            case R.id.navi_cancel:
                llNavigator.setVisibility(View.GONE);
                clearRoute();
                startActivityForResult(new Intent(this, RouteListActivity.class), REQUSET_CODE_HANGXIAN);
                break;
            case R.id.navi_go:
                Intent intent = new Intent(this, NavigationActivity.class);
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

    public void Back_Event(View view) {
        finish();
    }


    // TODO: test 添加测试 自身位置
    private void addLocation() {
        int lon = 1210000000;
        int lat = 310000000;
        for (int i = 0; i < 20; i++) {
            LocationBean locationBean = new LocationBean();
            locationBean.setLongitude(lon + 1000000 * i);
            locationBean.setLatitude(lat + 100000 * i);
            locationBean.setHeading(45.1f);
            locationBean.setAcqtime(com.cetcme.xkterminal.MyClass.Constant.SYSTEM_DATE);
            try {
                db.saveBindingId(locationBean);
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
//        Log.e(TAG, "addLocation: ok");
    }

    public void setOwnShip(LocationBean locationBean, float heading, boolean rotateScreen) {
        fMainView.mYimaLib.SetOwnShipCurrentInfo(locationBean.getLongitude(), locationBean.getLatitude(), heading, 50, 50, 0, 0);
        fMainView.mYimaLib.SetOwnShipShowSymbol(false, 4, true, 16, 5000000);
        fMainView.mYimaLib.RotateMapByScrnCenter(rotateScreen ? 0 - heading : 0);
        fMainView.mYimaLib.CenterMap(locationBean.getLongitude(), locationBean.getLatitude());
        fMainView.postInvalidate();
    }
}
