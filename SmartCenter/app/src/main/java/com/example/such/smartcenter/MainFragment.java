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
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.Console;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.abs;


public class MainFragment extends Fragment implements View.OnTouchListener {
    private static final float Button_size_dp = 75.0f;
    private static final int Button_default_margin = 50;
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
        final int Button_Size = (int) (Button_size_dp*scale);
        final int Button_Margin = (int) (Button_default_margin*scale);
        int width = getResources().getDisplayMetrics().widthPixels;
        return  (width-Button_Margin)/(Button_Margin+Button_Size);
    }

    public int GetSpacingForButtons(View view, int n) {
        final float scale = getResources().getDisplayMetrics().density;
        final int Button_Size = (int) (Button_size_dp*scale);
        int width = getResources().getDisplayMetrics().widthPixels;

        return (width-Button_Size*n)/(n+1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        buttons.clear();
        FrameLayout view = (FrameLayout)inflater.inflate(R.layout.fragment_main, container, false);
        final float scale = getResources().getDisplayMetrics().density;
        final int Button_Size = (int) (Button_size_dp*scale);
        final int Button_Margin = (int) (Button_default_margin*scale);

        int per_row = GetMaxButtonsPerRow(view);

        int nc = 0;
        int nr = 0;
        int actualMargin = Button_Margin;
        if(per_row > MainActivity.modules.size())
            actualMargin = GetSpacingForButtons(view, MainActivity.modules.size());
        else
            actualMargin = GetSpacingForButtons(view, per_row);
        for(CenterModule module : MainActivity.modules) {
            ImageView img = new ImageView(getActivity());

            img.setOnTouchListener(this);
            img.setImageResource(module.GetResourceId());
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
            view.addView(img);

            final FrameLayout.LayoutParams par=new FrameLayout.LayoutParams(Button_Size, Button_Size);
            par.leftMargin = actualMargin + nc*(actualMargin + Button_Size);
            par.topMargin = Button_Margin+ nr*(Button_Margin + Button_Size);
            img.setLayoutParams(par);
            buttons.put(img, module);
            nc++;
            if(nc == per_row) {
                nc = 0;
                nr++;
                if(per_row > MainActivity.modules.size()-nr*per_row)
                    actualMargin = GetSpacingForButtons(view, MainActivity.modules.size()-nr*per_row);
            }
        }

        return view;
    }
    private int prevX, prevY;
    private int originX, originY;
    private int omx, omy;
    private View dragging = null;
    public boolean onTouch(View v, MotionEvent event) {
        boolean eventConsumed = true;
        int x = (int)event.getRawX();
        int y = (int)event.getRawY();

        final FrameLayout.LayoutParams par=(FrameLayout.LayoutParams)v.getLayoutParams();
        int action = event.getAction();
        CenterModule module =  buttons.get(v);
        if (action == MotionEvent.ACTION_DOWN) {
            if (dragging == null && module != null) {
                dragging = v;
                prevX = x;
                prevY = y;
                originX = prevX;
                originY = prevY;
                omx = par.leftMargin;
                omy = par.topMargin;
                dragging.bringToFront();

                for(Map.Entry<ImageView,CenterModule> a : buttons.entrySet()) {
                    if(v != a.getKey() && module.CompatibleWith(a.getValue()) == false) {
                        a.getKey().setAlpha(0.5f);
                    } else {
                        a.getKey().setAlpha(1.0f);
                    }
                }
            }
        }
        else if (action == MotionEvent.ACTION_UP) {
            dragging = null;
            int displacement = abs(originY-y) + abs(originX -x);
            if(displacement < 10) {
                if(module != null)
                    module.OnClick((ActionBarActivity) getActivity());
            }
            else {
                int cx = par.leftMargin+par.height/2;
                int cy = par.topMargin+par.width/2;
                ImageView collide = null;
                int distance = par.height*par.height;
                for(Map.Entry<ImageView,CenterModule> a : buttons.entrySet()) {
                    if(a.getKey() == v)
                        continue;
                    final FrameLayout.LayoutParams par2=(FrameLayout.LayoutParams)a.getKey().getLayoutParams();
                    int cx2 = par2.leftMargin+par2.height/2;
                    int cy2 = par2.topMargin+par2.width/2;
                    cx2 = cx2 - cx;
                    cx2 = cx2*cx2;
                    cy2 = cy2 - cy;
                    cy2 = cy2*cy2;
                    if(cx2 + cy2 < distance) {
                        collide = a.getKey();
                        break;
                    }
                }
                if(collide != null) {
                    final int dx = omx-par.leftMargin;
                    final int dy = omy-par.topMargin;
                    final int ox = par.leftMargin;
                    final int oy = par.topMargin;
                    final View o = v;
                    Animation a = new Animation() {
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            final FrameLayout.LayoutParams par=(FrameLayout.LayoutParams)o.getLayoutParams();
                            par.leftMargin = (int) (ox + dx*interpolatedTime);
                            par.topMargin = (int) (oy + dy*interpolatedTime);
                            o.setLayoutParams(par);
                        }
                    };
                    a.setDuration(500);
                    v.startAnimation(a);
                    if(module.CompatibleWith(buttons.get(collide))) {

                    } else {
                    }
                }
            }
            for(Map.Entry<ImageView,CenterModule> a : buttons.entrySet()) {
                    a.getKey().setAlpha(1.0f);
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
