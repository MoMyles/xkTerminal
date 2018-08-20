package com.cetcme.xkterminal.ActionBar;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.cetcme.xkterminal.AisSetActivity;
import com.cetcme.xkterminal.MainActivity;
import com.cetcme.xkterminal.Navigation.NavigationMainActivity;
import com.cetcme.xkterminal.R;
import com.zyyoona7.popup.EasyPopup;
import com.zyyoona7.popup.XGravity;
import com.zyyoona7.popup.YGravity;

import java.util.ArrayList;

/**
 * Created by qiuhong on 10/01/2018.
 */

public class BottomBar2 extends RelativeLayout implements View.OnClickListener {

    public MainActivity mainActivity;

    private Button button_message, btn_map, btn_system, btn_alert, btn_post,btn_othership;

    private ArrayList<Button> buttons = new ArrayList<>();

    public BottomBar2(Context context) {
        super(context);
    }

    public BottomBar2(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 最先的板子分辨率为1024*552
        View view = LayoutInflater.from(context).inflate(R.layout.bar_bottom_scroll_view2, this, true);
        bindView(view);
    }

    private void bindView(View view) {
        button_message = view.findViewById(R.id.btn_message);
        btn_map = view.findViewById(R.id.btn_map);
        btn_system = view.findViewById(R.id.btn_system);
        btn_alert = view.findViewById(R.id.button_alert);
        btn_post = view.findViewById(R.id.button_post);
        btn_othership = view.findViewById(R.id.btn_othership);
        buttons.add(button_message);
        buttons.add(btn_map);
        buttons.add(btn_system);
        buttons.add(btn_alert);
        buttons.add(btn_post);
        buttons.add(btn_othership);

        for (Button button : buttons) {
            button.setTextColor(0xFFFFFFFF);
            button.setBackgroundResource(R.drawable.button_bg_selector);
            button.setOnClickListener(this);
        }
    }

    private EasyPopup popup, popup2, popup3;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_othership:
//                mainActivity.openOtherShips();
                popup3 = EasyPopup.create()
                        .setContentView(LayoutInflater.from(getContext()).inflate(R.layout.bar_bottom_item_ais, null))
                        .apply();
                Button btn12 = popup3.findViewById(R.id.btn12);// 附近船舶
                btn12.setOnClickListener(this);
                Button btn13 = popup3.findViewById(R.id.btn13);// AIS设置
                btn13.setOnClickListener(this);
                popup3.showAtAnchorView(btn_othership, XGravity.CENTER, YGravity.ABOVE, 130, -90);
                break;
            case R.id.btn12:
                mainActivity.openOtherShips();
                dismiss(popup3);
                break;
            case R.id.btn13:
                Intent intent = new Intent(getContext(), AisSetActivity.class);
                getContext().startActivity(intent);
                dismiss(popup3);
                break;
            case R.id.btn_message:
                popup = EasyPopup.create()
                        .setContentView(LayoutInflater.from(getContext()).inflate(R.layout.bar_bottom_item_message, null))
                        .apply();
                Button btn1 = popup.findViewById(R.id.btn1);// 新建短信
                btn1.setOnClickListener(this);
                Button btn2 = popup.findViewById(R.id.btn2);// 收件箱
                btn2.setOnClickListener(this);
                Button btn3 = popup.findViewById(R.id.btn3);// 发件箱
                btn3.setOnClickListener(this);
                Button btn9 = popup.findViewById(R.id.btn9);// 组播
                btn9.setOnClickListener(this);
                popup.showAtAnchorView(button_message, XGravity.CENTER, YGravity.ABOVE, 10, -110);
                break;
            case R.id.btn1:
                dismiss(popup);
                break;
            case R.id.btn2:
                mainActivity.initMessageFragment("receive");
                dismiss(popup);
                break;
            case R.id.btn3:
                mainActivity.initMessageFragment("send");
                dismiss(popup);
                break;
            case R.id.btn_map:
                popup2 = EasyPopup.create()
                        .setContentView(LayoutInflater.from(getContext()).inflate(R.layout.bar_bottom_item_map, null))
                        .apply();
                Button btn4 = popup2.findViewById(R.id.btn4);// 导航管理
                btn4.setOnClickListener(this);
                Button btn5 = popup2.findViewById(R.id.btn5);// 附近船舶
                btn5.setOnClickListener(this);
                Button btn6 = popup2.findViewById(R.id.btn6);// 地图标位
                btn6.setOnClickListener(this);
                Button btn10 = popup2.findViewById(R.id.btn10);// 航线
                btn10.setOnClickListener(this);
                Button btn11 = popup2.findViewById(R.id.btn11);// 航迹
                btn11.setOnClickListener(this);
                popup2.showAtAnchorView(btn_map, XGravity.CENTER, YGravity.ABOVE, 130, -90);
                break;
            case R.id.btn4:// 导航
                mainActivity.startActivity(new Intent(mainActivity, NavigationMainActivity.class));
                dismiss(popup2);
                break;
            case R.id.btn5:
                mainActivity.openOtherShips();
                dismiss(popup2);
                break;
            case R.id.btn6:
                mainActivity.openPinList();
                dismiss(popup2);
                break;
            case R.id.btn_system:
                mainActivity.initSettingFragment();
                break;
            case R.id.button_alert:
                mainActivity.initLogFragment("alert");
                break;
            case R.id.btn9:// 组播列表
                mainActivity.initLogFragment("group");
                dismiss(popup);
                break;
            case R.id.btn10:// 航线
                //TODO 航线操作
                dismiss(popup2);
                break;
            case R.id.btn11:// 航迹
                //TODO 航迹操作
                dismiss(popup2);
                break;
            case R.id.button_post:
                mainActivity.initLogFragment("inout");
                break;
            default:
                break;
        }
    }

    private void dismiss(EasyPopup popup) {
        if (popup != null && popup.isShowing()) {
            popup.dismiss();
        }
    }
}
