package com.example.such.smartcenter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * Created by Such on 4/19/2015.
 */
public class UniversalFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout view = (LinearLayout)inflater.inflate(R.layout.fragment_universal, container, false);

        for(CenterModule module : MainActivity.modules) {
            View v = module.InflateUniversal(inflater, container);
            if(v != null)
                view.addView(v);
        }

        return view;
    }
}
