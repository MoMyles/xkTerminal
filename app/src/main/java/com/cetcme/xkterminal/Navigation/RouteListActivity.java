package com.cetcme.xkterminal.Navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cetcme.xkterminal.R;
import com.qiuhong.qhlibrary.QHTitleView.QHTitleView;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.cetcme.xkterminal.Navigation.FileUtil.stampToDate;


public class RouteListActivity extends Activity {

    @BindView(R.id.listView)
    ListView listView;
    @BindView(R.id.qhTitleView)
    QHTitleView qhTitleView;

    //    private SimpleAdapter simpleAdapter;
    private TestAdapter testAdapter;
    private List<Map<String, Object>> dataList = new ArrayList<>();

    public static final int ACTIVITY_RESULT_ROUTE_SHOW = 0x02;
    public static final int ACTIVITY_RESULT_ROUTE_ADD = 0x01;
    public static final int ACTIVITY_RESULT_ROUTE_NOTHING = 0x00;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);
        ButterKnife.bind(this);
        context = this;

        // PermissionUtil.verifyStoragePermissions(this);

        initTitleView();
        initListView();

        getFilesData();
    }

    private void initTitleView() {
        qhTitleView.setTitle("航迹列表");
        qhTitleView.setBackView(R.mipmap.title_icon_back_2x);
        qhTitleView.setRightView(R.mipmap.title_icon_add_2x);
        qhTitleView.setBackgroundResource(R.drawable.top_select);
        qhTitleView.setClickCallback(new QHTitleView.ClickCallback() {
            @Override
            public void onBackClick() {
                setResult(ACTIVITY_RESULT_ROUTE_NOTHING);
                finish();
            }

            @Override
            public void onRightClick() {
                setResult(ACTIVITY_RESULT_ROUTE_ADD);
                finish();
            }
        });
    }

    private void initListView() {
//        simpleAdapter = new SimpleAdapter(this, getFilesData(), R.layout.cell_route_list,
//                new String[] {"fileName", "lastModifyTime"},
//                new int[] {R.id.tv_name, R.id.tv_time});
        testAdapter = new TestAdapter();
        listView.setAdapter(testAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.putExtra("fileName", dataList.get(i).get("fileName").toString());
                setResult(ACTIVITY_RESULT_ROUTE_SHOW, intent);
                finish();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final String fileName = dataList.get(i).get("fileName").toString();
                File filePath = new File(Constant.ROUTE_FILE_PATH + "/" + fileName);
                if (filePath.exists()) {
                    filePath.delete();
                }
                Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                dataList.remove(i);
                testAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    public List<Map<String, Object>> getFilesData() {
        dataList.clear();

        File f = new File(Constant.ROUTE_FILE_PATH);
        File[] files = f.listFiles();

        if (files == null) {
            return dataList;
        }

        for (File file : files) {
            Map<String, Object> map = new Hashtable<>();
            map.put("fileName", file.getName());
            map.put("lastModifyTime", stampToDate(file.lastModified()));
            map.put("lastModifyStamp", file.lastModified());
            map.put("fileLength", (new BigDecimal(file.length() / 1024f)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "KB");
            dataList.add(map);
        }
        System.out.println(dataList.size());

        // 排序 最近创建的在前面
        Collections.sort(dataList, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> lhs, Map<String, Object> rhs) {
                // rhs 和lhs 换位置 --> 正序或倒序
                return Long.valueOf(rhs.get("lastModifyStamp").toString()).compareTo(Long.valueOf(lhs.get("lastModifyStamp").toString()));
            }
        });

        testAdapter.notifyDataSetChanged();
        return dataList;
    }


    @Override
    public void onBackPressed() {
        setResult(ACTIVITY_RESULT_ROUTE_NOTHING);
        super.onBackPressed();
    }

    class TestAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Map<String, Object> getItem(int i) {
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
                view = getLayoutInflater().inflate(R.layout.cell_route_list, viewGroup, false);
                vh = new ViewHolder();
                vh.mTv1 = view.findViewById(R.id.tv_name);
                vh.mTv2 = view.findViewById(R.id.tv_time);
            } else {
                vh = (ViewHolder) view.getTag();
            }
            Map<String, Object> item = getItem(i);
            if (item != null) {
                vh.mTv1.setText(item.get("fileName") + "");
                vh.mTv2.setText(item.get("lastModifyTime") + "");
            } else {
                vh.mTv1.setText("");
                vh.mTv2.setText("");
            }
            return view;
        }

        class ViewHolder {
            TextView mTv1;
            TextView mTv2;
        }
    }
}
