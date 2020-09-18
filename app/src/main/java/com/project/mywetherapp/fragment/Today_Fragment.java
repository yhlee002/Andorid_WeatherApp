package com.project.mywetherapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.project.mywetherapp.R;
import com.project.mywetherapp.util.NotiSettingDialog;

public class Today_Fragment extends Fragment {

    private static Today_Fragment todayFragment = new Today_Fragment();

    public static Today_Fragment newInstance() {
        return todayFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_today, container, false);

        Button setNotiButton = rootView.findViewById(R.id.notiButton);
        setNotiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotiSettingDialog dialog = new NotiSettingDialog(getContext());
                dialog.create();
            }
        });
        return rootView;
    }
}