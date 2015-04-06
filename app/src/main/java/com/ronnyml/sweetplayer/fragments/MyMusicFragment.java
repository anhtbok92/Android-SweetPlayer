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
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.ronnyml.sweetplayer.Constants;
import com.ronnyml.sweetplayer.Globals;
import com.ronnyml.sweetplayer.R;
import com.ronnyml.sweetplayer.adapters.MyMusicAdapter;
import com.ronnyml.sweetplayer.models.Song;
import com.ronnyml.sweetplayer.ui.MainActivity;
import com.ronnyml.sweetplayer.utils.Utils;
import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class MyMusicFragment extends Fragment implements ObservableScrollViewCallbacks  {
    private static final String TAG = MyMusicFragment.class.getSimpleName();
    private static Activity mActivity;
    private static ArrayList<Song> mSongs = new ArrayList<>();
    private static ObservableListView mMyMusicListview;

    public static MyMusicFragment newInstance(int position, String screen) {
        MyMusicFragment fragment = new MyMusicFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.POSITION, position);
        args.putString(Constants.SCREEN, screen);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_music, container, false);
        mActivity = this.getActivity();
        mMyMusicListview = (ObservableListView) rootView.findViewById(R.id.my_music_listview);
        mMyMusicListview.setScrollViewCallbacks(this);

        TextView myMusicPathTextView = (TextView) rootView.findViewById(R.id.music_path_textview);
        myMusicPathTextView.setText(Constants.DOWNLOAD_FULL_DIRECTORY);

        clearData();
        getDowloads();

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

    private void clearData() {
        if (!mSongs.isEmpty()) {
            mSongs.clear();
        }
    }

    private void getDowloads() {
        File sdCard = Environment.getExternalStorageDirectory();
        File downloadDir = new File(sdCard, Constants.DOWNLOAD_DIRECTORY);

        FilenameFilter filenameFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lowercaseName = name.toLowerCase();
                return lowercaseName.endsWith(Constants.MP3_EXTENSION);
            }
        };

        if (downloadDir.exists()) {
            if (downloadDir.list(filenameFilter).length > 0) {
                for (String mp3_filename : downloadDir.list()) {
                    if (!mp3_filename.isEmpty()) {
                        String temp = mp3_filename.replace(Constants.MP3_EXTENSION, "");
                        String song_data[] = temp.split(" - ");
                        String song_name = song_data[0];
                        String artist_name = song_data[1];
                        Song song = new Song(artist_name, mp3_filename, song_name);
                        mSongs.add(song);
                    }
                }
            } else {
                Utils.showUserMessage(mActivity, mActivity.getString(R.string.download_no_downloads));
            }
        }

        setAdapter();
    }

    public static void deleteSong(int position) {
        mSongs.remove(position);
        setAdapter();
    }

    private static void setAdapter() {
        if (!mSongs.isEmpty()) Utils.sortArrayList(mSongs);
        MyMusicAdapter myMusicAdapter = new MyMusicAdapter(mActivity, mSongs);
        mMyMusicListview.setAdapter(myMusicAdapter);
        mMyMusicListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Globals.currentPosition = position;
                Globals.currentSongs = mSongs;
                PlayerFragment.showPlayer(false);
            }
        });
    }
}