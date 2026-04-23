package com.example.musicplayer;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity
        implements PlayListFragment.OnSongSelected {

    public static final String TAG = "CPTR320";
    public static final String PLAY_LIST_KEY = "playlist";
    public static final String PLAY_LIST_INDEX_KEY = "index-key";
    public static final String PLAY_LIST_NAME_KEY = "index-key";
    public String currentPlayListName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (savedInstanceState == null) {
            PlayList playList = createPlayList();
            PlayListFragment headLinesFragment = new PlayListFragment(playList);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, headLinesFragment).commit();
            currentPlayListName = playList.getName();
        } else {
            currentPlayListName = savedInstanceState.getString(PLAY_LIST_NAME_KEY);
        }
        TextView title = findViewById(R.id.playlistTitle);
        title.setText(currentPlayListName);
    }

    private PlayList createPlayList() {
        PlayList mylist = new PlayList("Loris's list");
        Song s1 = new Song(R.drawable.bizarre, R.raw.bizarre, "Bizarre");
        Song s2 = new Song(R.drawable.ghost, R.raw.avalanche, "Avalanche");
        Song s3 = new Song(R.drawable.ghost, R.raw.itsasin, "It's a sin");
        Song s4 = new Song(R.drawable.ghost, R.raw.devil, "Sympathy for the devil");
        mylist.addSong(s1);
        mylist.addSong(s2);
        mylist.addSong(s3);
        mylist.addSong(s4);

        return mylist;
    }

    @Override
    public void onSongSelected(PlayList playList, int position) {
        MediaPlayerFragment mpFragment = new MediaPlayerFragment();
        // Pass the playlist and the position to play to the fragment
        Bundle args = new Bundle();
        args.putInt(PLAY_LIST_INDEX_KEY, position);
        args.putParcelable(PLAY_LIST_KEY, playList);
        mpFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.fragment_container, mpFragment);
        transaction.addToBackStack("mediaplayer");
        transaction.commit();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(PLAY_LIST_NAME_KEY, currentPlayListName);
        super.onSaveInstanceState(outState);
    }
}