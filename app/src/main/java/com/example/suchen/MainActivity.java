package com.example.suchen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.suchen.databinding.ActivityMainBinding;
import com.example.suchen.databinding.FragmentAddBinding;
import com.example.suchen.fragments.AddFragment;
import com.example.suchen.fragments.BookmarksFragment;
import com.example.suchen.fragments.HomeFragment;
import com.example.suchen.fragments.MapFragment;
import com.example.suchen.fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity->";
    //view variable
    private ActivityMainBinding binding;
    private Fragment fragmentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //initViews();
        initBottomNavView();

        //FragmentAddBinding


    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer_mainActivity,fragment);
        fragmentTransaction.commit();
    }

    private void initViews(){
    }

    private void initBottomNavView(){
        Log.d(TAG, "initBottomNavView: started");

        //initial selected item
        binding.bottomNavigationViewMainActivity.setSelectedItemId(R.id.menu_items_map);
        //initial fragment
        replaceFragment(new MapFragment());

        binding.bottomNavigationViewMainActivity.setOnItemSelectedListener(item ->{
            switch (item.getItemId()){
                case R.id.menu_items_map:
                    //
                    replaceFragment(new MapFragment());
                    break;

                case R.id.menu_items_add:
                    //
                    replaceFragment(new AddFragment());

                    break;


                case R.id.menu_items_save:
                    //
                    replaceFragment(new BookmarksFragment());
                    break;

                case R.id.menu_items_settings:
                    //
                    replaceFragment(new SettingsFragment());
                    break;
                default:
                    break;
            }
            return true;
        });
    } //end of initBottomNavView()


}