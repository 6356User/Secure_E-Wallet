package com.example.secure_e_wallet.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.secure_e_wallet.fragments.HistoryFragment;
import com.example.secure_e_wallet.fragments.HomeFragment;
import com.example.secure_e_wallet.fragments.PersonFragment;

public class MainBottomNavigationAdapter extends FragmentStateAdapter {

    public MainBottomNavigationAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position){
            case 1:
                fragment = new HistoryFragment();
                break;
            case 2:
                fragment = new PersonFragment();
                break;
            default:
                fragment=new HomeFragment();
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
