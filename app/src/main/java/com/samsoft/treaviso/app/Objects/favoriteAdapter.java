package com.samsoft.treaviso.app.Objects;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONObject;

/**
 * Created by sam on 29/04/15.
 */
public class favoriteAdapter extends ArrayAdapter<JSONObject> {
    private final Context context;
    private final JSONObject[] values;

    public favoriteAdapter(Context context, JSONObject[] values) {
        super(context, android.R.layout.simple_list_item_1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        TextView textView = (TextView) rowView.findViewById(android.R.id.text1);
        textView.setTextColor(Color.BLACK);
        try {
            textView.setText(values[position].getString("name"));
        } catch (Exception e) {
            e.printStackTrace();
            textView.setText("Error en el titulo");
        }
        return rowView;
    }
}
