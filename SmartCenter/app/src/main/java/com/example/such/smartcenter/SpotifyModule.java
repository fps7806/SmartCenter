package com.example.such.smartcenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

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

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Such on 4/18/2015.
 */
public class SpotifyModule extends CenterModule implements PlayerNotificationCallback, ConnectionStateCallback {
    public static SpotifyModule Self;
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
    public void OnClick(ActionBarActivity context) {
        SpotifyFragment fragment = new SpotifyFragment();
        context.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container,fragment )
                .addToBackStack(null)
                .commit();
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
                Song s = songlist.get(2);
                currentSong = s;
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
                }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, s);

                try {
                    Track track = t1.get();
                    currentTrack = track;
                    AsyncTask<Track, Void, String> tt = new AsyncTask<Track, Void, String>() {
                        @Override
                        protected String doInBackground(Track... t) {
                            try {
                                return t[0].getForeignID();
                            } catch (EchoNestException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, track);

                    spotifyID = tt.get().trim();

                    System.out.printf("Spotify FID %s\n", spotifyID);
                    Log.d("PLAY", "Paused");
                    PlaySong(spotifyID);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }


            }
        }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
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
        Log.d("PLAYSONG", "Entered");

        if(mPlayer!=null) {
            if(id.length()>7 && id.toCharArray()[7]=='-'){
                Log.d("PLAY","before substring:"+id);
                id = id.substring(0,7)+id.substring(10);
            }
            Log.d("PLAY","Id: "+id);
            //mPlayer.pause();
            mPlayer.play(id);
            new AudioTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
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

        @Override
        protected Void doInBackground(Void... params) {
            PlayerState ps = new PlayerState();
            try {
                Thread.sleep(1000);
                Log.d("AUDIO","Entering Player State");
                boolean play = true;
                final String[] currentTrackUri = {""};
                final int[] currentPosition = {-1};
                double lastMax = currentTrack.getLoudness();
                float lastValue = 0.5f;
                LightModule.Self.SetBrightness(lastValue);
                Iterator<Segment> iterator = currentTrack.getAnalysis().getSegments().iterator();
                Segment segment = iterator.next();
                double avg = currentTrack.getLoudness();
                final int num_samples = 8;
                double[] last_samples =new double[num_samples];
                double localAverage = 0;
                double localMin = 0;
                double localMax = 0;
                int samples_count = 0;
                float last_value = 0.5f;
                double track_min = Float.MAX_VALUE;
                double track_max = Float.MIN_VALUE;
                for(Segment s : currentTrack.getAnalysis().getSegments())
                {
                    track_max = Math.max(track_max, s.getLoudnessMax());
                    track_min = Math.min(track_min, s.getLoudnessMax());
                }

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
                        localAverage = 0;
                        localMin = Float.MAX_VALUE;
                        localMax = Float.MIN_VALUE;
                        for(double a : last_samples) {
                            localAverage += a;
                            localMax = Math.max(localMax, a);
                            localMin = Math.min(localMin, a);
                        }
                        localAverage /= num_samples;
                        samples_count++;
                    }
                    double s = segment.getLoudnessMax();
                    localMin = Math.min(s, localMin);
                    localMax = Math.max(s, localMax);

                    double localv = (s-localMin)/(localMax-localMin);
                    double totalv = (s-track_min)/(track_max-track_min);
                    double v = totalv * localv;
                    Log.d("Smart", "local " + Double.toString(localv) + " total " + Double.toString(totalv));
                    last_value = (float) (lastValue + (v-last_value)*0.5f);
                    LightModule.Self.SetBrightness(last_value);
                    Thread.sleep(100);
                }
            }catch (EchoNestException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
