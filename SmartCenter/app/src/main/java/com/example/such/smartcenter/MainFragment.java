package com.example.such.smartcenter;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.Console;
import java.util.HashMap;

import static java.lang.Math.abs;


public class MainFragment extends Fragment implements View.OnTouchListener {
    private HashMap<ImageView,CenterModule> buttons = new HashMap<>();

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    public int GetMaxButtonsPerRow(View view) {
        final float scale = getResources().getDisplayMetrics().density;
        final int Button_Size = (int) (50.0f*scale);
        final int Button_Margin = 50;
        int width = getResources().getDisplayMetrics().widthPixels;
        return  (width-Button_Margin)/(Button_Margin+Button_Size);
    }

    public int GetSpacingForButtons(View view, int n) {
        final float scale = getResources().getDisplayMetrics().density;
        final int Button_Size = (int) (50.0f*scale);
        final int Button_Margin = 50;
        int width = getResources().getDisplayMetrics().widthPixels;

        return (width-Button_Size*n)/(n+1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FrameLayout view = (FrameLayout)inflater.inflate(R.layout.fragment_main, container, false);
        final float scale = getResources().getDisplayMetrics().density;
        final int Button_Size = (int) (50.0f*scale);
        final int Button_Margin = 50;

        int per_row = GetMaxButtonsPerRow(view);

        int nr = 0;
        int actualMargin = 0;
        if(per_row > MainActivity.modules.size())
            actualMargin = GetSpacingForButtons(view, MainActivity.modules.size());
        for(CenterModule module : MainActivity.modules) {
            ImageView img = new ImageView(getActivity());

            img.setOnTouchListener(this);
            img.setImageResource(module.GetResourceId());
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
            view.addView(img);

            final FrameLayout.LayoutParams par=new FrameLayout.LayoutParams(Button_Size, Button_Size);
            par.leftMargin = actualMargin + nr*(actualMargin + Button_Size);
            par.topMargin = Button_Margin;
            img.setLayoutParams(par);
            buttons.put(img, module);
            nr++;
        }

        return view;
    }
    private int prevX, prevY;
    private int originX, originY;
    private View dragging = null;
    public boolean onTouch(View v, MotionEvent event) {
        boolean eventConsumed = true;
        int x = (int)event.getRawX();
        int y = (int)event.getRawY();

        final FrameLayout.LayoutParams par=(FrameLayout.LayoutParams)v.getLayoutParams();
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            if (dragging == null && buttons.containsKey(v)) {
                dragging = v;
                prevX = x;
                prevY = y;
                originX = prevX;
                originY = prevY;
                dragging.bringToFront();
            }
        }
        else if (action == MotionEvent.ACTION_UP) {
            dragging = null;
            int displacement = abs(originY-y) + abs(originX -x);
            if(displacement < 10) {
                CenterModule module = buttons.get(v);
                if(module != null)
                    module.OnClick((ActionBarActivity) getActivity());
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
                if (dragging != null) {
                    par.leftMargin += x-prevX;
                    par.topMargin += y-prevY;
                    prevX = x;
                    prevY = y;
                    v.setLayoutParams(par);
                }
        }

        return eventConsumed;

    }
}
