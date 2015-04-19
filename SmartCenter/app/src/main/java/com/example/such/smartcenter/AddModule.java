package com.example.such.smartcenter;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.widget.ImageView;

/**
 * Created by Such on 4/18/2015.
 */
public class AddModule extends CenterModule {
    @Override
    public int GetResourceId() {
        return R.drawable.ic_plus;
    }

    @Override
    public void OnClick(final ActionBarActivity context, ImageView view) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.inflate(R.menu.menu_add);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.add_lights) {
                    LightModule module = new LightModule();
                    MainActivity.modules.add(module);
                    LeDevicesFragment frag = new LeDevicesFragment();
                    frag.module = module;
                    context.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, frag)
                            .addToBackStack(null)
                            .commit();
                } else if(menuItem.getItemId() == R.id.add_spotify) {
                    SpotifyModule module = new SpotifyModule();
                    MainActivity.modules.add(module);
                    SpotifyFragment frag = new SpotifyFragment();
                    context.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, frag)
                            .addToBackStack(null)
                            .commit();
                }
                return false;
            }
        });
        popup.show();
    }
}
