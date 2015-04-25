package com.samsoft.treaviso.app.Fragments;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.samsoft.treaviso.app.Objects.GeoSearch;
import com.samsoft.treaviso.app.Objects.MarkerWithRadius;
import com.samsoft.treaviso.app.R;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.List;


public class mapViewer extends Fragment implements MapEventsReceiver , LocationListener , GeoSearch.onSearchListener {
    public static final String MYPOSITION_LAT_ID = "MYPOSITION_LAT";
    public static final String MYPOSITION_LNG_ID = "MYPOSITION_LNG";
    public static final String CLICKPOSITION_LAT_ID = "CLICKPOSITION_LAT";
    public static final String CLICKPOSITION_LNG_ID = "CLICKPOSITION_LNG";
    public static final String CENTER_LAT_ID = "CENTER_LAT";
    public static final String CENTER_LNG_ID = "CENTER_LNG";
    public static final String RADIUS_ID = "RADIUS";
    public static final String ZOOM_ID = "ZOOM";

    private MenuItem searchMenuItem;
    private Activity activity;
    private MapView map;
    private IMapController mapCtl;

    protected MarkerWithRadius mMarker;
    protected Marker mPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map_viewer, container, false);
        map = (MapView) v.findViewById(R.id.openmapview);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(getActivity().getApplicationContext(), this);
        map.getOverlays().add(0, mapEventsOverlay);


        // Creo el market con la position actual
        GeoPoint p = getLastLocation();
        Integer zoom = 13;
        mPosition = new Marker(map);
        mPosition.setPosition(getLastLocation());
        mPosition.setDraggable(false);
        mPosition.setIcon(getResources().getDrawable(R.drawable.ic_marker_blue));
        mPosition.setAnchor(0.5f,1f);
        map.getOverlays().add(mPosition);
        mMarker = null;


        // Lectura de viejas condiciones
        if (savedInstanceState == null) requestPosition();
        else {
            mPosition.setPosition(new GeoPoint(savedInstanceState.getDouble(MYPOSITION_LAT_ID),savedInstanceState.getDouble(MYPOSITION_LNG_ID)));
            Log.d("MAPFRAG","Contiene: " + savedInstanceState.containsKey(CLICKPOSITION_LAT_ID));
            if (savedInstanceState.containsKey(CLICKPOSITION_LAT_ID))
            {
                mMarker = new MarkerWithRadius(map);
                mMarker.setPosition(new GeoPoint(savedInstanceState.getDouble(CLICKPOSITION_LAT_ID),savedInstanceState.getDouble(CLICKPOSITION_LNG_ID)));
                mMarker.setDraggable(true);
                mMarker.setIcon(getResources().getDrawable(R.drawable.ic_marker_red));
                mPosition.setAnchor(0.5f,1f);
                map.getOverlays().add(mMarker);
            }
            p = new GeoPoint(savedInstanceState.getDouble(CENTER_LAT_ID),savedInstanceState.getDouble(CENTER_LNG_ID));
            zoom = savedInstanceState.getInt(ZOOM_ID);
        }

        mapCtl = map.getController();
        mapCtl.setCenter(p);
        mapCtl.setZoom(zoom);
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map_viewer, menu);
        searchMenuItem = menu.findItem(R.id.act_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                GeoSearch t = new GeoSearch();
                t.setOnSearchListener(mapViewer.this);
                t.execute(s);
                if (android.os.Build.VERSION.SDK_INT >= 14) searchMenuItem.collapseActionView();
                else MenuItemCompat.collapseActionView(searchMenuItem);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    @Override
    public void onSearchEnd(GeoPoint p)
    {
        mapCtl.setCenter(p);
        mapCtl.setZoom(14);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.act_locate:
                mapCtl.animateTo(mPosition.getPosition());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*if (mMarker != null) {
            outState.putDouble(CLICKPOSITION_LAT_ID, mMarker.getPosition().getLatitude());
            outState.putDouble(CLICKPOSITION_LNG_ID, mMarker.getPosition().getLongitude());
            outState.putInt(RADIUS_ID,mMarker.getRadius());
        }
        outState.putDouble(MYPOSITION_LAT_ID, mPosition.getPosition().getLatitude());
        outState.putDouble(MYPOSITION_LNG_ID, mPosition.getPosition().getLongitude());
        outState.putDouble(CENTER_LAT_ID, map.getMapCenter().getLatitude());
        outState.putDouble(CENTER_LNG_ID, map.getMapCenter().getLongitude());
        outState.putInt(ZOOM_ID,map.getZoomLevel());*/
        outState.putAll(getInfo());
    }

    public Bundle getInfo()
    {
        Bundle info = new Bundle();
        if (mMarker != null) {
            info.putDouble(CLICKPOSITION_LAT_ID, mMarker.getPosition().getLatitude());
            info.putDouble(CLICKPOSITION_LNG_ID, mMarker.getPosition().getLongitude());
            info.putInt(RADIUS_ID,mMarker.getRadius());
        }
        info.putDouble(MYPOSITION_LAT_ID, mPosition.getPosition().getLatitude());
        info.putDouble(MYPOSITION_LNG_ID, mPosition.getPosition().getLongitude());
        info.putDouble(CENTER_LAT_ID, map.getMapCenter().getLatitude());
        info.putDouble(CENTER_LNG_ID, map.getMapCenter().getLongitude());
        info.putInt(ZOOM_ID,map.getZoomLevel());
        return info;

    }

    public void requestPosition()
    {
        Criteria criteria = new Criteria();
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        LocationManager lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        lm.requestSingleUpdate(criteria, this, null);
    }

    public GeoPoint getLastLocation()
    {
        float bestAccuracy = Float.MAX_VALUE;
        long minTime = Long.MIN_VALUE,bestTime = Long.MIN_VALUE;
        Location bestResult = null;
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        List<String> matchingProviders = lm.getAllProviders();
        for (String provider: matchingProviders) {
            Location location = lm.getLastKnownLocation(provider);
            if (location != null) {
                float accuracy = location.getAccuracy();
                long time = location.getTime();
                if (time > bestTime ){
                    bestResult = location;
                    bestTime = time;
                }
            }
        }
        return new GeoPoint(bestResult);
    }



    /*
        MAP EVENTS HANDLER

     */

    @Override public boolean singleTapConfirmedHelper(GeoPoint p) {
        if (mMarker == null) {
            mMarker = new MarkerWithRadius(map);
            mMarker.setPosition(p);
            mMarker.setDraggable(true);
            mMarker.setIcon(getResources().getDrawable(R.drawable.ic_marker_red));
            mPosition.setAnchor(0.5f,1f);
            map.getOverlays().add(mMarker);
        } else {
            mMarker.setPosition(p);
        }
        map.invalidate();
        return false;
    }

    @Override public boolean longPressHelper(GeoPoint p) {
        return false;
    }

    /*
        LocationListener

     */

    @Override
    public void onLocationChanged(Location l) {
        mPosition.setPosition(new GeoPoint(l));
        map.invalidate();
    }

    @Override
    public void onProviderDisabled(String provider) {
        // called when the GPS provider is turned off (user turning off the GPS on the phone)
    }

    @Override
    public void onProviderEnabled(String provider) {
        // called when the GPS provider is turned on (user turning on the GPS on the phone)
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // called when the status of the GPS provider changes
    }

}
