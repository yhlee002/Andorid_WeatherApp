package com.project.mywetherapp.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project.mywetherapp.R;


public class DayAftTomorr_Fragment extends Fragment {

    private static DayAftTomorr_Fragment dayAftTomorrFragment = new DayAftTomorr_Fragment();
    public static DayAftTomorr_Fragment newInstance(){
        return dayAftTomorrFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_day_aft_tomorr, container, false);
        return rootView;
    }

}