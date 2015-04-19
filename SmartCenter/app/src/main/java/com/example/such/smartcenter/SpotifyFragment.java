package com.example.such.smartcenter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Song;
import com.echonest.api.v4.Track;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;

import java.util.List;


public class SpotifyFragment extends Fragment {

    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 1337;

    int RESULTCODE = 0;
    EchoNestHelper echonest;
    //AudioRecord audio;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(SpotifyModule.Self.mPlayer == null) {
            Intent intent = SpotifyModule.Self.CreateLoginIntent(getActivity());
            startActivityForResult(intent, REQUEST_CODE);
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_spotify, container, false);
    }
    private void searchSongs() {
        final EditText titleET = null;//(EditText) findViewById(R.id.ETtitle);
        final EditText artistET = null;//(EditText) findViewById(R.id.ETartist);

        String title = String.valueOf(titleET.getText());
        String artist = String.valueOf(artistET.getText());
        new SongSearchTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,title,artist);



    }

    private void createTable(List<Song> songlist) {
        TableLayout ll = null;//(TableLayout) findViewById(R.id.searchResultsTable);
        ll.removeAllViews();
        TextView tvTitle;
        TextView tvArtist;
        Log.d("TABLE", "create table");
        if(songlist!=null) {
            for (final Song s : songlist) {
                TableRow row = new TableRow(getActivity());
                TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                row.setLayoutParams(lp);

                tvTitle = new TextView(getActivity());
                String title = s.getTitle();
                if(title.length()>30)
                    title = title.substring(0,30);
                tvTitle.setText(title);
                row.addView(tvTitle);

                tvArtist = new TextView(getActivity());
                String artist = s.getArtistName();
                if(artist.length()>30)
                    artist = artist.substring(0,30);
                tvArtist.setText("-"+artist);
                row.addView(tvArtist);

                ll.addView(row);

                Log.d("TABLE", "Row added with: " + s.getTitle() + " by " + s.getArtistName() + " (ID:" + s.getID() + ")" + " (Org ID:" + s.getOriginalID() + ")");

                row.setClickable(true);

                tvArtist.setClickable(true);
                tvArtist.setTextSize(20);

                tvTitle.setClickable(true);
                tvTitle.setTextSize(35);

                row.setPadding(0, 6, 0, 6);

                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("PLAY","clicked to play: "+s.getTitle()+" by "+s.getArtistName()+" id:"+s.getID());
                        HandleSongToPlay(s);
                    }
                });

                tvArtist.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("PLAY","clicked to play: "+s.getTitle()+" by "+s.getArtistName()+" id:"+s.getID());
                        HandleSongToPlay(s);
                    }
                });

                tvTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("PLAY","clicked to play: "+s.getTitle()+" by "+s.getArtistName()+" id:"+s.getID());

                        HandleSongToPlay(s);

                    }
                });

            }
        }
    }

    private void HandleSongToPlay(Song s) {
        Track spotifyTrack = null;
        try {
            // spotifyTrack = s.getTrack("spotify-WW");
            AsyncTask<Song, Void, Track> st = new SpotifyTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, s);

            spotifyTrack = st.get();


            if (spotifyTrack != null) {
                String spotifyID="";
                AsyncTask<Track, Void, String> tt = new SpotifyTrackIDTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, spotifyTrack);

                spotifyID = tt.get().trim();

                System.out.printf("Spotify FID %s\n", spotifyID);
                Log.d("PLAY", "Paused");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        RESULTCODE = resultCode;
        // Check if result comes from the correct activity
        Log.d("PLAYER","Entered onActivityResult");
        if (requestCode == REQUEST_CODE) {
            Log.d("PLAYER","Entered REQUEST_CODE");
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Log.d("PLAYER","Entered TOKEN");

                SpotifyModule.Self.accessToken = response.getAccessToken();
                SpotifyModule.Self.CreatePlayer(getActivity());
                 SpotifyModule.Self.FindSongAndPlay();//laySong("spotify:track:1QUOoDUouDDTAwzjIong25");
            }else{
                Log.d("PLAYER","TOKEN not valid:"+response.getType() +"  req:"+AuthenticationResponse.Type.TOKEN);
            }
        }
        else{
            Log.d("PLAYER","REQUEST_CODE not valid:"+requestCode);
        }
    }

    private class SongSearchTask extends AsyncTask<String, Void, List<Song>> {

        @Override
        protected List<Song> doInBackground(String... msgs) {
            String title = msgs[0];
            String artist = msgs[1];
            List<Song> songlist = null;
            try {
                echonest = new EchoNestHelper();
                songlist = echonest.searchSong(artist,title,5);
            } catch (EchoNestException e) {
                e.printStackTrace();
            }
            return songlist;
        }

        @Override
        protected void onPostExecute(List<Song> songlist)
        {
            createTable(songlist);
        }
    }


    private class SpotifyTask extends AsyncTask<Song, Void, Track> {

        @Override
        protected Track doInBackground(Song... s) {
            try {
                return s[0].getTrack("spotify-WW");
            } catch (EchoNestException e) {
                e.printStackTrace();
            }
            return null;
        }


    }

    private class SpotifyTrackIDTask extends AsyncTask<Track, Void, String> {

        @Override
        protected String doInBackground(Track... t) {
            try {
                return t[0].getForeignID();
            } catch (EchoNestException e) {
                e.printStackTrace();
            }
            return null;
        }


    }





}
