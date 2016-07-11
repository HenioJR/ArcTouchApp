package com.publictransportation.model;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.publictransportation.R;

/**
 * ArrayAdapter to show list of routes like that: shortName - longName. 
 * To do that, this class is working with Route objects.
 * 
 * @author Henio
 * @since 2014/02
 */
public class RoutesArrayAdapter extends ArrayAdapter<Route> {

    private Context context;
    private List<Route> routes;

    public RoutesArrayAdapter(Context context, int textViewResourceId, List<Route> routes) {
        super(context, textViewResourceId, routes);
        this.context = context;
        this.routes = routes;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.layout_list_routes, null);
        }

        Route route = this.routes.get(position);
        if (route != null) {
            TextView itemView = (TextView) view.findViewById(R.id.shortAndLongNameOfRoute);
            if (itemView != null) {
                itemView.setText(String.format(route.getShortName() + " - " + route.getLongName()));
            }
         }

        return view;
    }
}