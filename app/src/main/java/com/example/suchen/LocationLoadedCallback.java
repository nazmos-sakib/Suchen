package com.example.suchen;

import org.osmdroid.util.GeoPoint;

public interface LocationLoadedCallback {
    void onLocationLoaded(GeoPoint location);
}
