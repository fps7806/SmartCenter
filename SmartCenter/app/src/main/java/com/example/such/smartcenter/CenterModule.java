package com.example.such.smartcenter;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


/**
 * Created by Such on 4/18/2015.
 */
public abstract class CenterModule {
    private String m_id;

    public abstract int GetResourceId();

    public abstract void OnClick(ActionBarActivity context, ImageView view);
    public View InflateUniversal(LayoutInflater inflater, ViewGroup root) {
        return null;
    }

    public boolean CompatibleWith(CenterModule module)
    {
        return false;
    }

}
