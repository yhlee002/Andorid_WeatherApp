package com.project.mywetherapp.util;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.project.mywetherapp.fragment.DayAftTomorr_Fragment;
import com.project.mywetherapp.fragment.Today_Fragment;
import com.project.mywetherapp.fragment.Tomorrow_Fragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    public int mCount;

    public ViewPagerAdapter(@NonNull FragmentManager fm, int mCount) {
        super(fm);
        this.mCount = mCount;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        int index = getRealPosition(position);

        if (index == 0) {
            return Today_Fragment.newInstance();
        } else if (index == 1) {
            return Tomorrow_Fragment.newInstance();
        } else {
            return DayAftTomorr_Fragment.newInstance();
        }
    }

    private int getRealPosition(int position) {
        return position % mCount; // 0, 1, 2 가 반복되도록
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
//        super.destroyItem(container, position, object);
    }

    //    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, int mCount) {
//        super(fragmentActivity);
//        this.mCount = mCount;
//    }
//
//    @NonNull
//    @Override
//    public Fragment createFragment(int position) {
//        int index = getRealPosition(position);
//
//        if (index == 0) {
//            return new DayAftTomorr_Fragment();
//        } else if (index == 1) {
//            return new Today_Fragment();
//        } else {
//            return new Tomorrow_Fragment();
//        }
//    }
//
//    private int getRealPosition(int position) {
//        return position % mCount; // 0, 1, 2 가 반복되도록
//    }
//
//    @Override
//    public int getItemCount() {
//        return 2000;
//    }
}
