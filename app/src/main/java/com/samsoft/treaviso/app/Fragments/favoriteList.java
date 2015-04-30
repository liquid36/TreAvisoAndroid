package com.samsoft.treaviso.app.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.samsoft.treaviso.app.Objects.DataBase;
import com.samsoft.treaviso.app.Objects.favoriteAdapter;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link favoriteListListener}
 * interface.
 */
public class favoriteList extends ListFragment {
    private DataBase db;
    private favoriteListListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public favoriteList() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JSONArray arr = db.getFavoritos();
        JSONObject [] adapter = new JSONObject[arr.length()];
        for(int i = 0; i < arr.length();i++)
            try {adapter[i] = arr.getJSONObject(i);} catch (Exception e) {e.printStackTrace();}
        setListAdapter(new favoriteAdapter(getActivity().getApplicationContext(),adapter));
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        db = new DataBase(activity.getApplicationContext());
        try {
            mListener = (favoriteListListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFavoriteClick((JSONObject) getListAdapter().getItem(position));
        }
    }

    public interface favoriteListListener {
        public void onFavoriteClick(JSONObject id);
    }

}
