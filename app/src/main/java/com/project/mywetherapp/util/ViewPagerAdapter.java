package com.project.mywetherapp.util;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.project.mywetherapp.fragment.DayAftTomorr_Fragment;
import com.project.mywetherapp.fragment.Today_Fragment;
import com.project.mywetherapp.fragment.Tomorrow_Fragment;
// 페이지 슬라이딩을 위해 뷰페이저에 프레그먼트를 연결하는 역할을 수행하는 어댑터
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
    }
}
