package com.washmywhip.wmwvendor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ross on 4/2/2016.
 */
public class NavDrawerListAdapter extends ArrayAdapter<NavDrawerItem> {
    private Context context;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private int resourceID;
    private Typeface mFont;

    public NavDrawerListAdapter(Context context, int resource,ArrayList<NavDrawerItem> navDrawerItems ) {
        super(context, resource, navDrawerItems);
        mFont= Typeface.createFromAsset(context.getAssets(), "fonts/Archive.otf");
        this.navDrawerItems = navDrawerItems;
        this.context = context;
        this.resourceID = resource;
    }

    @Override
    public int getCount() {
        return navDrawerItems.size();
    }

    @Override
    public NavDrawerItem getItem(int position) {
       return navDrawerItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        DrawerItemHolder drawerHolder;
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            drawerHolder = new DrawerItemHolder();

            view = inflater.inflate(resourceID, parent, false);
            drawerHolder.ItemName = (TextView) view
                    .findViewById(R.id.navTitle);
            drawerHolder.icon = (ImageView) view.findViewById(R.id.navIcon);
            view.setTag(drawerHolder);

        } else {
            drawerHolder = (DrawerItemHolder) view.getTag();

        }

        NavDrawerItem item = this.navDrawerItems.get(position);

        drawerHolder.icon.setImageResource(item.getIcon());
        drawerHolder.ItemName.setText(item.getTitle());
        drawerHolder.ItemName.setTypeface(mFont);
        return view;
    }
    private static class DrawerItemHolder {
        TextView ItemName;
        ImageView icon;
    }
}
