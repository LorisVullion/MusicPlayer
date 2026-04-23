package com.example.musicplayer;

import static com.example.musicplayer.MainActivity.PLAY_LIST_INDEX_KEY;
import static com.example.musicplayer.MainActivity.PLAY_LIST_KEY;

import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MediaPlayerFragment extends Fragment {

    private MediaPlayer mediaPlayer = new MediaPlayer();

    private Timer timer = new Timer();
    private TimerTask timerTask;

    private PlayerState currentState = PlayerState.Idle;
    private final String CURR_POSITION_KEY = "currentPosition";
    private final String PLAYER_STATE_KEY = "playerState";
    private final String SONG_INDEX_KEY = "songIndex";
    private int mCurrentPosition = 0;

    // Parameters supplied by the calling activity
    private int currentIndex = 0;
    private PlayList playList;

    private enum PlayerState {
        Prepared,
        Playing,
        Stopped,
        Paused,
        Looping,
        Idle
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mediaplayer_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null ) {
            currentIndex = args.getInt(PLAY_LIST_INDEX_KEY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                playList = args.getParcelable(PLAY_LIST_KEY, PlayList.class);
            }
        }
        if (savedInstanceState != null) {
            currentState = PlayerState.values()[savedInstanceState.getInt(PLAYER_STATE_KEY)];
            currentIndex = savedInstanceState.getInt(SONG_INDEX_KEY);
            setupControls(view);
            setupMediaPlayer(view);
            setupTimer(view);
            updateCurrentSong(view);
            try {
                mediaPlayer.prepare();
                mediaPlayer.seekTo(savedInstanceState.getInt(CURR_POSITION_KEY));
                if (currentState == PlayerState.Playing)
                    mediaPlayer.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            setupControls(view);
            setupMediaPlayer(view);
            updateCurrentSong(view);
            mediaPlayer.prepareAsync();
            setupTimer(view);
        }
        // TODO -- we need to check the saved instance for fragment restarts
        super.onViewCreated(view, savedInstanceState);
    }

    private void setupTimer(View view) {
        final SeekBar seekBar = view.findViewById(R.id.seekBar);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    try {
                        if (mediaPlayer.isPlaying())
                            seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    }catch (IllegalStateException e) {
                        Log.w(MainActivity.TAG, "Media state is in wrong state!");
                    }
                }
            }
        };

        timer.schedule(task, 50, 100);
    }

    private void setupMediaPlayer(View view) {
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                if (currentState == PlayerState.Playing) {
                    mediaPlayer.start();
                } else if (currentState == PlayerState.Idle) {
                    currentState = PlayerState.Prepared;
                }
                // TODO -- set max and min of the seekbar
                SeekBar seekBar = view.findViewById(R.id.seekBar);
                seekBar.setMax(mediaPlayer.getDuration());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    seekBar.setMin(0);
                }
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Switch shuffleButton = view.findViewById(R.id.shuffle_switch);
                if (        shuffleButton.isChecked()) {
                    int newIndex = currentIndex;

                    while (currentIndex == newIndex) {
                        newIndex = new Random().nextInt(playList.size());
                    }

                    currentIndex = newIndex;
                } else {
                    currentIndex = ++currentIndex % playList.size();
                }

                updateCurrentSong(view);
                try {
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                mediaPlayer.start();
            }
        });

    }

    private void updateCurrentSong(View view) {
        Song song = playList.getSongs()[currentIndex];
        ImageView container = view.findViewById(R.id.imageView);
        Drawable drawable = AppCompatResources.getDrawable(requireContext(),
                song.getPicture());
        container.setImageDrawable(drawable);

        if (currentState == PlayerState.Playing) {
            mediaPlayer.reset();
        }

        try (AssetFileDescriptor fd = getResources()
                .openRawResourceFd(song.getData())) {
            mediaPlayer.setDataSource(fd);
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Error on reading song!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void setupControls(View view) {
        ImageButton playButton = view.findViewById(R.id.play_button);
        ImageButton pauseButton = view.findViewById(R.id.pause_button);
        ImageButton stopButton = view.findViewById(R.id.stop_button);
        SeekBar seekBar = view.findViewById(R.id.seekBar);


        // Tie callbacks to the buttons

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentState == PlayerState.Prepared
                    || currentState == PlayerState.Paused) {
                    mediaPlayer.start();;
                    currentState = PlayerState.Playing;
                }
                if (currentState == PlayerState.Idle) {
                    Toast.makeText(requireContext(),
                            "Please wait!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentState == PlayerState.Playing) {
                    mediaPlayer.pause();
                    currentState = PlayerState.Paused;
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO -- take care of timer stuff

                FragmentManager fm = requireActivity()
                        .getSupportFragmentManager();
                fm.popBackStack();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });
    }

    @Override
    public void onStop() {
        mCurrentPosition = mediaPlayer.getCurrentPosition();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(PLAYER_STATE_KEY, currentState.ordinal());
        outState.putInt(SONG_INDEX_KEY, currentIndex);
        outState.putInt(CURR_POSITION_KEY, mCurrentPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        mediaPlayer.stop();
        mediaPlayer.release();
        timer.cancel();
        super.onDetach();
    }
}
