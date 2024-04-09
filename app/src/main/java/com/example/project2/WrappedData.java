package com.example.project2;

import java.util.ArrayList;
import java.util.List;

public class WrappedData {
    private String username;

    private int followers;
    private String email;
    private List<String> topTracks;
    private List<String> topArtists;
    private int listeningTimeMS;

    private int sp;

    private List<String> topGenres;

    public void setTopGenres(List<String> topGenres) {
        this.topGenres = topGenres;
    }

    public WrappedData() {
        topTracks = new ArrayList<>();
        topArtists = new ArrayList<>();
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void addTrack(String track) {
        topTracks.add(track);
    }

    public void addArtist(String artist) {
        topArtists.add(artist);
    }

    public void addTime(int time) {
        listeningTimeMS += time;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("Username: ").append(username);
        out.append("\nSP: ").append(sp);
        out.append("\nTop Tracks:\n");
        for (String s : topTracks) {
            out.append('\t').append(s).append('\n');
        }
        out.append("Top Artists:\n");
        for (String s : topArtists) {
            out.append('\t').append(s).append('\n');
        }
        out.append("Genres:\n");
        for (String s : topGenres) {
            out.append('\t').append(s).append('\n');
        }
        out.append("Listening Time: ").append(listeningTimeMS);
        return out.toString();
    }

    private String ts() {
        if (sp == 0) {
            return "Month";
        } else if (sp == 1) {
            return "Half Year";
        } else {
            return "Year";
        }
    }

    public int getTotalTime() {
        return listeningTimeMS;
    }

    public int getListeningTimeMS() {
        return listeningTimeMS;
    }

    public List<String> getTopGenres() {
        return topGenres;
    }

    public List<String> getTopArtists() {
        return topArtists;
    }

    public List<String> getTopTracks() {
        return topTracks;
    }

    public List<String> getTracks() {
        return topTracks;
    }

    public String getUsername() {
        return username;
    }

    public void setSP(int i) {
        sp = i;
    }

    public void setListeningTimeMS(int listeningTimeMS) {
        this.listeningTimeMS = listeningTimeMS;
    }

    public void addGenre(String genre) {
        if (topGenres != null) {
            topGenres.add(genre);
        }
    }

    public int getSp() {
        return sp;
    }
}