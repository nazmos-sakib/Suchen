package com.example.suchen.Model;

import com.example.suchen.MainActivity;
import com.example.suchen.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class Map {
    private static final String TAG = "Map->";

    private MapView mapView;
    private Context ctx;

    GpsMyLocationProvider gpsMyLocationProvider;
    //overlays
    //important
    private IMapController mapController;
    private MyLocationNewOverlay myLocationOverlay;

    private MinimapOverlay mMinimapOverlay;
    private ScaleBarOverlay mScaleBarOverlay;
    private CompassOverlay mCompassOverlay;

    public Map(MapView mapView, Context ctx) {
        this.mapView = mapView;
        this.ctx = ctx;

        this.mapController = mapView.getController();
        initMyLocationOverlay();
    }

    public Map() {
    }

    public void setDefaultConfiguration(){
        mapView.setTilesScaledToDpi(true);
        this.setMAPNIKTileSource();

        //Changing the loading tile grid colors
        mapView.getOverlayManager().getTilesOverlay().setLoadingBackgroundColor(android.R.color.black);
        mapView.getOverlayManager().getTilesOverlay().setLoadingLineColor(Color.argb(255,0,255,0));

        //scales tiles to the current screen's DPI, helps with readability of labels
        mapView.setTilesScaledToDpi(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapController.setZoom(17f);
        //add a compass overlay
        addCompassOverlay();

        //on click get the clicked position geo
        addEventListenerOverlay();

        //change PersonIcon
        changePersonIcon(R.drawable.ic_baseline_circle_24);

        //setting
        enableRotationGestureOverlay();
        scaleBarOverlay();
        addMiniMap();

    } //end default config

    private void initMyLocationOverlay(){

        //My Location overlay
        //not setting the location change listener
        //this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(ctx), binding.map);
        //old code
        //this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(ctx), binding.map){
        //defining the gps properties separately
        gpsMyLocationProvider = new GpsMyLocationProvider(ctx);
        gpsMyLocationProvider.setLocationUpdateMinDistance(10000); // [m]  // Set the minimum distance for location updates
        gpsMyLocationProvider.setLocationUpdateMinTime(10000);   // [ms] // Set the minimum time interval for location updates
        this.myLocationOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, mapView){
            @Override
            public void onLocationChanged(Location location, IMyLocationProvider source) {
                super.onLocationChanged(location, source);

                // This is where you can use updated location
                mapController.setZoom(13f);
                mapController.setCenter(new GeoPoint(location));
            }
        };
        //this.myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.setDrawAccuracyEnabled(true);


        //this.mLocationOverlay.getMyLocation();
        /*
        if (this.myLocationOverlay.getMyLocation()!=null){
            mapController.setCenter(this.myLocationOverlay.getMyLocation());
        }
        */

        /*
        this.myLocationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {

                if(myLocationOverlay.getMyLocation()!=null){
                    ((MainActivity)ctx).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: ->"+myLocationOverlay.getMyLocation().getLatitude());
                            mapView.getController().animateTo(myLocationOverlay.getMyLocation());
                            Location location = myLocationOverlay.getLastFix();
                            *//*
                            lat = mLocationOverlay.getMyLocation().getLatitude();
                            lon = mLocationOverlay.getMyLocation().getLongitude();*//*
                            GeoPoint g = new GeoPoint(location);
                            //mapController.setCenter(MainActivity.this.mLocationOverlay.getMyLocation());
                            mapController.setCenter(g);

                        }
                    });
                }
            }
        });*/
        // Register a LocationChangedListener with the MyLocationNewOverlay
        //mLocationOverlay.onLocationChanged();

        mapView.getOverlays().add(this.myLocationOverlay);

    }

    public void setDefaultTileSource(){
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
    }
    public void setMAPNIKTileSource(){
        mapView.setTileSource(TileSourceFactory.MAPNIK);
    }
    public void setUSGS_SATTileSource(){
        mapView.setTileSource(TileSourceFactory.USGS_SAT);
    }
    public void setUSGS_TOPOTileSource(){
        mapView.setTileSource(TileSourceFactory.USGS_TOPO);
    }
    public void setCycleMapTileSource(){
        //mapView.setTileSource(TileSourceFactory.CYCLEMAP);
    }

    private void addCompassOverlay(){
        mCompassOverlay = new CompassOverlay(ctx, new InternalCompassOrientationProvider(ctx), mapView);
        mCompassOverlay.enableCompass();
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        mCompassOverlay.setCompassCenter( (float) dm.widthPixels/3-6f,300f);
        //mCompassOverlay.setCompassCenter( 100f,10f);
        mapView.getOverlays().add(mCompassOverlay);
    }

    private void addEventListenerOverlay(){
        //----------------------------------------------------------
        //on click get the clicked position geo
        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                Toast.makeText(ctx,
                        p.getLatitude() + ", " + p.getLongitude(),
                        Toast.LENGTH_SHORT).show();
                Log.d(TAG, "singleTapConfirmedHelper: geo point-> " + p.getLatitude() + ", " + p.getLongitude());
                //mapController.setCenter(p);
                return false;
            }
        };

        MapEventsOverlay eventsOverlay = new MapEventsOverlay(ctx, mapEventsReceiver);
        mapView.getOverlays().add(eventsOverlay);
    }

    private void changePersonIcon(int drawableToSet){

        // Create a new BitmapDrawable with your desired icon image
        Drawable iconDrawable = ctx.getResources().getDrawable(drawableToSet);
        // Convert the VectorDrawable to a Bitmap
        Bitmap iconBitmap = Bitmap.createBitmap(iconDrawable.getIntrinsicWidth(), iconDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(iconBitmap);
        iconDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        iconDrawable.draw(canvas);
        Paint paint = new Paint();
        ColorFilter filter = new LightingColorFilter(Color.BLACK, Color.BLACK);
        paint.setColorFilter(filter);
        canvas.drawBitmap(iconBitmap, 0, 0, paint);
        myLocationOverlay.setPersonIcon( iconBitmap);
    }

    private void enableRotationGestureOverlay(){
        //enable rotation gestures
        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(ctx, mapView);
        mRotationGestureOverlay.setEnabled(true);
        mapView.setMultiTouchControls(true);
        mapView.getOverlays().add(mRotationGestureOverlay);
    }

    private void scaleBarOverlay(){

        //Map Scale bar overlay
        // how high above is the camera
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        mScaleBarOverlay = new ScaleBarOverlay(mapView);
        mScaleBarOverlay.setCentred(true);
        // x,y position for setting the overlay
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 20);
        mScaleBarOverlay.setTextSize(50.0f);
        mScaleBarOverlay.setEnableAdjustLength(true);
        mScaleBarOverlay.enableScaleBar();
        //mScaleBarOverlay.
        mapView.getOverlays().add(mScaleBarOverlay);
    }

    private void addMiniMap(){
        //add the built-in Minimap
        mMinimapOverlay = new MinimapOverlay(ctx, mapView.getTileRequestCompleteHandler());
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        mMinimapOverlay.setWidth(dm.widthPixels / 5);
        mMinimapOverlay.setHeight(dm.heightPixels / 5);
        mMinimapOverlay.setZoomDifference(3); // mini map 3 unit zoomed out than original view
        //optionally, you can set the minimap to a different tile source
        mMinimapOverlay.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.getOverlays().add(mMinimapOverlay);
    }
    public void setMiniMapZoomDifference(int z){
        mMinimapOverlay.setZoomDifference(z); // mini map 3 unit zoomed out than original view
    }

    public void setZoom(float z){
        mapController.setZoom(z);
    }

    public void setGpsLocationUpdateDistance(int d){
        gpsMyLocationProvider.setLocationUpdateMinDistance(d); // [m]  // Set the minimum distance for location updates
    }

    public void setGpsLocationUpdateTime(int t){
        gpsMyLocationProvider.setLocationUpdateMinTime(t);   // [ms] // Set the minimum time interval for location updates
    }

    public MapView getMapView() {
        return mapView;
    }

    public IMapController getMapController() {
        return mapController;
    }

    public MyLocationNewOverlay getMyLocationOverlay() {
        return myLocationOverlay;
    }

    public GpsMyLocationProvider getGpsMyLocationProvider() {
        return gpsMyLocationProvider;
    }

    public MinimapOverlay getMinimapOverlay() {
        return mMinimapOverlay;
    }

    public ScaleBarOverlay getScaleBarOverlay() {
        return mScaleBarOverlay;
    }

    public CompassOverlay getCompassOverlay() {
        return mCompassOverlay;
    }

    public void invalidateMapView() {
        mapView.invalidate();
    }
}
