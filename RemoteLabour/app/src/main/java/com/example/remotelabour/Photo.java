package com.example.remotelabour;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;


public class Photo extends Fragment {
    ImageView imageView;
    String imageName = null;

    public Photo() {
        //CONSTRUCTOR
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_photo, container, false);
        imageView = view.findViewById(R.id.VistaImmagine);
        Bundle data = getArguments();
        if (data != null) {
            imageName = data.getString("imageName");
            if(imageName!=null) {
                File imageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), imageName);
                if (imageFile.exists()) {
                    imageView.setImageURI(Uri.fromFile(imageFile));
                    Log.d("IMMAGINE", imageName);
                } else {
                    //DO nothing
                }
            }
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}