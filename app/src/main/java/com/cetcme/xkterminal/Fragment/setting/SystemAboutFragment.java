package com.cetcme.xkterminal.Fragment.setting;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cetcme.xkterminal.MyClass.APKVersionCodeUtils;
import com.cetcme.xkterminal.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SystemAboutFragment extends Fragment {


    public SystemAboutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_system_about, container, false);
        TextView tv_version = view.findViewById(R.id.tv_version);
        String version = APKVersionCodeUtils.getVerName(getActivity());
        tv_version.setText(version);
        return view;
    }

}
