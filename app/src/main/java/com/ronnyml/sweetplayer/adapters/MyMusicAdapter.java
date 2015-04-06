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
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ronnyml.sweetplayer.R;
import com.ronnyml.sweetplayer.fragments.MyMusicFragment;
import com.ronnyml.sweetplayer.models.Song;
import com.ronnyml.sweetplayer.utils.Utils;

import java.util.ArrayList;

public class MyMusicAdapter extends BaseAdapter {
    private static final String TAG = MyMusicAdapter.class.getSimpleName();
    private Activity mActivity;
    private ArrayList<Song> mSongs;
    private LayoutInflater mLayoutInflater;

    public MyMusicAdapter(Activity activity, ArrayList<Song> songs) {
        mActivity = activity;
        mLayoutInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSongs = songs;
    }

    public int getCount() {
        return mSongs.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        ImageButton actionsImageButton;
        TextView artistNameTextView;
        TextView songNameTextView;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_my_music, null);
            viewHolder = new ViewHolder();
            viewHolder.actionsImageButton = (ImageButton) convertView.findViewById(R.id.actions_image_button);
            viewHolder.artistNameTextView = (TextView) convertView.findViewById(R.id.artist_name);
            viewHolder.songNameTextView = (TextView) convertView.findViewById(R.id.song_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Song song = mSongs.get(position);
        viewHolder.artistNameTextView.setText(song.getArtistName());
        viewHolder.songNameTextView.setText(song.getSongName());

        mActivity.registerForContextMenu(viewHolder.actionsImageButton);
        viewHolder.actionsImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mActivity.openContextMenu(v);
            }
        });
        viewHolder.actionsImageButton.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(0, 0, 0, mActivity.getString(R.string.dialog_delete));
                menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Utils.LogI(TAG, "Deleting");
                        MyMusicFragment.deleteSong(position);
                        return false;
                    }
                });
            }
        });

        return convertView;
    }
}
