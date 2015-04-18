package com.example.such.smartcenter;

import android.support.v7.app.ActionBarActivity;

/**
 * Created by Such on 4/18/2015.
 */
public abstract class CenterModule {
    private String m_id;

    public abstract int GetResourceId();

    public abstract void OnClick(ActionBarActivity context);

    public boolean CompatibleWith(CenterModule module)
    {
        return false;
    }

}
