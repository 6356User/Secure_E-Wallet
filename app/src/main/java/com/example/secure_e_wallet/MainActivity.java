package com.example.secure_e_wallet;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.secure_e_wallet.adapters.MainBottomNavigationAdapter;
import com.example.secure_e_wallet.databinding.ActivityMainBinding;
import com.example.secure_e_wallet.model.User;
import com.example.secure_e_wallet.utilities.Constants;
import com.example.secure_e_wallet.utilities.PreferenceManager;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // initialize viewbinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Init();
        //events
        eventViewPager2ChangePage();
        eventBottomNavigationItemSelected();
    }

    private void eventBottomNavigationItemSelected() {
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.item_home_main_navigation) {
                    binding.viewpager2.setCurrentItem(0);
                    return true;
                } else if (item.getItemId() == R.id.item_history_main_navigation) {
                    binding.viewpager2.setCurrentItem(1);
                    return true;
                } else if (item.getItemId() == R.id.item_person_main_navigation) {
                    binding.viewpager2.setCurrentItem(2);
                    return true;
                }
                return false;
            }
        });
    }

    private void eventViewPager2ChangePage() {
        binding.viewpager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        binding.bottomNavigation.getMenu().findItem(R.id.item_home_main_navigation).setChecked(true);
                        break;
                    case 1:
                        binding.bottomNavigation.getMenu().findItem(R.id.item_history_main_navigation).setChecked(true);
                        break;
                    case 2:
                        binding.bottomNavigation.getMenu().findItem(R.id.item_person_main_navigation).setChecked(true);
                        break;
                }
            }
        });
    }

    private void Init() {
        MainBottomNavigationAdapter adapter = new MainBottomNavigationAdapter(this);
        binding.viewpager2.setAdapter(adapter);
    }
}