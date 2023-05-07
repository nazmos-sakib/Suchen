package com.example.suchen.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.suchen.BuildConfig;
import com.example.suchen.R;
import com.example.suchen.databinding.FragmentAddBinding;
import com.example.suchen.fragments.Authentication.LoginFragment;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Date;

public class AddFragment extends Fragment {
    private static final String TAG = "AddFragment->";

    private FragmentAddBinding binding;
    private Context activityContext;


    private static final int PICK_IMAGE_REQUEST = 9544;
    // Permissions for accessing the storage
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    GpsMyLocationProvider gpsMyLocationProvider;
    //overlays
    //important
    private IMapController mapController;
    private MyLocationNewOverlay myLocationOverlay;

    private MinimapOverlay mMinimapOverlay;
    private ScaleBarOverlay mScaleBarOverlay;
    private CompassOverlay mCompassOverlay;


    //addToServer dialog box image view global variable
    ImageView chosenImageDialogBOx;

    public AddFragment(Context activityContext) {
        // Required empty public constructor
        this.activityContext = activityContext;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ParseUser currentUser = ParseUser.getCurrentUser();
        //if user logged in continue
        if (currentUser != null) {
            // do stuff with the user
            Log.d(TAG, "onCreate: login user->"+currentUser.getUsername());


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

        } else {
            //user not logged in
            // show the signup or login screen
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer_mainActivity,new LoginFragment())
                    .commit();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding =  FragmentAddBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.setDefaultTileSource();

        //sets initial options such
        binding.mapAddFragment.setBuiltInZoomControls(true);
        binding.mapAddFragment.setMultiTouchControls(true);
        binding.mapAddFragment.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        binding.mapAddFragment.setMinZoomLevel(3.0);
        binding.mapAddFragment.setMaxZoomLevel(19.0); // Latest OSM can go to 21!
        binding.mapAddFragment.getTileProvider().createTileCache();


        //My Location
        GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(activityContext);
        //gpsMyLocationProvider.setLocationUpdateMinDistance(100); // [m]  // Set the minimum distance for location updates
        gpsMyLocationProvider.setLocationUpdateMinTime(100);   // [ms] // Set the minimum time interval for location updates
        //mMyLocationOverlay = new MyLocationNewOverlay(MapViewerOsmDroid.this.getBaseContext(), gpsMyLocationProvider, mMap);
        myLocationOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, binding.mapAddFragment);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();
        myLocationOverlay.setDrawAccuracyEnabled(true);
        myLocationOverlay.setPersonAnchor(3f,4f);
        binding.mapAddFragment.getOverlays().add(this.myLocationOverlay);

        this.mapController = binding.mapAddFragment.getController();
        mapController.setZoom(17f);
        //mapController.setCenter(myLocationOverlay.getMyLocation());

        binding.mapAddFragment.setExpectedCenter(myLocationOverlay.getMyLocation());

        addEventListenerOverlay();

        binding.fabStandPositionAddFragment.setOnClickListener(View->{
            mapController.setZoom(18f);
            mapController.setCenter(myLocationOverlay.getMyLocation());
        });

    }



    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        binding.mapAddFragment.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        binding.mapAddFragment.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }


    public void setDefaultTileSource(){
        binding.mapAddFragment.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
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

                Log.d(TAG, "longTapConfirmedHelper: geo point-> " + p.getLatitude() + ", " + p.getLongitude());
                //mapController.setCenter(p);

                Marker m = new Marker(binding.mapAddFragment);
                m.setPosition(new GeoPoint(p.getLatitude(),p.getLongitude()));
                m.setTextLabelBackgroundColor(
                        Color.TRANSPARENT
                );
                m.setTextLabelForegroundColor(
                        Color.RED
                );
                m.setTitle("this is the best place");
                m.setTextLabelFontSize(40);
                //m.setTextIcon("text");
                m.setIcon(getResources().getDrawable(R.drawable.ic_baseline_water_drop_24));

                m.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker, MapView mapView) {
                        Toast.makeText(activityContext, "Marker clicked: " + marker.getTitle(), Toast.LENGTH_SHORT).show();

                        showAddClearDialog(m);

                        return true; // Return true to consume the event
                    }
                });

                m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP);
                binding.mapAddFragment.getOverlays()
                        .add(m);

                return false;
            }
        };

        MapEventsOverlay eventsOverlay = new MapEventsOverlay(activityContext, mapEventsReceiver);
        binding.mapAddFragment.getOverlays().add(eventsOverlay);
    }

    private void showAddClearDialog(Marker m) {
        AlertDialog  alertDialog  = new AlertDialog.Builder(activityContext)
                .setTitle("Add")
                .setMessage("you can clear the marker by clicking CLEAR button. " +
                        "If you want to add this location to the map click ADD button")
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Clear any data or input fields
                    }
                })
                .create();


        // Inflate the view containing the EditText and Button
        LayoutInflater inflater = LayoutInflater.from(activityContext);
        View view = inflater.inflate(R.layout.add_fragment_add_clear_dialog, null);

        // Set the view to the AlertDialog
        alertDialog.setView(view);

        Button addBtn = view.findViewById(R.id.btn_add_dialog);
        addBtn.setOnClickListener(View->{
            showAddToServerDialog(m);
        });

        //if click dismiss alert dialog and and the marker overlay
        Button clearBtn = view.findViewById(R.id.btn_clear_dialog);
        clearBtn.setOnClickListener(View->{
        alertDialog.dismiss();
        binding.mapAddFragment.getOverlays().remove(m);
        });


        alertDialog.show();
    }
    private void showAddToServerDialog(Marker m) {
        // Create the second AlertDialog object and set the title
        AlertDialog locationDetailsDialog = new AlertDialog.Builder(activityContext)
                .setTitle("Details")
                .setMessage("you can clear the marker by clicking CLEAR button. " +
                        "If you want to add this location to the map click ADD button")
                .create();

        // Inflate the view containing the EditText and Button
        LayoutInflater inflater = LayoutInflater.from(activityContext);
        View view = inflater.inflate(R.layout.add_fragment_add_to_server_dialog, null);

        // Set the view to the AlertDialog
        locationDetailsDialog.setView(view);

        //chose image to upload text view.
        TextView uploadImg = view.findViewById(R.id.tv_upload_dialogbox);
        uploadImg.setOnClickListener(View->{
            pick();
        });

        //show chosen image
        //defined in a global variable so that it can be accessed from onActivityResult function
        chosenImageDialogBOx = view.findViewById(R.id.iv_locationPic_dialogbox);


        //
        Button btnSubmit = view.findViewById(R.id.btn_submit_dialogbox);
        btnSubmit.setOnClickListener(View->{
            Log.d(TAG, "showAddToServerDialog: -> submit button pressed");
            // Handle the save button click
            // Get the text from the EditText
            EditText titleEditView = view.findViewById(R.id.ev_title_diologbox);
            EditText descriptionEditView = view.findViewById(R.id.ev_description_dialogbox);
            TextView warningEditView = view.findViewById(R.id.tv_warning_dialogbox);

            String title = titleEditView.getText().toString();
            String description = descriptionEditView.getText().toString();

            //checking radioButton
            RadioButton isFountainActiveView = view.findViewById(R.id.radioButtonYes_dialogbox);
            //Boolean isFountainActive = isFountainActiveView.isChecked()? true


            byte[] data = null;

            if (chosenImageDialogBOx.getDrawable() != null){
                Bitmap bitmap = ((BitmapDrawable) chosenImageDialogBOx.getDrawable()).getBitmap();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                data = baos.toByteArray();
            }

            if (title.isEmpty()){
                warningEditView.setVisibility(android.view.View.VISIBLE);
            } else {
                warningEditView.setVisibility(android.view.View.INVISIBLE);
                Log.d(TAG, "showAddToServerDialog: -> before inserting");
                ParseObject fountainAddToServer = new ParseObject("fountainLocation");
                fountainAddToServer.put("title",title); //Column name and value
                fountainAddToServer.put("description",description);
                fountainAddToServer.put("location", new ParseGeoPoint( m.getPosition().getLatitude(), m.getPosition().getLongitude()));
                fountainAddToServer.put("userWhoAddedThis",ParseUser.getCurrentUser().getObjectId());
                fountainAddToServer.put("isCurrentlyActive",isFountainActiveView.isChecked());
                //combining string and timestamp for naming file
                fountainAddToServer.put("image", new ParseFile("image-"+new Timestamp(new Date().getTime()).toString(), data));

                fountainAddToServer.saveInBackground(new SaveCallback() { //upload file to the parse server
                    //using this call back function will return extra information like if it failed or succeed to upload the file
                    @Override
                    public void done(ParseException e) {
                        if (e==null) { // no error occurred
                            Log.d(TAG, "parse server upload->done: -> Succeed");
                            locationDetailsDialog.cancel();
                        } else {
                            e.getMessage();
                            e.getStackTrace();
                        }
                    }
                });
            }



            // Do something with the text
            /*Toast.makeText(activityContext,
                    text1 + ", " + text2,
                    Toast.LENGTH_SHORT).show();*/
        });

        // upon clicking cancel button it will close this dialog box
        Button btnCancel = view.findViewById(R.id.btn_cancel_dialogbox);
        btnCancel.setOnClickListener(View->{
            locationDetailsDialog.cancel();
        });



        // Create and show the AlertDialog
        locationDetailsDialog.show();

    }

    // Method for starting the activity for selecting image from phone storage
    public void pick() {
        //verifyStoragePermissions(getActivity());
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Open Gallery"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            // Get the selected image URI and set it to the ImageView in the dialog box
            Uri imageUri = data.getData();
            Log.d(TAG, "onActivityResult: ->"+imageUri.toString());
            chosenImageDialogBOx.setImageURI(imageUri);
        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

}