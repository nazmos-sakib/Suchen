package com.example.suchen.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.suchen.BuildConfig;
import com.example.suchen.LocationLoadedCallback;
import com.example.suchen.MainActivity;
import com.example.suchen.Model.Map;
import com.example.suchen.R;
import com.example.suchen.databinding.FragmentMapBinding;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MapFragment extends Fragment implements LocationLoadedCallback {
    private static final String TAG = "MapFragment->";

    //private MapView map = null;

    FragmentMapBinding binding;

    Context ctx;

    private double lat=0.0, lon=0.0;

    Map map;



    public MapFragment(Context ctx) {
        // Required empty public constructor
        this.ctx = ctx;
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
        //return inflater.inflate(R.layout.fragment_map, container, false);

        binding = FragmentMapBinding.inflate(inflater,container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        //Adjust the size of the cache on disk The primary usage is downloaded map tiles
        //this will set the disk cache size in MB to 1GB , 9GB trim size
        //OpenStreetMapTileProviderConstants. (1000L, 900L);
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));


        map = new Map(binding.map,ctx);
        map.initMyLocationOverlay(this);
        map.setDefaultConfiguration();
        //map.setUSGS_SATTileSource();
        map.getGpsMyLocationProvider().setLocationUpdateMinDistance(1f);

        map.getMapView().addMapListener(new MapListener() {

            @Override
            public boolean onScroll(ScrollEvent event) {
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                //getNearByFountainLocation();
                Log.d(TAG, "onZoom: its changing");
                return false;
            }
        });

        //binding.map.addOnFirstLayoutListener();


        //floating action button
        setFABClickListener();

        //setting popUp menu for layer



    }

    private void getNearByFountainLocation() {

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



    private void setFABClickListener(){
        binding.fabStandPositionMainActivity.setBackgroundDrawable(ContextCompat.getDrawable(ctx, R.drawable.floating_button_bg));
        binding.fabStandPositionMainActivity.setOnClickListener(View -> {

            map.setZoom(13f);
            map.getMapController().setCenter(map.getMyLocationOverlay().getMyLocation());
        });

        binding.fabLayer.setOnClickListener(View->{

            PopupMenu popupMenu = new PopupMenu(getContext(), View);
            popupMenu.getMenuInflater().inflate(R.menu.menu_map_layout_option, popupMenu.getMenu());
            popupMenu.setGravity(2);
            popupMenu.setForceShowIcon(true);
            for (int i = 0; i < popupMenu.getMenu().size(); i++) {
                MenuItem item = popupMenu.getMenu().getItem(i);
                SpannableString spannableString = new SpannableString(item.getTitle().toString());
                spannableString.setSpan(new TextAppearanceSpan(ctx, R.style.PopupMenuTextAppearance), 0, spannableString.length(), 0);
                item.setTitle(spannableString);
            }
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_layout_standArd:
                            // Do something when option 1 is clicked
                            map.setDefaultTileSource();
                            return true;
                        case R.id.menu_layout_satellite:
                            // Do something when option 2 is clicked
                            map.setUSGS_SATTileSource();
                            return true;
                        case R.id.menu_layout_cancel:
                            // Do something when option 2 is clicked
                            return false;
                        default:
                            return false;
                    }
                }
            });
            popupMenu.show();
        });
    }

    @Override
    public void onLocationLoaded(GeoPoint location) {

       /* //your items
        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        items.add(new OverlayItem("Title", "Description", new GeoPoint(0.0d,0.0d))); // Lat/Lon decimal degrees

        //the overlay
        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        //do something
                        return true;
                    }
                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                }, context);
        mOverlay.setFocusItemsOnTap(true);

        mMapView.getOverlays().add(mOverlay);*/

    // get the bounding box of the visible area
        BoundingBox boundingBox = map.getMapView().getBoundingBox();

    // calculate the visible area in square kilometers
        double area = boundingBox.getLatitudeSpan() * boundingBox.getLongitudeSpan() * Math.cos(Math.toRadians(boundingBox.getCenter().getLatitude())) * 111.319;
        Log.d(TAG, "getNearByFountainLocation: ->calculating "+area);


        ParseQuery<ParseObject> query = new ParseQuery<>("fountainLocation");
        query.whereWithinKilometers("location",new ParseGeoPoint(location.getLatitude(),location.getLongitude()),30);
        query.findInBackground((objects, e) -> {
            if (e==null){
                Log.d(TAG, "getNearByFountainLocation: ->"+objects.size());
                for (ParseObject obj: objects){

                    Marker m = new Marker(map.getMapView());
                    GeoPoint mLocation = new GeoPoint(obj.getParseGeoPoint("location").getLatitude(),obj.getParseGeoPoint("location").getLongitude());
                    m.setPosition(mLocation);
                    m.setTextLabelBackgroundColor(
                            Color.TRANSPARENT
                    );
                    m.setTextLabelForegroundColor(
                            Color.RED
                    );
                    m.setTitle(obj.getString("title"));
                    m.setSubDescription(obj.getString("description"));
                    m.setTextLabelFontSize(40);
                    //m.setTextIcon("text");
                    m.setIcon(getResources().getDrawable(R.drawable.ic_baseline_water_drop_24));
                    m.setPanToView(true);
                    m.setRotation(360f);


                    ParseFile a = obj.getParseFile("image");
                    a.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {
                            if (e == null) {
                                // The image data was successfully retrieved
                                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                // Use the bitmap as needed
                                Drawable drawable = new BitmapDrawable(getResources(), bitmap);

                                m.setImage(drawable);

                            } else {
                                // There was an error retrieving the image data
                                Log.e(TAG, "Error loading image data: " + e.getMessage());
                            }
                        }
                    });

                    /*m.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker, MapView mapView) {
                            return false;
                        }
                    });*/
                    m.setDraggable(true);
                    m.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
                        @Override
                        public void onMarkerDrag(Marker marker) {

                        }

                        @Override
                        public void onMarkerDragEnd(Marker marker) {

                        }

                        @Override
                        public void onMarkerDragStart(Marker marker) {
                            showAddClearDialog(m);
                        }
                    });



                    MarkerInfoWindow infoWindow = new MarkerInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble , map.getMapView());
                    m.setInfoWindow(infoWindow);


                    m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    map.getMapView().getOverlays()
                            .add(m);

                }
            } else {
                Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    //on marker single click call this function
    private void showAddClearDialog(Marker m) {
        AlertDialog alertDialog  = new AlertDialog.Builder(ctx)
                .setTitle("Find Direction")
                .setMessage("you can clear the marker by clicking CLEAR button. " +
                        "If you want to go to this location, click Show Direction")
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Clear any data or input fields
                    }
                })
                .create();


        // Inflate the view containing the EditText and Button
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.map_fragment_direction_clear_dialog, null);

        // Set the view to the AlertDialog
        alertDialog.setView(view);

        Button directionBtn = view.findViewById(R.id.btn_direction_map_dialog);
        directionBtn.setOnClickListener(View->{
            //showAddToServerDialog(m,alertDialog);



/*

            RoadManager roadManager = new OSRMRoadManager(ctx);
            ArrayList<GeoPoint> waypoints = new ArrayList<>();
            waypoints.add(startPoint);
            waypoints.add(endPoint);
            Road road = roadManager.getRoad(waypoints);

*/

            alertDialog.cancel();

        });

        //if click dismiss alert dialog and and the marker overlay
        Button addBookmarkBtn = view.findViewById(R.id.btn_book_mark_map_dialog);
        addBookmarkBtn.setOnClickListener(View->{

            GeoPoint location = m.getPosition();

            ParseQuery<ParseObject> query1  = new ParseQuery<ParseObject>("Bookmarks");
            query1.whereEqualTo("user_id", ParseUser.getCurrentUser().getObjectId());
            query1.whereEqualTo("location",new ParseGeoPoint(location.getLatitude(),location.getLongitude()));
            query1.setLimit(1);
            query1.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e==null){
                        if (objects.size()>0){  //already bookmarked
                            Log.d(TAG, "done: -> this place is already bookmarked");
                        } else {  //not bookmarked
                            //proceeded to upload the bookmark

                            //step 1:
                            //find details about the coordinate
                            Geocoder geocoder;
                            List<Address> addresses = null;
                            geocoder = new Geocoder(getContext(), Locale.getDefault());

                            try {
                                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }

                            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                            String city = addresses.get(0).getLocality();
                            String state = addresses.get(0).getAdminArea();
                            String country = addresses.get(0).getCountryName();
                            String postalCode = addresses.get(0).getPostalCode();
                            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                            //step 2:
                            //upload
                            ParseObject bookmarks = new ParseObject("Bookmarks");
                            bookmarks.put("title", city+" - "+country);
                            bookmarks.put("details",knownName + "," + address +", " + state +", " + city+" - "+country);
                            bookmarks.put("user_id", ParseUser.getCurrentUser().getObjectId());

                            //location upload
                            ParseGeoPoint pGeoPoint = new ParseGeoPoint( m.getPosition().getLatitude(), m.getPosition().getLongitude());
                            bookmarks.put("location", pGeoPoint);

                            bookmarks.saveInBackground(new SaveCallback() { //upload file to the parse server
                                //using this call back function will return extra information like if it failed or succeed to upload the file
                                @Override
                                public void done(ParseException e) {
                                    if (e==null) { // no error occurred
                                        //upload successful
                                        alertDialog.dismiss();
                                    } else {
                                        e.getMessage();
                                        e.getStackTrace();
                                    }
                                }
                            });

                        }
                    }

                }
            });



            //alertDialog.dismiss();
            //map.getMapView().getOverlays().remove(m);
        });

        //if click dismiss alert dialog and and the marker overlay
        Button clearBtn = view.findViewById(R.id.btn_clear_map_dialog);
        clearBtn.setOnClickListener(View->{
            alertDialog.dismiss();
            //map.getMapView().getOverlays().remove(m);
        });

        alertDialog.show();
    }

}