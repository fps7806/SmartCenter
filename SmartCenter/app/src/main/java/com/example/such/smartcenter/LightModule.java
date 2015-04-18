package com.example.such.smartcenter;

import android.support.v7.app.ActionBarActivity;

/**
 * Created by Such on 4/18/2015.
 */
public class LightModule extends CenterModule {

    @Override
    public int GetResourceId() {
        return R.drawable.ic_light;
    }

    @Override
    public void OnClick(ActionBarActivity context) {
        context.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new LeDevicesFragment())
                .addToBackStack(null)
                .commit();
    }
}
