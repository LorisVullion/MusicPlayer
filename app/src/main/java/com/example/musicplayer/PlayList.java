package com.example.musicplayer;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;

public class PlayList implements Iterable<Song>, Parcelable {

    private String name;

    private ArrayList<Song> list = new ArrayList<>();

    public PlayList(String name) {
        this.name = name;
    }

    protected PlayList(Parcel in) {
        name = in.readString();
        list = in.createTypedArrayList(Song.CREATOR);
    }

    public static final Creator<PlayList> CREATOR = new Creator<PlayList>() {
        @Override
        public PlayList createFromParcel(Parcel in) {
            return new PlayList(in);
        }

        @Override
        public PlayList[] newArray(int size) {
            return new PlayList[size];
        }
    };

    public void addSong(Song song) {
        list.add(song);
    }

    public void removeSong(Song song) {
        list.remove(song);
    }

    public Song[] getSongs() {
        return list.toArray(new Song[0]);
    }

    public String[] getTitles() {
        String[] titles = new String[list.size()];
        int index = 0;
        for (Song s : list)
            titles[index++] = s.getTitle();
        return titles;
    }

    public String getName() {
        return name;
    }

    @NonNull
    @Override
    public Iterator<Song> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < list.size();
            }

            @Override
            public Song next() {
                return list.get(index++);
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeTypedList(list);
    }

    public int size(){
        return list.size();
    }
}
