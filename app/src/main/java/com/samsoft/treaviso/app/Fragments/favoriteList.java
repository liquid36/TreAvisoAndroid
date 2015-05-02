package com.samsoft.treaviso.app.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.samsoft.treaviso.app.Objects.DataBase;
import com.samsoft.treaviso.app.Objects.favoriteAdapter;
import com.samsoft.treaviso.app.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


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
    private favoriteAdapter madapter;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public favoriteList() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        madapter = new favoriteAdapter(getActivity().getApplicationContext(),new ArrayList<JSONObject>());
        recalcularAdapter();
        setListAdapter(madapter);

    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
        getListView().setOnItemLongClickListener(longClick);
    }

    public void recalcularAdapter()
    {
        JSONArray arr = db.getFavoritos();
        madapter.clear();
        for(int i = 0; i < arr.length();i++)
            try {madapter.add(arr.getJSONObject(i));} catch (Exception e) {e.printStackTrace();}
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
            mListener.onFavoriteClick((JSONObject) getListAdapter().getItem(position));
        }
    }

    public interface favoriteListListener {
        public void onFavoriteClick(JSONObject id);
    }

    private AdapterView.OnItemLongClickListener longClick = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long id) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getActivity().getApplicationContext().getString(R.string.txt_dalete_favorite));
            builder.setMessage(getActivity().getApplicationContext().getString(R.string.txt_pregunta_borrar));
            builder.setPositiveButton(R.string.btn_aceptar,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    JSONObject o = (JSONObject) getListAdapter().getItem(position);
                    try {
                        db.removeFavorito(o.getInt("id"));
                        recalcularAdapter();
                        madapter.notifyDataSetChanged();
                    } catch (Exception e) {e.printStackTrace();}
                }
            });
            builder.setNegativeButton(R.string.btn_cancelar,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.show();
            return false;
        }
    };

}
