/*
 * Copyright (C) 2015 Ronny Yabar Aizcorbe <ronnycontacto@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ronnyml.sweetplayer.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ronnyml.sweetplayer.R;
import com.ronnyml.sweetplayer.models.DrawerItem;

import java.util.ArrayList;

public class DrawerAdapter extends BaseAdapter {
    private ArrayList<DrawerItem> mDrawerItems;
    private LayoutInflater mLayoutInflater;

    public DrawerAdapter(Activity activity, ArrayList<DrawerItem> drawerItems) {
        mDrawerItems = drawerItems;
        mLayoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return mDrawerItems.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        ImageView drawerIconImageView;
        TextView drawerTitleTextView;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_drawer, null);
            viewHolder = new ViewHolder();
            viewHolder.drawerIconImageView = (ImageView) convertView.findViewById(R.id.drawer_icon);
            viewHolder.drawerTitleTextView = (TextView) convertView.findViewById(R.id.drawer_title);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        DrawerItem drawerItem = mDrawerItems.get(position);
        String drawerIcon = drawerItem.getIcon();
        String drawerTitle = drawerItem.getTitle();

        int resourceId = Integer.parseInt(drawerIcon);
        viewHolder.drawerIconImageView.setImageResource(resourceId);
        viewHolder.drawerTitleTextView.setText(drawerTitle);

        return convertView;
    }
}
