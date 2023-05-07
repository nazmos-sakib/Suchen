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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.suchen.BuildConfig;
import com.example.suchen.Model.BookmarkLocationModel;
import com.example.suchen.R;
import com.example.suchen.databinding.FragmentShowBookmarkBinding;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShowBookmarkFragment extends Fragment {

    private static final String TAG = "ShowBookmarkFragment->";
    private FragmentShowBookmarkBinding binding;

    private Context activityContext;
    private BookmarkLocationModel locationModel;
    private GeoPoint geoPoint;

    GpsMyLocationProvider gpsMyLocationProvider;
    //overlays
    //important
    private IMapController mapController;
    private MyLocationNewOverlay myLocationOverlay;

    private MinimapOverlay mMinimapOverlay;
    private ScaleBarOverlay mScaleBarOverlay;
    private CompassOverlay mCompassOverlay;


    public ShowBookmarkFragment(Context ctx , BookmarkLocationModel m) {
        this.activityContext = ctx;
        this.locationModel = m;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //load/initialize the osmdroid configuration, this can be done
        Configuration.getInstance().load(this.activityContext, PreferenceManager.getDefaultSharedPreferences(activityContext));
        //Adjust the size of the cache on disk The primary usage is downloaded map tiles
        //this will set the disk cache size in MB to 1GB , 9GB trim size
        //OpenStreetMapTileProviderConstants. (1000L, 900L);
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        Configuration.getInstance().setOsmdroidBasePath(activityContext.getCacheDir());
        Configuration.getInstance().setOsmdroidTileCache(activityContext.getCacheDir());

        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's
        //tile servers will get you banned based on this string


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding =  FragmentShowBookmarkBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.setDefaultTileSource();
        this.addCompassOverlay(200f);

        binding.mapShowBookmarkFragment.setBuiltInZoomControls(true);
        binding.mapShowBookmarkFragment.setMultiTouchControls(true);
        binding.mapShowBookmarkFragment.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        /*binding.mapShowBookmarkFragment.getZoomController().setOnZoomListener(new CustomZoomButtonsController.OnZoomListener() {
            @Override
            public void onVisibilityChanged(boolean b) {

            }

            @Override
            public void onZoom(boolean b) {
        mMapView.getController().zoomOut();
        mMapView.getController().zoomIn();

            }
        });*/

        this.enableRotationGestureOverlay();
        this.scaleBarOverlay();
        this.addMiniMap();

        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

        Marker startMarker = new Marker(binding.mapShowBookmarkFragment);
        startMarker.setPosition(locationModel.getGeoPoint());
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        startMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_bookmark_24));
        startMarker.setTitle("Start point");
        binding.mapShowBookmarkFragment.getOverlays().add(startMarker);


        this.mapController = binding.mapShowBookmarkFragment.getController();
        mapController.setZoom(17f);
        mapController.setCenter(locationModel.getGeoPoint());

        binding.fabStandPositionShowBookmarkFragment.setOnClickListener(View->{
            mapController.setCenter(startMarker.getPosition());
        });

    }


    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        binding.mapShowBookmarkFragment.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        binding.mapShowBookmarkFragment.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    private void setClickedListener(){
        //search button
        binding.btnSearchShowBookmarkFragment.setOnClickListener(View->{
            Toast.makeText(getContext(), binding.edtSearchLocationShowBookmarkFragment.getText(), Toast.LENGTH_SHORT).show();

            Geocoder geocoder = new Geocoder(activityContext);
            try {
                String searchText = binding.edtSearchLocationShowBookmarkFragment.getText().toString();
                List<Address> addresses = geocoder.getFromLocationName(searchText, 1);
                if (addresses != null && addresses.size() > 0) {
                    Address address = addresses.get(0);
                    GeoPoint geoPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
                    Log.d(TAG, "setClickedListener: "+address.getCountryName());
                    binding.mapShowBookmarkFragment.getController().setCenter(geoPoint);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        });

        //edit text
        binding.edtSearchLocationShowBookmarkFragment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0){
                    binding.btnClearShowBookmarkFragment.setVisibility(View.VISIBLE);
                }
            }


            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //clear button
        binding.btnClearShowBookmarkFragment.setOnClickListener(View->{
            binding.edtSearchLocationShowBookmarkFragment.setText("");
            binding.btnClearShowBookmarkFragment.setVisibility(android.view.View.INVISIBLE);
        });
    }



    public void setDefaultTileSource(){
        binding.mapShowBookmarkFragment.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
    }
    public void setMAPNIKTileSource(){
        binding.mapShowBookmarkFragment.setTileSource(TileSourceFactory.MAPNIK);
    }
    public void setUSGS_SATTileSource(){
        binding.mapShowBookmarkFragment.setTileSource(TileSourceFactory.USGS_SAT);
    }
    public void setUSGS_TOPOTileSource(){
        binding.mapShowBookmarkFragment.setTileSource(TileSourceFactory.USGS_TOPO);
    }
    public void setCycleMapTileSource(){
        //binding.mapShowBookmarkFragment.setTileSource(TileSourceFactory.CYCLEMAP);
    }


    private void addCompassOverlay(float y){
        mCompassOverlay = new CompassOverlay(activityContext, new InternalCompassOrientationProvider(activityContext), binding.mapShowBookmarkFragment);
        mCompassOverlay.enableCompass();
        DisplayMetrics dm = activityContext.getResources().getDisplayMetrics();
        mCompassOverlay.setCompassCenter( (float) dm.widthPixels/3-6f,y);
        //mCompassOverlay.setCompassCenter( 100f,10f);
        binding.mapShowBookmarkFragment.getOverlays().add(mCompassOverlay);
    }


    private void enableRotationGestureOverlay(){
        //enable rotation gestures
        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(activityContext, binding.mapShowBookmarkFragment);
        mRotationGestureOverlay.setEnabled(true);
        binding.mapShowBookmarkFragment.setMultiTouchControls(true);
        binding.mapShowBookmarkFragment.getOverlays().add(mRotationGestureOverlay);
    }

    private void scaleBarOverlay(){

        //Map Scale bar overlay
        // how high above is the camera
        DisplayMetrics dm = activityContext.getResources().getDisplayMetrics();
        mScaleBarOverlay = new ScaleBarOverlay(binding.mapShowBookmarkFragment);
        mScaleBarOverlay.setCentred(true);
        // x,y position for setting the overlay
        //mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, dm.heightPixels-200);
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 20);
        mScaleBarOverlay.setTextSize(50.0f);
        mScaleBarOverlay.setEnableAdjustLength(true);
        mScaleBarOverlay.enableScaleBar();
        //mScaleBarOverlay.
        binding.mapShowBookmarkFragment.getOverlays().add(mScaleBarOverlay);
    }

    private void addMiniMap(){
        //add the built-in Minimap
        mMinimapOverlay = new MinimapOverlay(activityContext, binding.mapShowBookmarkFragment.getTileRequestCompleteHandler());
        DisplayMetrics dm = activityContext.getResources().getDisplayMetrics();
        mMinimapOverlay.setWidth(dm.widthPixels / 5);
        mMinimapOverlay.setHeight(dm.heightPixels / 5);
        mMinimapOverlay.setZoomDifference(4); // mini map 3 unit zoomed out than original view
        //optionally, you can set the minimap to a different tile source
        mMinimapOverlay.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        binding.mapShowBookmarkFragment.getOverlays().add(mMinimapOverlay);
    }
}