package com.project.mywetherapp.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project.mywetherapp.R;

public class Tomorrow_Fragment extends Fragment {

    private static Tomorrow_Fragment tomorrowFragment = new Tomorrow_Fragment();
    public static Tomorrow_Fragment newInstance(){
        return tomorrowFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_tomorrow, container, false);
        return rootView;
    }
}