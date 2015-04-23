package com.samsoft.treaviso.app.Fragments;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.samsoft.treaviso.app.R;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.List;


public class mapViewer extends Fragment implements MapEventsReceiver {
    private Activity activity;
    private static final String ARG_PARAM1 = "param1";
    private String mParam1;
    private MapView map;
    private IMapController mapCtl;
    private OnFragmentInteractionListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map_viewer, container, false);
        map = (MapView) v.findViewById(R.id.openmapview);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        mapCtl = map.getController();
        mapCtl.setCenter(getLastLocation());
        mapCtl.setZoom(12);

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(getActivity().getApplicationContext(), this);
        map.getOverlays().add(0, mapEventsOverlay);

        return v;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        /*try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        Marker startMarker = new Marker(map);
        startMarker.setPosition(p);
        startMarker.setDraggable(true);
        startMarker.setIcon(getResources().getDrawable(R.drawable.ic_marker_red));
        map.getOverlays().add(startMarker);
        map.invalidate();
        return false;
    }

    @Override public boolean longPressHelper(GeoPoint p) {
        return false;
    }

    /*
        INTERFACE PARA LA ACTIVITY

     */

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

}
