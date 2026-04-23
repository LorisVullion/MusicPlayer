package com.example.musicplayer;

import static com.example.musicplayer.MainActivity.PLAY_LIST_KEY;
import static com.example.musicplayer.MainActivity.TAG;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;

import java.util.Objects;

public class PlayListFragment extends ListFragment {

    public interface OnSongSelected {
        void onSongSelected(PlayList pLayList, int position);
    }

    private OnSongSelected mListener;

    private PlayList playList;

    public PlayListFragment() {
        // To keep the framework happy
    }

    public PlayListFragment(PlayList playList) {
        this.playList = playList;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        try {
            mListener = (OnSongSelected) context;
        } catch (ClassCastException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        int layout = android.R.layout.simple_list_item_activated_1;
        if (savedInstanceState != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                playList = savedInstanceState.getParcelable(PLAY_LIST_KEY, PlayList.class);
            }
        }
        setListAdapter(new ArrayAdapter<String>(requireActivity(),
                layout, playList.getTitles()));
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v,
                                int position, long id) {
        // daisy chain the call
        mListener.onSongSelected(playList, position);
        // set the selected item highlighted
        getListView().setItemChecked(position, true);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(PLAY_LIST_KEY, playList);
        super.onSaveInstanceState(outState);
    }
}
