package com.example.project2;

import java.util.ArrayList;
import java.util.List;

public class WrappedData {
    private String username;
    private int followers;
    private String email;
    private List<String> topTracks;
    private List<String> topArtists;
    private int listeningTime;

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
        listeningTime += time;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("Username: ").append(username).append("\nFollowers: ").append(followers).append("\n Email: ").append(email);
        out.append("\nTop Tracks:\n");
        for (String s : topTracks) {
            out.append('\t').append(s).append('\n');
        }
        out.append("Top Artists:\n");
        for (String s : topArtists) {
            out.append('\t').append(s).append('\n');
        }
        out.append("Listening Time: ").append(listeningTime).append(" Seconds");
        return out.toString();
    }

    public int getTotalTime() {
        return listeningTime;
    }

    public List<String> getTracks() {
        return topTracks;
    }
}