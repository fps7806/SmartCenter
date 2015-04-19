package com.example.such.smartcenter;

import com.echonest.api.v4.Artist;
import com.echonest.api.v4.ArtistParams;
import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Params;
import com.echonest.api.v4.Song;
import com.echonest.api.v4.SongParams;
import com.echonest.api.v4.Track;

import java.util.List;

public class EchoNestHelper {

    private EchoNestAPI en;

    public EchoNestHelper() throws EchoNestException {
        en = new EchoNestAPI("3JOYDQYGY0GW8SI41");
        en.setTraceSends(true);
        en.setTraceRecvs(false);
    }

    public void dumpSong(Song song) throws EchoNestException {
        System.out.printf("%s\n", song.getTitle());
        System.out.printf("   artist: %s\n", song.getArtistName());
        System.out.printf("   duration   : %.3f\n", song.getDuration());
        System.out.printf("   Tempo   : %.3f\n", song.getTempo());
        System.out.printf("   Mode  : %d\n", song.getMode());
        System.out.printf("   Song hottness : %.3f\n", song.getSongHotttnesss());
        System.out.printf("   Artist hotttness : %.3f\n", song.getArtistHotttnesss());
        System.out.printf("   Artist familarity : %.3f\n", song.getArtistFamiliarity());
        System.out.printf("   Artist location : %s\n", song.getArtistLocation());
       	//storeSongAnalysisInFile(song);

    }

    /*private void storeSongAnalysisInFile(Song song) {
    	try (Writer writer = new BufferedWriter(new OutputStreamWriter(
	              new FileOutputStream("TrackAnalysis/"+song.getTitle()+" by "+song.getArtistName()+".txt"), "utf-8"))) {
    			
    		writer.write("Average Loudness:"+song.getLoudness()+"\n\n");
    		List<Segment> segments = song.getAnalysis().getSegments();
    		int i=0;
    		Iterator<Segment> it = segments.iterator();
    		while(it.hasNext()){
    			Segment s = it.next();
    			writer.write("Start: "+s.getStart()+"\n"+
    							"Duration: "+s.getDuration()+"\n"+
    							"Max Loudness: "+s.getLoudnessMax()+"\n\n");
    			//i++;
    		}
		    		
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EchoNestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}*/

    public List<Song> searchSong(String artist, String title, int results)
            throws EchoNestException {
        SongParams p = new SongParams();

        if(artist!=null && !artist.isEmpty() && artist.length()>0)
            p.setArtist(artist);

        if(title!=null && !title.isEmpty() && title.length()>0)
            p.setTitle(title);

        p.includeAudioSummary();
        p.includeArtistHotttnesss();
        p.includeSongHotttnesss();
        p.includeArtistFamiliarity();
        p.includeArtistLocation();
        p.setResults(results);

        List<Song> songs = en.searchSongs(p);
        return songs;
    }

	public void searchSongsByArtist(String artist, int results)
            throws EchoNestException {
        SongParams p = new SongParams();
        p.setArtist(artist);
        p.includeAudioSummary();
        p.includeArtistHotttnesss();
        p.includeSongHotttnesss();
        p.includeArtistFamiliarity();
        p.includeArtistLocation();
        p.setResults(results);
        p.sortBy("song_hotttnesss", false);


        List<Song> songs = en.searchSongs(p);
        for (Song song : songs) {
            dumpSong(song);
            System.out.println();
        }
    }

    public void searchSongsByTempo(String artist, int results)
            throws EchoNestException {
        Params p = new Params();
        p.add("artist", artist);
        p.add("bucket", "audio_summary");
        p.add("results", results);
        p.add("sort", "tempo-asc");

        List<Song> songs = en.searchSongs(p);
        for (Song song : songs) {
            System.out.printf("%.0f %s %s\n", song.getTempo(), song
                    .getArtistName(), song.getTitle());
        }
    }

    public void searchForFastestSongsByArtist(String artist, int results)
            throws EchoNestException {
        ArtistParams ap = new ArtistParams();
        ap.addName(artist);
        List<Artist> artists = en.searchArtists(ap);
        if (artists.size() > 0) {
            Params p = new Params();
            p.add("artist_id", artists.get(0).getID());
            p.add("bucket", "audio_summary");
            p.add("results", results);
            p.add("sort", "tempo-desc");

            List<Song> songs = en.searchSongs(p);
            for (Song song : songs) {
                System.out.printf("%.0f %s %s\n", song.getTempo(), song
                        .getArtistName(), song.getTitle());
            }
        }
    }

    public void searchSongsByTitle(String title, int results)
            throws EchoNestException {
        Params p = new Params();
        p.add("title", title);
        p.add("results", results);
        List<Song> songs = en.searchSongs(p);
        for (Song song : songs) {
            dumpSong(song);
            System.out.println();
        }
    }

    public Double getTempo(String artistName, String title)
            throws EchoNestException {
        SongParams p = new SongParams();
        p.setArtist(artistName);
        p.setTitle(title);
        p.setResults(1);
        p.includeAudioSummary();
        List<Song> songs = en.searchSongs(p);
        if (songs.size() > 0) {
            double tempo = songs.get(0).getTempo();
            return Double.valueOf(tempo);
        } else {
            return null;
        }
    }

    public void stats() {
        en.showStats();
    }

    public void searchSongsWithIDSpace(String artist, String title, int results)
            throws EchoNestException {
        SongParams p = new SongParams();
        p.setArtist(artist);
        //p.setLimitAny();
        p.includeTracks();
        p.setResults(results);
        p.addIDSpace("rdio-US");
        p.addIDSpace("spotify-WW");
        
        if(title!=null && !title.isEmpty() && title.length()>0)
        	p.setTitle(title);

        List<Song> songs = en.searchSongs(p);
        for (Song song : songs) {
            System.out.printf("%s\n", song.getTitle());
            System.out.printf("   artist: %s\n", song.getArtistName());
            Track rdioTrack = song.getTrack("rdio-US");
            if (rdioTrack != null) {
                System.out.printf("Rdio FID %s\n", rdioTrack.getForeignID());
            }

            Track spotifyTrack = song.getTrack("spotify-WW");
            if (spotifyTrack != null) {
                System.out.printf("Spotify FID %s\n", spotifyTrack.getForeignID());
            }

            System.out.println();
        }
    }
}