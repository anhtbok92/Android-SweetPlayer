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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.ronnyml.sweetplayer.Constants;
import com.ronnyml.sweetplayer.Globals;
import com.ronnyml.sweetplayer.R;
import com.ronnyml.sweetplayer.adapters.CommonAdapter;
import com.ronnyml.sweetplayer.adapters.SongsAdapter;
import com.ronnyml.sweetplayer.models.Common;
import com.ronnyml.sweetplayer.models.Song;
import com.ronnyml.sweetplayer.ui.MainActivity;
import com.ronnyml.sweetplayer.utils.Utils;
import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class SearchFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ObservableScrollViewCallbacks {
    private static final String TAG = SearchFragment.class.getSimpleName();
    private Activity mActivity;
    private ArrayList<Common> mArtists = new ArrayList<>();
    private ArrayList<Song> mSongs = new ArrayList<>();
    private EditText mEditText;
    private CommonAdapter mGeneralAdapter;
    private ImageButton mSearchButton;
    private LinearLayout mTopArtistsLayout;
    private LinearLayout mTopSongsLayout;
    private ObservableListView mSongsListview;
    private ProgressBar mLoadingBar;
    private RecyclerView mArtistsRecyclerView;
    private SongsAdapter mSongsAdapter;

    public static SearchFragment newInstance(int position, String screen) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.POSITION, position);
        args.putString(Constants.SCREEN, screen);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        mActivity = this.getActivity();
        mArtistsRecyclerView = (RecyclerView) rootView.findViewById(R.id.top_artists_recyclerview);
        mEditText = (EditText) rootView.findViewById(R.id.search_edit_text);
        mLoadingBar = (ProgressBar) rootView.findViewById(R.id.loading_bar);
        mSearchButton = (ImageButton) rootView.findViewById(R.id.search_button);
        mSongsListview = (ObservableListView) rootView.findViewById(R.id.songs_listview);
        mTopArtistsLayout = (LinearLayout) rootView.findViewById(R.id.top_artists_layout);
        mTopSongsLayout = (LinearLayout) rootView.findViewById(R.id.top_songs_layout);

        LinearLayoutManager mArtistsLinearLayoutManager = new LinearLayoutManager(mActivity.getApplicationContext());
        mArtistsLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mArtistsRecyclerView.setLayoutManager(mArtistsLinearLayoutManager);
        mArtistsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mSongsListview.setScrollViewCallbacks(this);
        setupSearchButton();

        if (Utils.isConnectedToInternet(mActivity)) {
            Intent intent = mActivity.getIntent();
            if (intent != null) {
                if (intent.getBooleanExtra(Constants.IS_SEARCH, false)) {
                    search(intent.getStringExtra(Constants.SEARCH_TEXT));
                } else {
                    getRequest(Constants.ARTISTS_URL, 0);
                    getRequest(Constants.TOP_URL, 1);
                }
            }
        } else {
            Utils.showAlertDialog(
                    mActivity,
                    mActivity.getString(R.string.internet_no_connection_title),
                    mActivity.getString(R.string.internet_no_connection_message));
        }

        return rootView;
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
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

            if (!mTopArtistsLayout.isShown() && !mArtists.isEmpty()) {
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

    private void clearViews() {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mArtists.clear();
                mArtistsRecyclerView.setAdapter(null);
                if (mGeneralAdapter != null) {
                    mGeneralAdapter.notifyDataSetChanged();
                }

                mSongs.clear();
                mSongsListview.setAdapter(null);
                if (mSongsAdapter != null) {
                    mSongsAdapter.notifyDataSetChanged();
                }

            }
        });
    }

    private void getRequest(String url, final int operation) {
        RequestQueue requestQueue = Volley.newRequestQueue(mActivity);
        JsonObjectRequest jsonReq = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        switch (operation) {
                            case 0:
                                getTopArtists(response);
                                if (mArtists.size() == 0) {
                                    mTopArtistsLayout.setVisibility(View.GONE);
                                } else {
                                    mTopArtistsLayout.setVisibility(View.VISIBLE);
                                }
                                break;
                            case 1:
                                getTopSongs(response);
                                mLoadingBar.setVisibility(View.GONE);
                                mTopSongsLayout.setVisibility(View.VISIBLE);
                                break;
                        }
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

    private void getTopArtists(JSONObject response) {
        try {
            JSONArray json_array = response.getJSONArray(Constants.ARTISTS_ITEM);
            for (int i = 0; i < json_array.length(); i++) {
                JSONObject json_object = (JSONObject) json_array.get(i);
                String id = json_object.getString(Constants.ID);
                String image = json_object.getString(Constants.IMAGE);
                String name = json_object.getString(Constants.ARTIST_NAME);

                Common artist = new Common(id, image, name);
                mArtists.add(artist);
            }

            if (!mArtists.isEmpty()) {
                mGeneralAdapter = new CommonAdapter(mActivity, Constants.IS_ARTIST, mArtists, false);
                mArtistsRecyclerView.setAdapter(mGeneralAdapter);
            }
        } catch (JSONException x) {
            x.printStackTrace();
        }
    }

    private void getTopSongs(JSONObject response) {
        try {
            JSONArray json_array = response.getJSONArray(Constants.SONGS_ITEM);
            for (int i = 0; i < json_array.length(); i++) {
                JSONObject json_object = (JSONObject) json_array.get(i);
                String artist_name = json_object.getString(Constants.ARTIST_NAME);
                String duration = json_object.getString(Constants.DURATION);
                String image = json_object.getString(Constants.IMAGE);
                String mp3 = json_object.getString(Constants.MP3);
                String song_name = json_object.getString(Constants.SONG_NAME);

                Song song = new Song(artist_name, duration, image, mp3, song_name);
                mSongs.add(song);
            }
        } catch (JSONException x) {
            x.printStackTrace();
        }

        if (!mSongs.isEmpty()) {
            mSongsAdapter = new SongsAdapter(mActivity, mSongs, true);
            mSongsListview.setAdapter(mSongsAdapter);
            mSongsListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Globals.currentPosition = position;
                    Globals.currentSongs = mSongs;
                    PlayerFragment.showPlayer(true);
                }
            });
        } else {
            Utils.showUserMessage(mActivity, mActivity.getString(R.string.search_no_results));
        }
    }

    private void search(String search_text) {
        Utils.hideSoftKeyboard(mActivity);
        String encodedSearchText = "";

        try {
            encodedSearchText = URLEncoder.encode(search_text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        clearViews();

        String final_url = Constants.SEARCH_MUSIC_URL + encodedSearchText;
        Utils.LogD(TAG, "Final URL: " + final_url);

        if (Utils.isConnectedToInternet(mActivity)) {
            mLoadingBar.setVisibility(View.VISIBLE);
            getRequest(final_url, 0);
            getRequest(final_url, 1);
        } else {
            Utils.showAlertDialog(
                    mActivity,
                    mActivity.getString(R.string.internet_no_connection_title),
                    mActivity.getString(R.string.internet_no_connection_message));
        }
    }

    private void setupSearchButton() {
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                String search_text = mEditText.getText().toString();
                if (search_text.equals("") || search_text.length() == 0) {
                    Utils.showUserMessage(
                            mActivity.getApplicationContext(),
                            getString(R.string.search_text_empty));
                } else {
                    search(search_text);
                }
            }
        });
    }
}