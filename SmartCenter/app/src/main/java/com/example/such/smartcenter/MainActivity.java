package com.example.such.smartcenter;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ImageView;

import com.spotify.sdk.android.player.Spotify;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    static public MainActivity Self;

    static public ArrayList<CenterModule> modules = new ArrayList<CenterModule>();

    public class UniversalModule extends CenterModule {
        @Override
        public int GetResourceId() {
            return R.drawable.ic_remote;
        }

        @Override
        public void OnClick(ActionBarActivity context, ImageView view) {
            context.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new UniversalFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }

    public MainActivity() {
        Self = this;
        if(modules.size() == 0) {
            modules.add(new AddModule());
            modules.add(new UniversalModule());
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SplashScreenFragment())
                    .commit();
        }
    }

    @Override
    protected void onDestroy() {
        if(SpotifyModule.Self.mPlayer != null)
            Spotify.destroyPlayer(SpotifyModule.Self.mPlayer);
        super.onDestroy();
    }

}
