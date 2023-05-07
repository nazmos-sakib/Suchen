package com.example.suchen;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;

import com.example.suchen.databinding.ActivityMainBinding;
import com.example.suchen.fragments.AddFragment;
import com.example.suchen.fragments.BookmarksFragment;
import com.example.suchen.fragments.HomeFragment;
import com.example.suchen.fragments.MapFragment;
import com.example.suchen.fragments.SearchFragment;
import com.example.suchen.fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import android.Manifest;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity->";
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    //view variable
    private ActivityMainBinding binding;
    private Fragment fragmentView;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setOnTouchClickListenerToRootView();

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(getResources().getString(R.string.toolbar_title));
        }


        //initViews();
        initBottomNavView();
        //if permission granted load default fragment
        askUserPermission();


    }

    private void replaceFragment(Fragment fragment){
     /*   FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer_mainActivity,fragment);
        fragmentTransaction.commit();
    */
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer_mainActivity,fragment)
                .commit();
    }

    private void initViews(){
    }

    private void initBottomNavView(){
        Log.d(TAG, "initBottomNavView: started");

        //initial selected item
        binding.bottomNavigationViewMainActivity.setSelectedItemId(R.id.menu_items_map);
        //initial fragment
        //replaceFragment(new MapFragment(getApplicationContext()));

        binding.bottomNavigationViewMainActivity.setOnItemSelectedListener(item ->{
            switch (item.getItemId()){
                case R.id.menu_items_search:
                    //
                    replaceFragment(new SearchFragment(MainActivity.this));
                    //replaceFragment(new MapFragment(MainActivity.this));
                    break;
                case R.id.menu_items_map:
                    //
                    replaceFragment(new MapFragment(MainActivity.this));
                    break;

                case R.id.menu_items_add:
                    //
                    replaceFragment(new AddFragment(MainActivity.this));

                    break;


                case R.id.menu_items_save:
                    //
                    replaceFragment(new BookmarksFragment(MainActivity.this));
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

    private void askUserPermission(){
        // Check if the app has permission to access the user's location
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //first time denied. ask again for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("We need permission for getting Current location")
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                        REQUEST_LOCATION_PERMISSION);
                            }
                        })
                        .show();
            } else { //first time ask for permission
                // If the app doesn't have permission, request it from the user
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
            }
        } else { //permission is already granted
            // load default fragment
            replaceFragment(new MapFragment(this));
        }

    }

    // Override the onRequestPermissionsResult() method to handle the user's response
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with accessing the user's location
                // load default fragment
                replaceFragment(new MapFragment(MainActivity.this));
            } else {
                // Permission denied, handle the error or inform the user
                startActivity(new Intent(MainActivity.this,PermissionDeniedActivity.class));
            }
        }
    }

    //set keyboard hiding functionality
    //it calls another function: hideKeyboard();
    @SuppressLint("ClickableViewAccessibility")
    private void setOnTouchClickListenerToRootView(){
        //View rootView = findViewById(android.R.id.root_container_mainActivity);
        View rootView = binding.rootContainerMainActivity;
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });
    }

    //implements the functionality of hiding keyboard upon clicking anywhere in the page
    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }


}