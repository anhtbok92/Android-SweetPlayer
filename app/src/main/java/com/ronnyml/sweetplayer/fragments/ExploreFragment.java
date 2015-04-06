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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.ronnyml.sweetplayer.Constants;
import com.ronnyml.sweetplayer.Globals;
import com.ronnyml.sweetplayer.R;
import com.ronnyml.sweetplayer.adapters.CommonAdapter;
import com.ronnyml.sweetplayer.adapters.SongsAdapter;
import com.ronnyml.sweetplayer.models.Common;
import com.ronnyml.sweetplayer.models.Song;
import com.ronnyml.sweetplayer.ui.MainActivity;
import com.ronnyml.sweetplayer.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ExploreFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ObservableScrollViewCallbacks {
    private static final String TAG = ExploreFragment.class.getSimpleName();
    private Activity mActivity;
    private ArrayList<Common> mTopArtists = new ArrayList<>();
    private ArrayList<Common> mTopGenres = new ArrayList<>();
    private ArrayList<Common> mTopPlaylists = new ArrayList<>();
    private ArrayList<Song> mTopSongs = new ArrayList<>();
    private EditText mSearchEditText;
    private ImageButton mSearchImageButton;
    private LinearLayout mTopArtistsLayout;
    private LinearLayout mTopGenresLayout;
    private LinearLayout mTopPlaylistsLayout;
    private LinearLayout mTopSongsLayout;
    private ObservableListView mTopSongsListview;
    private ProgressBar mLoadingBar;
    private RecyclerView mTopArtistsRecyclerView;
    private RecyclerView mTopGenresRecyclerView;
    private RecyclerView mTopPlaylistsRecyclerView;

    public static ExploreFragment newInstance(int position, String screen) {
        ExploreFragment fragment = new ExploreFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.POSITION, position);
        args.putString(Constants.SCREEN, screen);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_explore, container, false);
        mActivity = getActivity();
        mLoadingBar = (ProgressBar) rootView.findViewById(R.id.loading_bar);
        mSearchEditText = (EditText) rootView.findViewById(R.id.search_edit_text);
        mSearchImageButton = (ImageButton) rootView.findViewById(R.id.search_imagebutton);
        mTopArtistsLayout = (LinearLayout) rootView.findViewById(R.id.top_artists_layout);
        mTopArtistsRecyclerView = (RecyclerView) rootView.findViewById(R.id.top_artists_recyclerview);
        mTopGenresLayout = (LinearLayout) rootView.findViewById(R.id.top_genres_layout);
        mTopGenresRecyclerView = (RecyclerView) rootView.findViewById(R.id.top_genres_recyclerview);
        mTopPlaylistsLayout = (LinearLayout) rootView.findViewById(R.id.top_playlists_layout);
        mTopPlaylistsRecyclerView = (RecyclerView) rootView.findViewById(R.id.top_playlists_recyclerview);
        mTopSongsLayout = (LinearLayout) rootView.findViewById(R.id.top_songs_layout);
        mTopSongsListview = (ObservableListView) rootView.findViewById(R.id.top_songs_listview);

        LinearLayoutManager mArtistsLinearLayoutManager = new LinearLayoutManager(mActivity.getApplicationContext());
        mArtistsLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mTopArtistsRecyclerView.setLayoutManager(mArtistsLinearLayoutManager);
        mTopArtistsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager mGenresLinearLayoutManager = new LinearLayoutManager(mActivity.getApplicationContext());
        mGenresLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mTopGenresRecyclerView.setLayoutManager(mGenresLinearLayoutManager);
        mTopGenresRecyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager mPlaylistsLinearLayoutManager = new LinearLayoutManager(mActivity.getApplicationContext());
        mPlaylistsLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mTopPlaylistsRecyclerView.setLayoutManager(mPlaylistsLinearLayoutManager);
        mTopPlaylistsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        setupSearchButton();

        if (Utils.isConnectedToInternet(mActivity)) {
            getRequest(Constants.PLAYLIST_URL, 0);
            getRequest(Constants.GENRES_URL, 1);
            getRequest(Constants.ARTISTS_URL, 2);
            getRequest(Constants.TOP_URL, 3);
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

            if (PlayerFragment.playerRelativeLayout.isShown()) {
                PlayerFragment.playerRelativeLayout.setVisibility(View.GONE);
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (!MainActivity.actionBar.isShowing()) {
                MainActivity.actionBar.show();
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

    private void setupSearchButton() {
        mSearchImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                String search_text = mSearchEditText.getText().toString();
                if (search_text.equals("") || search_text.length() == 0) {
                    Utils.showUserMessage(
                            mActivity.getApplicationContext(),
                            getString(R.string.search_text_empty));
                } else {
                    Intent intent = new Intent(mActivity.getApplicationContext(), MainActivity.class);
                    intent.putExtra(Constants.IS_SEARCH, true);
                    intent.putExtra(Constants.SEARCH_TEXT, search_text);
                    mActivity.startActivity(intent);
                }
            }
        });
    }

    private void getRequest(final String url, final int operation) {
        RequestQueue requestQueue = Volley.newRequestQueue(mActivity);
        JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        switch (operation) {
                            case 0:
                                getTopPlaylists(response);
                                mTopPlaylistsLayout.setVisibility(View.VISIBLE);
                                break;
                            case 1:
                                getTopGenres(response);
                                mTopGenresLayout.setVisibility(View.VISIBLE);
                                break;
                            case 2:
                                getTopArtists(response);
                                mTopArtistsLayout.setVisibility(View.VISIBLE);
                                break;
                            case 3:
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
                mTopArtists.add(artist);
            }

            CommonAdapter playlistAdapter = new CommonAdapter(mActivity, Constants.IS_ARTIST, mTopArtists, false);
            mTopArtistsRecyclerView.setAdapter(playlistAdapter);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void getTopGenres(JSONObject response) {
        try {
            JSONArray json_array = response.getJSONArray(Constants.GENRES_ITEM);
            for (int i = 0; i < json_array.length(); i++) {
                JSONObject json_object = (JSONObject) json_array.get(i);
                String id = json_object.getString(Constants.ID);
                String image = json_object.getString(Constants.IMAGE);
                String name = json_object.getString(Constants.GENRE_NAME);

                Common genre = new Common(id, image, name);
                mTopGenres.add(genre);
            }

            CommonAdapter playlistAdapter = new CommonAdapter(mActivity, Constants.IS_GENRE, mTopGenres, true);
            mTopGenresRecyclerView.setAdapter(playlistAdapter);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void getTopPlaylists(JSONObject response) {
        try {
            JSONArray json_array = response.getJSONArray(Constants.PLAYLISTS_ITEM);
            for (int i = 0; i < json_array.length(); i++) {
                JSONObject json_object = (JSONObject) json_array.get(i);
                String id = json_object.getString(Constants.ID);
                String image = json_object.getString(Constants.IMAGE);
                String name = json_object.getString(Constants.NAME);

                Common playlist = new Common(id, image, name);
                mTopPlaylists.add(playlist);
            }

            CommonAdapter playlistAdapter = new CommonAdapter(mActivity, Constants.IS_PLAYLIST, mTopPlaylists, false);
            mTopPlaylistsRecyclerView.setAdapter(playlistAdapter);
        } catch (JSONException ex) {
            ex.printStackTrace();
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
                mTopSongs.add(song);
            }
        } catch (JSONException ex) {
           ex.printStackTrace();
        }

        SongsAdapter songsAdapter = new SongsAdapter(mActivity, mTopSongs, true);
        mTopSongsListview.setAdapter(songsAdapter);
        mTopSongsListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Globals.currentPosition = position;
                Globals.currentSongs = mTopSongs;
                PlayerFragment.showPlayer(true);
            }
        });
    }
}