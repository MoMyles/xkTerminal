package com.cetcme.xkterminal.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cetcme.xkterminal.ActionBar.TitleBar;
import com.cetcme.xkterminal.Fragment.setting.BlankFragment;
import com.cetcme.xkterminal.Fragment.setting.SatelliteFragment;
import com.cetcme.xkterminal.Fragment.setting.SeriveStatusFragment;
import com.cetcme.xkterminal.Fragment.setting.SystemSettingFragment;
import com.cetcme.xkterminal.Fragment.setting.DBSettingFragment;
import com.cetcme.xkterminal.Fragment.setting.WarningSettingFragment;
import com.cetcme.xkterminal.Fragment.setting.WeatherInfoFragment;
import com.cetcme.xkterminal.R;
import com.qmuiteam.qmui.widget.QMUITabSegment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qiuhong on 10/01/2018.
 */

public class SettingFragment extends Fragment {

    QMUITabSegment mTabSegment;
    ViewPager mContentViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_setting,container,false);

        TitleBar titleBar = view.findViewById(R.id.titleBar);
        titleBar.setTitle("系统参数");

        mTabSegment = view.findViewById(R.id.tabSegment);
        mContentViewPager = view.findViewById(R.id.contentViewPager);
        initTabAndPager();

        return view;
    }

    private void initTabAndPager() {
        //构造适配器
        List<Fragment> fragments=new ArrayList<>();
        fragments.add(new DBSettingFragment());
        fragments.add(new SatelliteFragment());
        fragments.add(new WarningSettingFragment());
        fragments.add(new SeriveStatusFragment());
        fragments.add(new WeatherInfoFragment());
        fragments.add(new SystemSettingFragment());
        FragAdapter adapter = new FragAdapter(getChildFragmentManager(), fragments);

        mContentViewPager.setAdapter(adapter);

        mContentViewPager.setCurrentItem(0, false);
        mTabSegment.addTab(new QMUITabSegment.Tab("北斗参数"));
        mTabSegment.addTab(new QMUITabSegment.Tab("卫星分布"));
        mTabSegment.addTab(new QMUITabSegment.Tab("报警设置"));
        mTabSegment.addTab(new QMUITabSegment.Tab("服务状态"));
        mTabSegment.addTab(new QMUITabSegment.Tab("当前海域气象信息"));
        mTabSegment.addTab(new QMUITabSegment.Tab("系统设置"));


        mTabSegment.setupWithViewPager(mContentViewPager, false);
        mTabSegment.setMode(QMUITabSegment.MODE_FIXED);
        mTabSegment.setHasIndicator(true);
        mTabSegment.setIndicatorPosition(false);
        mTabSegment.setIndicatorWidthAdjustContent(false);

        mTabSegment.addOnTabSelectedListener(new QMUITabSegment.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int index) {
                mTabSegment.hideSignCountView(index);
            }

            @Override
            public void onTabUnselected(int index) {

            }

            @Override
            public void onTabReselected(int index) {
                mTabSegment.hideSignCountView(index);
            }

            @Override
            public void onDoubleTap(int index) {

            }
        });
    }

    public class FragAdapter extends FragmentPagerAdapter {

        private List<Fragment> mFragments;

        public FragAdapter(FragmentManager fm,List<Fragment> fragments) {
            super(fm);
            // TODO Auto-generated constructor stub
            mFragments = fragments;
        }

        @Override
        public Fragment getItem(int arg0) {
            // TODO Auto-generated method stub
            return mFragments.get(arg0);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mFragments.size();
        }

    }

}
