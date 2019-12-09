package com.example.kasvikullat;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class NavigationTabAdapter extends FragmentStatePagerAdapter {
    private final List<Fragment> fragmentList = new ArrayList<>();
    private int currentPosition = -1;
    // tabs won't have textTitles at this point
    // private final List<String> titleList = new ArrayList<>();

    public NavigationTabAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    public void addFragment(Fragment fragment) {
        fragmentList.add(fragment);
        // if titles are needed for tabs then String parameter to method
        // and titleList.add(string);
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.setPrimaryItem(container, position, object);
        if (position != currentPosition) {
            Fragment fragment = (Fragment) object;
            HeightWrappingViewPager pager = (HeightWrappingViewPager) container;
            if (fragment.getView() != null) {
                currentPosition = position;
                pager.measureCurrentView(fragment.getView());
            }
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return null;
        // with textTitles return titleList.get(position);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}
