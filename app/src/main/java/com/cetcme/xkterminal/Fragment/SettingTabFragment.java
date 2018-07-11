package com.cetcme.xkterminal.Fragment;


import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cetcme.xkterminal.Fragment.setting.DBSettingFragment;
import com.cetcme.xkterminal.Fragment.setting.SatelliteFragment;
import com.cetcme.xkterminal.Fragment.setting.SeriveStatusFragment;
import com.cetcme.xkterminal.Fragment.setting.SystemSettingFragment;
import com.cetcme.xkterminal.Fragment.setting.WarningSettingFragment;
import com.cetcme.xkterminal.Fragment.setting.WeatherInfoFragment;
import com.cetcme.xkterminal.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingTabFragment extends Fragment {

    /**
     * 顶部2个LinearLayout
     */
    @BindView(R.id.ly_tab1)  LinearLayout ly_tab1;
    @BindView(R.id.ly_tab2)  LinearLayout ly_tab2;
    @BindView(R.id.ly_tab3)  LinearLayout ly_tab3;
    @BindView(R.id.ly_tab4)  LinearLayout ly_tab4;
    @BindView(R.id.ly_tab5)  LinearLayout ly_tab5;
    @BindView(R.id.ly_tab6)  LinearLayout ly_tab6;

    /**
     * 顶部的6个TextView
     */
    @BindView(R.id.tv_tab1)  TextView tv_tab1;
    @BindView(R.id.tv_tab2)  TextView tv_tab2;
    @BindView(R.id.tv_tab3)  TextView tv_tab3;
    @BindView(R.id.tv_tab4)  TextView tv_tab4;
    @BindView(R.id.tv_tab5)  TextView tv_tab5;
    @BindView(R.id.tv_tab6)  TextView tv_tab6;

    /**
     * Tab的那个引导线
     */
    @BindView(R.id.img_tab_line) ImageView img_tab_line;

    @BindView(R.id.viewpager_in_sign_activity) ViewPager mViewPager;

    /**
     * 屏幕的宽度
     */
    private int screenWidth;


    private FragmentAdapter mAdapter;
    private List<Fragment> fragments = new ArrayList<>();

    private Resources res;

    Unbinder unbinder;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
            return view;
        }
        view = inflater.inflate(R.layout.fragment_setting_tab, container, false);
        unbinder = ButterKnife.bind(this, view);
        res = getResources();
        initViewPager(view);
        
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void initViewPager(View view) {
        initView(view);


        /**
         * 初始化Adapter
         */
        mAdapter = new FragmentAdapter(getChildFragmentManager(), fragments);

        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new TabOnPageChangeListener());

        initTabLine(view);
    }

    /**
     * 功能：主页引导栏的三个Fragment页面设置适配器
     */

    class FragmentAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments;

        public FragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments=fragments;
        }

        public Fragment getItem(int fragment) {
            return fragments.get(fragment);
        }

        public int getCount() {
            return fragments.size();
        }

    }

    /**
     * 根据屏幕的宽度，初始化引导线的宽度
     */
    private void initTabLine(View view) {
        //获取屏幕的宽度
        DisplayMetrics outMetrics = new DisplayMetrics();
        getActivity().getWindow().getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        screenWidth = outMetrics.widthPixels;

        //获取控件的LayoutParams参数(注意：一定要用父控件的LayoutParams写LinearLayout.LayoutParams)
        LinearLayout.LayoutParams layoutParams = (android.widget.LinearLayout.LayoutParams) img_tab_line.getLayoutParams();
        layoutParams.width = screenWidth / fragments.size();//设置该控件的layoutParams参数
        img_tab_line.setLayoutParams(layoutParams);//将修改好的layoutParams设置为该控件的layoutParams
    }

    /**
     * 初始化控件，初始化Fragment
     */
    private void initView(View view) {
        tv_tab1.setOnClickListener(new TabOnClickListener(0));
        tv_tab2.setOnClickListener(new TabOnClickListener(1));
        tv_tab3.setOnClickListener(new TabOnClickListener(2));
        tv_tab4.setOnClickListener(new TabOnClickListener(3));
        tv_tab5.setOnClickListener(new TabOnClickListener(4));
        tv_tab6.setOnClickListener(new TabOnClickListener(5));

        fragments.add(new DBSettingFragment());
        fragments.add(new SatelliteFragment());
        fragments.add(new WarningSettingFragment());
        fragments.add(new SeriveStatusFragment());
//        fragments.add(new WeatherInfoFragment());
        fragments.add(new SystemSettingFragment());
        fragments.add(new AboutFragment());

        ly_tab1.setOnClickListener(new TabOnClickListener(0));
        ly_tab2.setOnClickListener(new TabOnClickListener(1));
        ly_tab3.setOnClickListener(new TabOnClickListener(2));
        ly_tab4.setOnClickListener(new TabOnClickListener(3));
        ly_tab5.setOnClickListener(new TabOnClickListener(4));
        ly_tab6.setOnClickListener(new TabOnClickListener(5));
    }

    /**
     * 重置颜色
     */
    private void resetTextView() {
        tv_tab1.setTextColor(res.getColor(R.color.text_clo));
        tv_tab2.setTextColor(res.getColor(R.color.text_clo));
        tv_tab3.setTextColor(res.getColor(R.color.text_clo));
        tv_tab4.setTextColor(res.getColor(R.color.text_clo));
        tv_tab5.setTextColor(res.getColor(R.color.text_clo));
        tv_tab6.setTextColor(res.getColor(R.color.text_clo));
    }

    /**
     * 功能：点击主页TAB事件
     */
    public class TabOnClickListener implements View.OnClickListener{
        private int index = 0;

        public TabOnClickListener(int i){
            index = i;
        }

        public void onClick(View v) {
            mViewPager.setCurrentItem(index);//选择某一页
        }

    }

    /**
     * 功能：Fragment页面改变事件
     */
    public class TabOnPageChangeListener implements ViewPager.OnPageChangeListener {

        //当滑动状态改变时调用
        public void onPageScrollStateChanged(int state) {

        }

        //当前页面被滑动时调用
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){
            LinearLayout.LayoutParams layoutParams = (android.widget.LinearLayout.LayoutParams) img_tab_line.getLayoutParams();
            //返回组件距离左侧组件的距离
            layoutParams.leftMargin= (int) ((positionOffset + position) * screenWidth / fragments.size());
            img_tab_line.setLayoutParams(layoutParams);
        }

        //当新的页面被选中时调用
        public void onPageSelected(int position) {
            //重置所有TextView的字体颜色
            resetTextView();
            switch (position) {
                case 0:
                    tv_tab1.setTextColor(res.getColor(R.color.QHTitleColor));
                    break;
                case 1:
                    tv_tab2.setTextColor(res.getColor(R.color.QHTitleColor));
                    break;
                case 2:
                    tv_tab3.setTextColor(res.getColor(R.color.QHTitleColor));
                    break;
                case 3:
                    tv_tab4.setTextColor(res.getColor(R.color.QHTitleColor));
                    break;
                case 4:
                    tv_tab5.setTextColor(res.getColor(R.color.QHTitleColor));
                    break;
                case 5:
                    tv_tab6.setTextColor(res.getColor(R.color.QHTitleColor));
                    break;
            }
        }
    }

}
