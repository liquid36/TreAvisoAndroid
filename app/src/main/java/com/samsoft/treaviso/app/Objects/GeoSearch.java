package com.samsoft.treaviso.app.Objects;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by sam on 24/04/15.
 */
public class GeoSearch extends AsyncTask<String, Integer, Boolean> {
    private onSearchListener mListener = null;
    private double dlon,dlat;
    public void setOnSearchListener (onSearchListener action)
    {
        mListener = action;
    }

    public InputStream getInputStreamFromUrl(String dir) {
        String url = "http://maps.googleapis.com/maps/api/geocode/xml?address=" + dir + "&sensor=false";
        InputStream content = null;
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
                Log.d("LOCATION", "Error por aca");
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
        if (mListener != null) {
            mListener.onSearchEnd(new GeoPoint(dlat, dlon));
        }

    }


    public interface onSearchListener {
        abstract void onSearchEnd(GeoPoint g);

    }
}


