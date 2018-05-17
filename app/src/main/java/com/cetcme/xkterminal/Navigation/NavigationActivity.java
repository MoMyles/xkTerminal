package com.cetcme.xkterminal.Navigation;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cetcme.xkterminal.MyApplication;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import yimamapapi.skia.M_POINT;
import yimamapapi.skia.ShipOffRoute;

import com.cetcme.xkterminal.R;
import com.cetcme.xkterminal.Sqlite.Bean.LocationBean;

public class NavigationActivity extends AppCompatActivity implements SkiaDrawView.OnMapClickListener{

    @BindView(R.id.skiaView) SkiaDrawView fMainView;
    @BindView(R.id.btn_navigation) Button btn_navigation;
    @BindView(R.id.ly_status) LinearLayout ly_status;

    @BindView(R.id.tv_lon) TextView tv_lon;
    @BindView(R.id.tv_lat) TextView tv_lat;
    @BindView(R.id.tv_head) TextView tv_head;
    @BindView(R.id.tv_speed) TextView tv_speed;

    private int startWp = -1;
    private int endWp = -1;
    private int routeID = -1;
    private M_POINT myLocation;

    private boolean inNavigating = false;
    private Toast toast;

    private static final int MESSAGE_TYPE_FLASH_BACK_COLOR = 0x01;
    private static final int MESSAGE_TYPE_SET_NEED_CENTER_OWN_SHIP = 0x02;

    private DbManager db = MyApplication.getInstance().getDb();
    String routeFileName = null;

    String TAG = "NavigationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        //设置全屏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_navigation);
        getSupportActionBar().hide();

        ButterKnife.bind(this);
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        // 如果是从航线文件进入导航界面
        routeFileName = getIntent().getStringExtra("routeFileName");
        if (routeFileName != null) {
            fMainView.mYimaLib.AddRoutesFromFile(Constant.ROUTE_FILE_PATH + "/" + routeFileName);
            int routeCount = fMainView.mYimaLib.GetRoutesCount();
            routeID = fMainView.mYimaLib.GetRouteIDFromPos(routeCount - 1);
        }

        fMainView.setOnMapClickListener(this);

        btn_navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (myLocation == null) {
                    toast.setText("未获取自身定位");
                    toast.show();
                    NavigationMainActivity.play("未获取自身定位");
                    return;
                }

                if (inNavigating) {
                    toast.setText("导航结束");
                    toast.show();
                    NavigationMainActivity.play("导航结束");
                    btn_navigation.setText("开始导航");

                    needCenterOwnShip = false;
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            finish();
//                        }
//                    }, 1000);
                } else {
                    if (myLocation.x == 0 && myLocation.y == 0) {
                        toast.setText("未获取自身定位");
                        toast.show();
                        NavigationMainActivity.play("未获取自身定位");
                        return;
                    }

                    if (routeID == -1) {
                        toast.setText("请设置导航终点");
                        toast.show();
                        NavigationMainActivity.play("请设置导航终点");
                        return;
                    }

                    needCenterOwnShip = true;
                    toast.setText("开始导航");
                    toast.show();
                    NavigationMainActivity.play("开始导航");

                    btn_navigation.setText("结束导航");

                }
                inNavigating = !inNavigating;
                fMainView.mYimaLib.CenterMap(myLocation.x, myLocation.y);
            }
        });
    }

    float addOne = 0.003f;
    Timer timer;
    @Override
    protected void onStart() {
        super.onStart();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fMainView.mYimaLib.SetCurrentScale(8878176.0f);
                fMainView.postInvalidate();


                //TODO: test 添加自身位置，实际需要从Event中取
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        LocationBean locationBean = new LocationBean();
                        locationBean.setLongitude((int) ((121.12 + addOne) * 10000000));
                        locationBean.setLatitude((int) ((31.12+ addOne) * 10000000));
                        locationBean.setHeading(45f);
                        locationBean.setSpeed(12.3f);
                        EventBus.getDefault().post(locationBean);
                        addOne += 0.003f;
                    }
                }, 1000, 1000);

                // end

            }
        }, 10);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onMapClicked(M_POINT m_point) {
        // 没有读取航线文件的时候，选取导航终点，直线导航
        if (routeFileName == null) {
            if (!inNavigating) {

                if (myLocation == null) {
                    toast.setText("未获取自身定位");
                    toast.show();
                    NavigationMainActivity.play("未获取自身定位");
                    return;
                }

                if (routeID == -1) {
                    // 创建航线，添加自己定位
                    startWp = fMainView.mYimaLib.AddWayPoint(myLocation.x, myLocation.y, "1", 20, "1");
                    int[] wpids = new int[]{startWp};
                    routeID = fMainView.mYimaLib.AddRoute("导航航线", wpids, 1, true);
                }

                if (endWp != -1) {
                    // 如果已经选取了导航终点，更新位置
                    fMainView.mYimaLib.SetWayPointCoor(endWp, m_point.x, m_point.y);
                } else {
                    endWp = fMainView.mYimaLib.AddWayPoint(m_point.x, m_point.y, "1", 20, "1");
                    fMainView.mYimaLib.AddRouteWayPoint(routeID, 1, new int[]{endWp}, 1);
                }

                fMainView.postInvalidate();
            }
        }
    }

    @Override
    public void onMapTouched(int action) {
        if (inNavigating) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    handler.removeMessages(MESSAGE_TYPE_SET_NEED_CENTER_OWN_SHIP);
                    break;
                case MotionEvent.ACTION_MOVE:
                    needCenterOwnShip = false;
                    break;
                case MotionEvent.ACTION_UP:
                    handler.sendEmptyMessageDelayed(MESSAGE_TYPE_SET_NEED_CENTER_OWN_SHIP, 1000 * 5);
                    break;
            }
        }
    }

    /**
     * 放大地图
     * @param view
     */
    public void ZoomInClick_Event(View view) {
        fMainView.mYimaLib.SetCurrentScale(fMainView.mYimaLib.GetCurrentScale() / 2);
        fMainView.postInvalidate();//刷新fMainView
    }

    /**
     * 缩小地图
     * @param view
     */
    public void ZoomOutClick_Event(View view) {
        fMainView.mYimaLib.SetCurrentScale(fMainView.mYimaLib.GetCurrentScale() * 2);
        fMainView.postInvalidate();//刷新fMainView
    }

    /**
     * 定位本船位置
     * @param view
     */
    public void OwnCenterClick_Event(View view){
        if (myLocation.x == 0.0 && myLocation.y == 0.0) return;
        fMainView.mYimaLib.CenterMap(myLocation.x, myLocation.y);
        fMainView.postInvalidate();//刷新fMainView
    }

    /**
     * 返回
     * @param view
     */
    public void Back_Event(View view){
        finish();
    }

    /**
     * 设置本船
     */
    public void setOwnShip(M_POINT m_point, float heading, boolean rotateScreen) {
        fMainView.mYimaLib.SetOwnShipBasicInfo("本船", "123456789", 100, 50);
        fMainView.mYimaLib.SetOwnShipCurrentInfo(m_point.x, m_point.y, heading, 50, 50, 0, 0);
        fMainView.mYimaLib.SetOwnShipShowSymbol(false, 4, true, 16, 5000000);
        fMainView.mYimaLib.RotateMapByScrnCenter(rotateScreen ? 0 - heading : 0);
        if (needCenterOwnShip) {
            fMainView.mYimaLib.CenterMap(myLocation.x, myLocation.y);
        }
        fMainView.postInvalidate();
    }

    /**
     * 航行监控，获取位置后调用此方法，无信息则返回null
     * @param m_point
     * @param heading
     * @param routeID
     * @return
     */
    private String safetyControl(M_POINT m_point, float heading, int routeID) {
        String msg = null;
        boolean approachDanger = fMainView.mYimaLib.IsShipApproachingIsolatedDanger(m_point.x, m_point.y, Constant.NAVIGATION_TO_DANGER_DIST_LIMIT);
        boolean crossingSafety = fMainView.mYimaLib.IsShipCrossingSafetyContour(m_point.x, m_point.y, heading, Constant.NAVIGATION_APPROACH_DIST_LIMIT);
        ShipOffRoute offRoute = fMainView.mYimaLib.isShipOffRoute(m_point.x, m_point.y, routeID, Constant.NAVIGATION_OFF_ROUTE_LIMIT);

        if (approachDanger) {
            msg = "即将进入危险区，距离" + Constant.NAVIGATION_TO_DANGER_DIST_LIMIT + "米";
        } else if (crossingSafety) {
            msg = "已进入危险水域，水深" + Constant.NAVIGATION_APPROACH_DIST_LIMIT + "米";
        } else if (offRoute.bOffRoute) {
            msg = "已偏航" + offRoute.offDistByMeter + "米";
        }
        Log.i(TAG, "航行监控: approachDanger: " + approachDanger + ", crossingSafety: " + crossingSafety
                + ", ShipOffRoute: " + offRoute.bOffRoute + ", " + offRoute.offDistByMeter);
        return msg;
    }

    boolean isBackWrite = true;
    boolean isDanger = false;
    private void flashStatusBackColor() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isDanger) {
                    try {
                        Message msg = new Message();
                        msg.what = MESSAGE_TYPE_FLASH_BACK_COLOR;
                        handler.sendMessage(msg);
                        Thread.sleep(700);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        // 处理子线程给我们发送的消息。
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_TYPE_FLASH_BACK_COLOR){
                ly_status.setBackgroundColor(isBackWrite ? getResources().getColor(R.color.navigation_status_danger) : getResources().getColor(R.color.navigation_status_normal));
//                tv_lon.setTextColor(isBackWrite? getResources().getColor(android.R.color.white) : getResources().getColor(android.R.color.black));
//                tv_lat.setTextColor(isBackWrite? getResources().getColor(android.R.color.white) : getResources().getColor(android.R.color.black));
//                tv_head.setTextColor(isBackWrite? getResources().getColor(android.R.color.white) : getResources().getColor(android.R.color.black));
//                tv_speed.setTextColor(isBackWrite? getResources().getColor(android.R.color.white) : getResources().getColor(android.R.color.black));
                isBackWrite = !isBackWrite;
            } else if (msg.what == MESSAGE_TYPE_SET_NEED_CENTER_OWN_SHIP) {
                if (inNavigating) needCenterOwnShip = true;
            }
        }
    };


    @Override
    protected void onDestroy() {
        isDanger = false;
        if (timer != null) timer.cancel();
        super.onDestroy();
    }

    private boolean needCenterOwnShip = false;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationEvent(LocationBean locationBean) {
        Log.i(TAG, "onLocationEvent: 收到自身位置");
        Log.i(TAG, "onLocationEvent: 纬度lat:" + locationBean.getLatitude());
        Log.i(TAG, "onLocationEvent: 经度lon:" + locationBean.getLongitude());
        if (myLocation == null) {
            myLocation = new M_POINT();
            myLocation.x = locationBean.getLongitude();
            myLocation.y = locationBean.getLatitude();
            fMainView.mYimaLib.CenterMap(myLocation.x, myLocation.y);
        } else {
            myLocation.x = locationBean.getLongitude();
            myLocation.y = locationBean.getLatitude();
        }

        // 更新路径起点
        if (!inNavigating) fMainView.mYimaLib.SetWayPointCoor(startWp, myLocation.x, myLocation.y);

        // 根据每次gps信息更新位置
        setOwnShip(myLocation, locationBean.getHeading(), inNavigating);

        // 更新框
        updateShipInfo(locationBean);

        if (inNavigating) {
            try {
                db.saveBindingId(locationBean);
            } catch (DbException e) {
                e.printStackTrace();
            }

            // 判断是否有危险
            String msg = safetyControl(myLocation, locationBean.getHeading(), routeID);
            if (msg != null) {
                // 如果有危险
                if (!isDanger) {
                    isDanger = true;
                    flashStatusBackColor();
                }
                toast.setText(msg);
                toast.show();
                NavigationMainActivity.play(msg);
            } else {
                isDanger = false;
            }

            // 计算终点距离，判断是否结束导航
            if (getNavigationEndDistance(myLocation, endWp) < Constant.NAVIGATION_END_DIST) {
                toast.setText("已到达目的地附件，导航结束");
                toast.show();
                NavigationMainActivity.play("已到达目的地附件，导航结束");
                btn_navigation.setText("开始导航");
                inNavigating = false;
            }
        }
    }

    private double getNavigationEndDistance(M_POINT m_point, int endWp) {
        M_POINT wpCoor = fMainView.mYimaLib.getWayPointCoor(endWp);
        double dist = fMainView.mYimaLib.GetDistBetwTwoPoint(wpCoor.x, wpCoor.y, m_point.x, m_point.y) * 1852;
        Log.i(TAG, "计算导航终点的距离: " + dist);
        return dist;
    }

    /**
     * 更新GPS状态框
     * @param locationBean
     */
    private void updateShipInfo(LocationBean locationBean) {
        tv_lon.setText(String.format("%.3f", locationBean.getLongitude() / 10000000f));
        tv_lat.setText(String.format("%.3f", locationBean.getLatitude() / 10000000f));
        tv_head.setText(locationBean.getHeading() + "");
        tv_speed.setText(locationBean.getSpeed() + "");
    }
}
