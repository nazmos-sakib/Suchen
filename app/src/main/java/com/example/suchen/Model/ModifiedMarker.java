package com.example.suchen.Model;

import android.view.MotionEvent;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class ModifiedMarker extends Marker {
    public ModifiedMarker(MapView mapView) {
        super(mapView);
    }

    @Override
    public boolean onLongPress(MotionEvent event, MapView mapView) {
        return super.onLongPress(event, mapView);
    }
}
