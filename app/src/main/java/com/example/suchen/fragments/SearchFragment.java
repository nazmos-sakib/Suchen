package com.example.suchen.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.suchen.BuildConfig;
import com.example.suchen.LocationLoadedCallback;
import com.example.suchen.Model.BookmarkLocationModel;
import com.example.suchen.Model.Map;
import com.example.suchen.R;
import com.example.suchen.databinding.FragmentSearchBinding;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.List;

public class SearchFragment extends Fragment implements LocationLoadedCallback {

    private static final String TAG = "SearchFragment->";
    FragmentSearchBinding binding;

    Context ctx;
    BookmarkLocationModel bookmarkLocationModel;
    Map map;

    private boolean bookmarkFlag = false;
    public SearchFragment(Context ctx) {
        this.ctx = ctx;
    }

    public SearchFragment(BookmarkLocationModel model, Context context) {
        bookmarkLocationModel = model;
        bookmarkFlag = true;
        ctx = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        Configuration.getInstance().load(this.ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //Adjust the size of the cache on disk The primary usage is downloaded map tiles
        //this will set the disk cache size in MB to 1GB , 9GB trim size
        //OpenStreetMapTileProviderConstants. (1000L, 900L);
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        Configuration.getInstance().setOsmdroidBasePath(ctx.getCacheDir());
        Configuration.getInstance().setOsmdroidTileCache(ctx.getCacheDir());

        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's
        //tile servers will get you banned based on this string

        //inflate and create the map

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // search button
        // edit text watcher
        // clear button
        setClickedListener();

        //Adjust the size of the cache on disk The primary usage is downloaded map tiles
        //this will set the disk cache size in MB to 1GB , 9GB trim size
        //OpenStreetMapTileProviderConstants. (1000L, 900L);
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        map = new Map(binding.map,ctx);
        map.initMyLocationOverlay(this);
        map.setDefaultConfiguration();
        //map.setUSGS_SATTileSource();

        //floating action button

        //setting popUp menu for layer


        setOnTouchClickListenerToRootView();


    }

    @Override
    public void onStart() {
        super.onStart();
        if (bookmarkFlag){
            map.getMapController().setCenter(bookmarkLocationModel.getGeoPoint());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        binding.map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        binding.map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    private void setClickedListener(){
        //search button
        binding.btnSearchSearchFragment.setOnClickListener(View->{
            Toast.makeText(getContext(), binding.edtSearchLocationSearchFragment.getText(), Toast.LENGTH_SHORT).show();

            Geocoder geocoder = new Geocoder(ctx);
            try {
                String searchText = binding.edtSearchLocationSearchFragment.getText().toString();
                List<Address> addresses = geocoder.getFromLocationName(searchText, 1);
                if (addresses != null && addresses.size() > 0) {
                    Address address = addresses.get(0);
                    GeoPoint geoPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
                    Log.d(TAG, "setClickedListener: "+address.getCountryName());
                    binding.map.getController().setCenter(geoPoint);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        });

        //edit text
        binding.edtSearchLocationSearchFragment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0){
                    binding.btnClearSearchFragment.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //clear button
        binding.btnClearSearchFragment.setOnClickListener(View->{
            binding.edtSearchLocationSearchFragment.setText("");
            binding.btnClearSearchFragment.setVisibility(android.view.View.INVISIBLE);
        });
    }


    //set keyboard hiding functionality
    //it calls another function: hideKeyboard();
    @SuppressLint("ClickableViewAccessibility")
    private void setOnTouchClickListenerToRootView(){
        //View rootView = findViewById(android.R.id.root_container_mainActivity);
        View rootView = binding.parentContainerSearchFragment;
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
        InputMethodManager inputMethodManager = (InputMethodManager) ctx.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View currentFocus = getActivity().getCurrentFocus();
        if (currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    @Override
    public void onLocationLoaded(GeoPoint location) {

    }
}