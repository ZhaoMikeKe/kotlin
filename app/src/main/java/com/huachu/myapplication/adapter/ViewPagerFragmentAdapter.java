package com.huachu.myapplication.adapter;

import com.blankj.utilcode.util.ToastUtils;
import com.huachu.myapplication.fragment.TestSeeStudentAnswerFragment;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerFragmentAdapter extends FragmentStateAdapter {

    private ArrayList<Fragment> arrayList = new ArrayList<>();


    public ViewPagerFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new TestSeeStudentAnswerFragment();
            case 1:
                return new TestSeeStudentAnswerFragment();
            case 2:
                return new TestSeeStudentAnswerFragment();

        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}