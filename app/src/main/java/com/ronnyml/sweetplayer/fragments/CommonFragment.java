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

package com.ronnyml.sweetplayer.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ronnyml.sweetplayer.Constants;
import com.ronnyml.sweetplayer.Globals;
import com.ronnyml.sweetplayer.R;
import com.ronnyml.sweetplayer.adapters.SongsAdapter;
import com.ronnyml.sweetplayer.utils.MyImageLoader;
import com.ronnyml.sweetplayer.models.Song;
import com.ronnyml.sweetplayer.ui.MainActivity;
import com.ronnyml.sweetplayer.utils.RoundedNetworkImageView;
import com.ronnyml.sweetplayer.utils.Utils;
import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CommonFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ObservableScrollViewCallbacks {
    private static final String TAG = CommonFragment.class.getSimpleName();
    private Activity mActivity;
    private ArrayList<Song> mSongs = new ArrayList<>();
    private ImageLoader mImageLoader;
    private LinearLayout mTopArtistsLayout;
    private LinearLayout mTopSongsLayout;
    private ObservableListView mSongsListview;
    private ProgressBar mLoadingBar;
    private RoundedNetworkImageView mImageView;

    public static CommonFragment newInstance(int position, String screen) {
        CommonFragment fragment = new CommonFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.POSITION, position);
        args.putString(Constants.SCREEN, screen);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_common, container, false);
        mActivity = this.getActivity();
        mImageLoader = MyImageLoader.getInstance(mActivity).getImageLoader();
        mImageView = (RoundedNetworkImageView) rootView.findViewById(R.id.playlist_image);
        mLoadingBar = (ProgressBar) rootView.findViewById(R.id.loading_bar);
        mSongsListview = (ObservableListView) rootView.findViewById(R.id.songs_listview);
        mTopArtistsLayout = (LinearLayout) rootView.findViewById(R.id.top_artists_layout);
        mTopSongsLayout = (LinearLayout) rootView.findViewById(R.id.top_songs_layout);

        mSongsListview.setScrollViewCallbacks(this);
        if (Utils.isConnectedToInternet(mActivity)) {
            getCommonData();
        } else {
            Utils.showAlertDialog(
                    mActivity,
                    mActivity.getString(R.string.internet_no_connection_title),
                    mActivity.getString(R.string.internet_no_connection_message));
        }

        return rootView;
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        if (scrollState == ScrollState.UP) {
            if (MainActivity.actionBar.isShowing()) {
                MainActivity.actionBar.hide();
            }

            if (mTopArtistsLayout.isShown()) {
                mTopArtistsLayout.setVisibility(View.GONE);
            }

            if (PlayerFragment.playerRelativeLayout.isShown()) {
                PlayerFragment.playerRelativeLayout.setVisibility(View.GONE);
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (!MainActivity.actionBar.isShowing()) {
                MainActivity.actionBar.show();
            }

            if (!mTopArtistsLayout.isShown()) {
                mTopArtistsLayout.setVisibility(View.VISIBLE);
            }

            if (!PlayerFragment.playerRelativeLayout.isShown() && Globals.isPlayerServiceRunning) {
                PlayerFragment.playerRelativeLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    private void getCommonData() {
        Intent intent = mActivity.getIntent();
        if (intent != null) {
            String url = "";
            if (intent.getBooleanExtra(Constants.IS_PLAYLIST, false)) {
                url = Constants.PLAYLIST_DETAIL_URL;
            } else if (intent.getBooleanExtra(Constants.IS_ARTIST, false)) {
                url = Constants.ARTISTS_DETAIL_URL;
            } else if (intent.getBooleanExtra(Constants.IS_GENRE, false)) {
                url = Constants.GENRE_DETAIL_URL;
            }

            String image = intent.getStringExtra(Constants.IMAGE);
            mImageView.setImageUrl(image, mImageLoader);

            String id = intent.getStringExtra(Constants.ID);
            getRequest(url + id);
        }
    }

    private void getRequest(String url) {
        RequestQueue requestQueue = Volley.newRequestQueue(mActivity);
        JsonObjectRequest jsonReq = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        getTopSongs(response);
                        mLoadingBar.setVisibility(View.GONE);
                        mTopSongsLayout.setVisibility(View.VISIBLE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Utils.LogE(TAG, "Error: " + error.getMessage());
                    }
                });
        requestQueue.add(jsonReq);
    }

    private void getTopSongs(JSONObject response) {
        try {
            JSONArray json_array = response.getJSONArray(Constants.SONGS_ITEM);
            for (int i = 0; i < json_array.length(); i++) {
                JSONObject json_object = (JSONObject) json_array.get(i);
                String artist_name = json_object.getString(Constants.ARTIST_NAME);
                String duration = json_object.getString(Constants.DURATION);
                String mp3 = json_object.getString(Constants.MP3);
                String song_name = json_object.getString(Constants.SONG_NAME);

                Song song = new Song(artist_name, duration, mp3, song_name);
                mSongs.add(song);
            }
        } catch (JSONException x) {
            x.printStackTrace();
        }

        SongsAdapter songsAdapter = new SongsAdapter(mActivity, mSongs, false);
        mSongsListview.setAdapter(songsAdapter);
        mSongsListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Globals.currentPosition = position;
                Globals.currentSongs = mSongs;
                PlayerFragment.showPlayer(true);
            }
        });
    }
}