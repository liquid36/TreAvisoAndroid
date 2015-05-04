package com.samsoft.treaviso.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import com.samsoft.treaviso.app.Fragments.favoriteList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class MapViewer extends ActionBarActivity implements favoriteList.favoriteListListener{
    private MapView myOpenMapView;
    private IMapController mapController;
    private double lat,lon;
    private MapOverlay movl;
    private GeoPoint myLocation;
    private Integer metros = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_frame);


        myLocation = new GeoPoint(-32.948081,-60.694156);
        movl = new MapOverlay(this,new MarkerListener());
        movl.addItem(new OverlayItem("MY", "MY", myLocation));

        myOpenMapView = (MapView) findViewById(R.id.openmapview);
        myOpenMapView.setBuiltInZoomControls(true);
        myOpenMapView.setMultiTouchControls(true);
        //myOpenMapView.getOverlays().add(movl);

        Marker startMarker = new Marker(myOpenMapView);
        startMarker.setPosition(new GeoPoint(-30.948081,-60.694156));
        startMarker.setDraggable(true);
        startMarker.setIcon(getResources().getDrawable(R.drawable.ic_marker_red));
        myOpenMapView.getOverlays().add(startMarker);

        mapController = myOpenMapView.getController();
        mapController.setZoom(12);
        mapController.setCenter(new GeoPoint(-32.948081,-60.694156 ));

        Criteria criteria = new Criteria();
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(true);

        LocationListener locationListener = new MyLocationListener();
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lm.requestSingleUpdate(criteria, locationListener, null);

        metros = getIntent().getIntExtra("Metros",400);
    }

    private MenuItem searchMenuItem;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_viewer, menu);
        searchMenuItem = menu.findItem(R.id.act_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                GeolocationTask t = new GeolocationTask();
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

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.act_do:
                Intent resultIntent = new Intent( MapViewer.this , MapViewer.class);
                setResult(Activity.RESULT_OK, resultIntent);
                resultIntent.putExtra("lat",lat);
                resultIntent.putExtra("lon",lon);
                finish();
                return true;
            case R.id.act_back:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void makeToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    public class MarkerListener implements   ItemizedIconOverlay.OnItemGestureListener <OverlayItem> {
        @Override
        public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
            makeToast("hice click"); return true;
        }

        @Override
        public boolean onItemLongPress(final int index, final OverlayItem item) {
            return false;
        }
    }

    public void onFavoriteClick(JSONObject id)
    {

    }

    public class MapOverlay extends ItemizedOverlayWithFocus <OverlayItem> {
        private ItemizedOverlayWithFocus<OverlayItem> anotherItemizedIconOverlay = null;

        public MapOverlay(Context ctx,MarkerListener m) {super(ctx, new ArrayList<OverlayItem>(), m); }

        @Override
        public void draw(Canvas canvas, MapView mapview, boolean arg2)
        {
            //super.draw(canvas, mapview, arg2);
            if (this.size() > 0) {
                for (int i = 0; i < this.size(); i++) {
                    IGeoPoint in = getItem(i).getPoint();
                    Point out = new Point();
                    mapview.getProjection().toPixels(in, out);
                    Bitmap bm;
                    if (getItem(i).getTitle() == "MY") bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_marker_blue);
                    else {
                        // Draw a Circule
                        if (metros > 100) {
                            final float radius = mapview.getProjection().metersToEquatorPixels(metros);
                            Paint mCirclePaint = new Paint();
                            mCirclePaint.setARGB(0, 100, 100, 255);
                            mCirclePaint.setAntiAlias(true);
                            mCirclePaint.setAlpha(50);
                            mCirclePaint.setStyle(Paint.Style.FILL);
                            canvas.drawCircle(out.x, out.y, radius, mCirclePaint);
                        }
                        bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_marker_red);
                    }
                    canvas.drawBitmap(bm, out.x - bm.getWidth() / 2, out.y - bm.getHeight() , null);
                }
            }
        }



        @Override
        public boolean onTouchEvent(MotionEvent e, MapView mapView) {return true; }

        @Override
        public boolean onSingleTapUp(MotionEvent e, MapView mapView) {
            IGeoPoint g;
            if(e.getAction() == MotionEvent.ACTION_UP) {
                g = mapView.getProjection().fromPixels((int) e.getX(),(int) e.getY());
                //Toast.makeText(MapViewer.this, "Lattitude="+ g.getLatitudeE6() / 1E6 + " Longitude="+  g.getLongitudeE6() / 1E6  , Toast.LENGTH_SHORT).show();
                lat = g.getLatitude();
                lon = g.getLongitude();
                this.removeAllItems();
                this.addItem(new OverlayItem("MY", "MY", myLocation));
                this.addItem(new OverlayItem("", "", new GeoPoint(g.getLatitude(), g.getLongitude())));
                mapView.invalidate();
            }
            return false;
        }
    }

    private class GeolocationTask extends AsyncTask<String, Integer, Boolean> {
        public  InputStream getInputStreamFromUrl(String dir) {
            String url = "http://maps.googleapis.com/maps/api/geocode/xml?address=" + dir + "&sensor=false";
            InputStream content = null;
            //Log.d("LOCATION", URLEncoder.encode(url, "UTF-8") );
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response = httpclient.execute(new HttpGet(url.replaceAll(" ", "%20")));
                content = response.getEntity().getContent();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return content;
        }
        private double dlon,dlat;

        protected Boolean doInBackground(String... urls) {
            int count = urls.length;
            long totalSize = 0;
            for (int j = 0; j < count; j++) {
                try {
                    InputStream i = getInputStreamFromUrl(urls[j]);
                    BufferedReader br = new BufferedReader(new InputStreamReader(i));
                    String line;
                    StringBuilder sb = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }

                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    dbFactory.setNamespaceAware(true);
                    Document doc = dBuilder.parse(new InputSource(new StringReader(sb.toString())));
                    NodeList nList = doc.getElementsByTagName("location");

                    if (nList.item(0).getNodeType() == Node.ELEMENT_NODE) {
                        Element e = (Element) nList.item(0);
                        dlat = Double.parseDouble(e.getElementsByTagName("lat").item(0).getTextContent());
                        dlon = Double.parseDouble(e.getElementsByTagName("lng").item(0).getTextContent());
                        return true;
                    }

                } catch (Exception ee ) {
                    Log.d("LOCATION", "Error por aca" );
                    ee.printStackTrace();
                    return false;
                }


            }
            return false;
        }

        protected void onProgressUpdate(Integer... progress) {
            return;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                lat = dlat;
                lon = dlon;
                mapController.setZoom(16);
                mapController.setCenter(new GeoPoint(lat, lon));
                //mapController.animateTo(new GeoPoint(lat, lon));
                movl.removeAllItems();
                movl.addItem(new OverlayItem("MY", "MY", myLocation));
                movl.addItem(new OverlayItem("", "", new GeoPoint( lat, lon)));
                myOpenMapView.invalidate();
            }
            return;
        }
    }


    private final class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location l) {
            myLocation = new GeoPoint(l.getLatitude(),l.getLongitude());
            movl.removeAllItems();
            movl.addItem(new OverlayItem("MY", "MY", myLocation));
            movl.addItem(new OverlayItem("", "", new GeoPoint( lat, lon)));
            myOpenMapView.getController().setCenter(myLocation);
            myOpenMapView.invalidate();
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


}


