package com.example.remotelabour;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class PlaySound extends Fragment {

    static Button pause;
    static Button stop;
    static PlaySoundService soundService = null;

    public PlaySound(){
        //CONSTRUCTOR
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play_sound, container, false);

        pause = view.findViewById(R.id.PlayOrPause);
        stop = view.findViewById(R.id.Stop);


        pause.setOnClickListener(v -> {
            soundService = ((MainActivity)getActivity()).passSoundService();
            if(soundService!=null){
                MediaPlayer mediaPlayer = soundService.passMediaPlayer();
                if(mediaPlayer!=null){
                    if(mediaPlayer.isPlaying()){
                        soundService.pause();
                        pause.setText("RESUME");
                    }else{
                        soundService.riprendi();
                        pause.setText("PAUSE");
                    }
                }
            }
        });

        stop.setOnClickListener(v -> ((MainActivity) getActivity()).closeServiceSound());

        return view;
    }
}