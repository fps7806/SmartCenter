package com.example.such.smartcenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Segment;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Such on 4/18/2015.
 */
public class SpotifyModule extends CenterModule implements PlayerNotificationCallback, ConnectionStateCallback {
    public static SpotifyModule Self;
    private AudioTask audioTask;

    public ArrayList<LightModule> connectedTo = new ArrayList<>();

    public SpotifyModule() {
        Self = this;
    }

    public Player mPlayer = null;
    public String accessToken = "";
    public Song currentSong;
    public Track currentTrack;
    String id = "1QUOoDUouDDTAwzjIong25";
    public static final String CLIENT_ID = "9d86b7b41cc04f0299f770842bb5cd3c";
    private static final String REDIRECT_URI = "spotify://callback";

    @Override
    public int GetResourceId() {
        return R.drawable.ic_spotify;
    }

    @Override
    public void OnClick(ActionBarActivity context, ImageView view) {
        context.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container,new SpotifyFragment() )
                .addToBackStack(null)
                .commit();
    }

    @Override
    public View InflateUniversal(LayoutInflater inflater, ViewGroup root) {
        View view =  inflater.inflate(R.layout.item_control_spotify, root, false);

        ImageButton play = (ImageButton)view.findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(audioTask != null) {
                    if(audioTask.play)
                        Pause();
                    else
                        Resume();
                }
                else
                    SpotifyModule.this.FindSongAndPlay();
            }
        });

        return view;
    }

    public void FindSongAndPlay() {
        new AsyncTask<String, Void, List<Song>>() {
            @Override
            protected List<Song> doInBackground(String... msgs) {
                String title = "Summer";
                String artist = "Calvin Harris";
                List<Song> songlist = null;
                try {
                    EchoNestHelper echonest = new EchoNestHelper();
                    songlist = echonest.searchSong(artist,title,5);
                } catch (EchoNestException e) {
                    e.printStackTrace();
                }
                return songlist;
            }

            @Override
            protected void onPostExecute(List<Song> songlist)
            {
                PlaySong(songlist.get(2));
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    public void Stop() {
        if(audioTask != null) {
            mPlayer.pause();
            audioTask.play = false;
            audioTask = null;
        }
    }

    public void Pause() {
        if(audioTask != null) {
            mPlayer.pause();
            audioTask.play = false;
        }
    }
    public void Resume() {
        if(audioTask != null) {
            mPlayer.resume();
            audioTask = new AudioTask();
            audioTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void PlaySong(Song s) {
        Stop();
        String spotifyID="";
        AsyncTask<Song, Void, Track> t1 = new AsyncTask<Song, Void, Track>() {
            @Override
            protected Track doInBackground(Song... s) {
                try {
                    return s[0].getTrack("spotify-WW");
                } catch (EchoNestException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, s);

        try {
            Track track = t1.get();
            currentTrack = track;
            AsyncTask<Track, Void, String> tt = new AsyncTask<Track, Void, String>() {
                @Override
                protected String doInBackground(Track... t) {
                    try {
                        if(t.length > 0 && t[0] != null)
                            return t[0].getForeignID();
                    } catch (EchoNestException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, track);

            String id = tt.get();
            if(id == null) {
                Toast.makeText(MainActivity.Self, "Invalid Track", Toast.LENGTH_LONG).show();
                return;
            }
            spotifyID = tt.get().trim();

            System.out.printf("Spotify FID %s\n", spotifyID);
            Log.d("PLAY", "Paused");
            currentSong = s;
            PlaySong(spotifyID);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void CreatePlayer(Context context) {
        Config playerConfig = new Config(context, accessToken, CLIENT_ID);
        Player.Builder pb = new Player.Builder(playerConfig);
        Handler myHandler = new Handler();
        pb.setCallbackHandler(myHandler);
        SpotifyModule.Self.mPlayer = Spotify.getPlayer(pb, this, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                Log.d("PLAYSONG", "entered onInitialized");
                mPlayer.addConnectionStateCallback(SpotifyModule.Self);
                mPlayer.addPlayerNotificationCallback(SpotifyModule.Self);
                Log.d("PLAYER", "in onActivityResult Default ID:" + id);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
            }


        });
    }

    public void PlaySong(String id) {
        Stop();
        Log.d("PLAYSONG", "Entered");

        if(mPlayer!=null) {
            if(id.length()>7 && id.toCharArray()[7]=='-'){
                Log.d("PLAY","before substring:"+id);
                id = id.substring(0,7)+id.substring(10);
            }
            Log.d("PLAY","Id: "+id);
            //mPlayer.pause();
            mPlayer.play(id);
            audioTask = new AudioTask();
            audioTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public Intent CreateLoginIntent(Activity context) {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        return AuthenticationClient.createLoginActivityIntent(context, request);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {

    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {

    }

    @Override
    public void onLoggedIn() {

    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Throwable throwable) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {

    }

    private class AudioTask extends AsyncTask<Void, Void, Void> {
        private boolean loaded = false;
        public boolean play = true;
        Iterator<Segment> iterator;
        final int[] currentPosition = {-1};

        double track_min = Float.MAX_VALUE;
        double track_max = Float.MIN_VALUE;

        double localMin = 0;
        double localMax = 0;
        int samples_count = 0;
        float last_value = 0.5f;
        final int num_samples = 8;
        double[] last_samples =new double[num_samples];
        Segment segment;
        final String[] currentTrackUri = {""};

        @Override
        protected Void doInBackground(Void... params) {
            try {

                if(loaded == false) {
                    loaded = true;
                    iterator = currentTrack.getAnalysis().getSegments().iterator();
                    segment = iterator.next();

                    for (Segment s : currentTrack.getAnalysis().getSegments()) {
                        track_max = Math.max(track_max, s.getLoudnessMax());
                        track_min = Math.min(track_min, s.getLoudnessMax());
                    }
                }
                Log.d("AUDIO","Entering Player State");
                while(play){

                    mPlayer.getPlayerState(new PlayerStateCallback() {
                        @Override
                        public void onPlayerState(PlayerState playerState) {
                            currentTrackUri[0] = playerState.trackUri;
                            currentPosition[0] = playerState.positionInMs;
                        }
                    });


                    double current = currentPosition[0]/1000.0;

                    while(segment.getStart()+segment.getDuration() < current) {
                        if(iterator.hasNext())
                            segment = iterator.next();
                        else {
                            play = false;
                            break;
                        }
                        last_samples[samples_count%num_samples] = segment.getLoudnessMax();
                        localMin = Float.MAX_VALUE;
                        localMax = Float.MIN_VALUE;
                        for(double a : last_samples) {
                            localMax = Math.max(localMax, a);
                            localMin = Math.min(localMin, a);
                        }
                        samples_count++;
                    }
                    double s = segment.getLoudnessMax();
                    localMin = Math.min(s, localMin);
                    localMax = Math.max(s, localMax);

                    double localv = (s-localMin)/(localMax-localMin);
                    double totalv = (s-track_min)/(track_max-track_min);
                    double v = Math.min(totalv * localv + 0.1, 1.0);
                    last_value = (float) (last_value + (v-last_value)*0.5f);
                    for(LightModule m : connectedTo)
                        m.SetBrightness(last_value);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (EchoNestException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
