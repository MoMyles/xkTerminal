package com.cetcme.xkterminal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cetcme.xkterminal.MyClass.Constant;
import com.cetcme.xkterminal.MyClass.GPSFormatUtils;
import com.cetcme.xkterminal.Sqlite.Bean.PinBean;
import com.cetcme.xkterminal.Sqlite.Proxy.PinProxy;

import org.greenrobot.eventbus.EventBus;
import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PinActivity extends Activity {

    public static final int RESULT_CODE_NOTHING = 0;
    public static final int RESULT_CODE_MAP = 1;
    public static final int RESULT_CODE_CO = 2;
    public static final int RESULT_OUT_OF_LIMIT = 3;

    @BindView(R.id.listView)
    ListView listView;

    @BindView(R.id.iv_back)
    ImageView iv_back;

    @BindView(R.id.tv_pin_map)
    TextView tv_pin_map;

    @BindView(R.id.tv_pin_co)
    TextView tv_pin_co;

    private TestAdapter testAdapter;
    private List<PinBean> dataList = new ArrayList<>();

    private DbManager db = MyApplication.getInstance().getDb();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);
        ButterKnife.bind(this);

        initTitleView();
        initListView();
        getPinData();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CODE_NOTHING);
        super.onBackPressed();
    }

    private void initTitleView() {
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CODE_NOTHING);
                finish();
            }
        });

        tv_pin_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkCount()) {
                    setResult(RESULT_OUT_OF_LIMIT);
                } else {
                    setResult(RESULT_CODE_MAP);
                }
                finish();
            }
        });

        tv_pin_co.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkCount()) {
                    setResult(RESULT_OUT_OF_LIMIT);
                } else {
                    setResult(RESULT_CODE_CO);
                }
                finish();
            }
        });
    }

    private boolean checkCount() {
        long count = PinProxy.getCount(MyApplication.getInstance().getDb());
        return count >= Constant.LIMIT_PIN;
    }


    private void initListView() {
        testAdapter = new TestAdapter();
        listView.setAdapter(testAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                AlertDialog.Builder builder = new AlertDialog.Builder(PinActivity.this);
                builder.setMessage("确定要删除标位吗？");
                builder.setPositiveButton("删除",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                try {
                                    db.delete(dataList.get(i));
                                } catch (DbException e) {
                                    e.printStackTrace();
                                }
                                dataList.remove(i);
                                testAdapter.notifyDataSetChanged();
                                EventBus.getDefault().post("update_biaowei_show");
                                Toast.makeText(MyApplication.getInstance().mainActivity, "删除成功", Toast.LENGTH_SHORT).show();
                            }
                        });
                builder.setNeutralButton("取消", null);

                builder.show();
            }
        });
    }

    public List<PinBean> getPinData() {
        dataList.clear();

        try {
            List<PinBean> pinBeanList = db.selector(PinBean.class).findAll();
            if (pinBeanList != null && !pinBeanList.isEmpty()) {
                dataList.addAll(pinBeanList);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

        testAdapter.notifyDataSetChanged();
        return dataList;
    }


    class TestAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public PinBean getItem(int i) {
            return dataList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder vh = null;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.cell_pin_list, viewGroup, false);
                vh = new ViewHolder();
                vh.mTv1 = view.findViewById(R.id.tv_name);
                vh.mTv2 = view.findViewById(R.id.tv_lon);
                vh.mTv3 = view.findViewById(R.id.tv_lat);
                vh.mTv4 = view.findViewById(R.id.tv_color);
                view.setTag(vh);
            } else {
                vh = (ViewHolder) view.getTag();
            }

            PinBean pin = getItem(i);
            if (pin != null) {
                vh.mTv1.setText(pin.getName());
                vh.mTv2.setText(GPSFormatUtils.DDtoDMS(pin.getLon() / 10000000d, true));
                vh.mTv3.setText(GPSFormatUtils.DDtoDMS(pin.getLat() / 10000000d, false));
                // vh.mTv4.setBackgroundColor(pin.getColor());
                if (getResources().getColor(android.R.color.holo_red_dark) == pin.getColor()) {
                    vh.mTv4.setImageResource(R.drawable.bw_red);
                } else if (getResources().getColor(android.R.color.holo_green_dark) == pin.getColor()) {
                    vh.mTv4.setImageResource(R.drawable.bw_green);
                } else if (getResources().getColor(android.R.color.holo_blue_dark) == pin.getColor()) {
                    vh.mTv4.setImageResource(R.drawable.bw_blue);
                } else {
                    vh.mTv4.setImageResource(R.drawable.bw_yellow);
                }
            } else {
                vh.mTv1.setText("");
                vh.mTv2.setText("");
                vh.mTv3.setText("");
                vh.mTv4.setBackgroundColor(getResources().getColor(android.R.color.white));
            }
            return view;
        }

        class ViewHolder {
            TextView mTv1;
            TextView mTv2;
            TextView mTv3;
            ImageView mTv4;
        }
    }
}
