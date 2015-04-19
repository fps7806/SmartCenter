package com.danza.spotify;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Song;
import com.echonest.api.v4.Track;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.PlayerStateCallback;
import com.spotify.sdk.android.player.Spotify;

import java.util.List;

public class MainActivity extends Activity implements
        PlayerNotificationCallback, ConnectionStateCallback {

    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 1337;
    Player.Builder pb;
    int RESULTCODE = 0;
    EchoNestHelper echonest;
    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "9d86b7b41cc04f0299f770842bb5cd3c";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "spotify://callback";
    String id = "1QUOoDUouDDTAwzjIong25";
    Intent intent;
    String accessToken = "";
    //AudioRecord audio;
    private Player mPlayer = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //audio = new AudioRecord(0,44100,16,2,AudioRecord.getMinBufferSize (44100, 16, 2));



        findViewById(R.id.BTsearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                searchSongs();

            }
        });

        findViewById(R.id.BTpause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mPlayer!=null)
                    mPlayer.pause();

            }
        });


        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);


    }

    private void searchSongs() {



        final EditText titleET = (EditText) findViewById(R.id.ETtitle);
        final EditText artistET = (EditText) findViewById(R.id.ETartist);

        String title = String.valueOf(titleET.getText());
        String artist = String.valueOf(artistET.getText());
        new SongSearchTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,title,artist);



    }

    private void createTable(List<Song> songlist) {
        TableLayout ll = (TableLayout) findViewById(R.id.searchResultsTable);
        ll.removeAllViews();
        TextView tvTitle;
        TextView tvArtist;
        Log.d("TABLE","create table");
        if(songlist!=null) {
            for (final Song s : songlist) {
                TableRow row = new TableRow(this);
                TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                row.setLayoutParams(lp);

                tvTitle = new TextView(this);
                String title = s.getTitle();
                if(title.length()>30)
                    title = title.substring(0,30);
                tvTitle.setText(title);
                row.addView(tvTitle);

                tvArtist = new TextView(this);
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
                //id = spotifyID;
                intent.putExtra("id",spotifyID);
                //mPlayer.pause();
                Log.d("PLAY", "Paused");
                               /* Spotify.destroyPlayer(this);
                                Log.d("PLAY","Destroyed");*/
                PlaySong(accessToken);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        RESULTCODE = resultCode;
        // Check if result comes from the correct activity
        Log.d("PLAYER","Entered onActivityResult");
        if (requestCode == REQUEST_CODE) {
            Log.d("PLAYER","Entered REQUEST_CODE");
            this.intent = intent;
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Log.d("PLAYER","Entered TOKEN");

                accessToken = response.getAccessToken();
                PlaySong(accessToken);

            }else{
                Log.d("PLAYER","TOKEN not valid:"+response.getType() +"  req:"+AuthenticationResponse.Type.TOKEN);
            }
        }
        else{
            Log.d("PLAYER","REQUEST_CODE not valid:"+requestCode);
        }
    }

    private void PlaySong(String accessToken) {
        Log.d("PLAYSONG","Entered");

        if(mPlayer==null) {
            Config playerConfig = new Config(this, accessToken, CLIENT_ID);
            pb = new Player.Builder(playerConfig);
            Handler myHandler = new Handler();
            pb.setCallbackHandler(myHandler);

            //final String intentID = intent.getStringExtra("id");

            mPlayer = Spotify.getPlayer(pb, this, new Player.InitializationObserver() {
                @Override
                public void onInitialized(Player player) {
                    Log.d("PLAYSONG", "entered onInitialized");
                    mPlayer.addConnectionStateCallback(MainActivity.this);
                    mPlayer.addPlayerNotificationCallback(MainActivity.this);
                   // if (intentID == null || intentID.isEmpty()) {
                      Log.d("PLAYER", "in onActivityResult Default ID:" + id);
                        mPlayer.play("spotify:track:" + id);
                    new AudioTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                   /* } else {
                        Log.d("PLAYER", "in onActivityResult new IDx:" + intentID);
                        mPlayer.play("spotify:track:" + intentID);

                    }*/

                }

                @Override
                public void onError(Throwable throwable) {
                    Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                }



            });


        }else{
            String intentID = intent.getStringExtra("id").trim();
            intentID = intentID.replace("-AD","");
            Log.d("PLAYER", "PLAYER NOT NULL in onActivityResult new IDx:" + intentID);
            Log.d("PLAYER", "PLAYER NOT NULL in onActivityResult new IDx:" + id);
            if(id.equals(intentID))
                Log.e("PLAYER","BOTH IDs Same");
            else
                Log.e("PLAYER","BOTH NOT SAME");
            mPlayer.pause();
            mPlayer.play(intentID);
            //new AudioTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d("MainActivity", "Playback event received: " + eventType.name());
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {
        Log.d("MainActivity", "Playback error received: " + errorType.name());
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
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

    private class AudioTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            PlayerState ps = new PlayerState();
            try {
                Thread.sleep(10000);
                Log.d("AUDIO","Entering Player State");
                boolean play = true;
                final String[] currentTrackUri = {""};
                final int[] currentPosition = {-1};
                while(play){

                    mPlayer.getPlayerState(new PlayerStateCallback() {
                        @Override
                        public void onPlayerState(PlayerState playerState) {
                            currentTrackUri[0] = playerState.trackUri;
                            currentPosition[0] = playerState.positionInMs;
                        }
                    });

                    Log.d("AUDIO","Player State"+currentPosition[0]);
                    Thread.sleep(4000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}