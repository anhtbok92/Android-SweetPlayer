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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ronnyml.sweetplayer.Constants;
import com.ronnyml.sweetplayer.Globals;
import com.ronnyml.sweetplayer.R;
import com.ronnyml.sweetplayer.utils.MyImageLoader;
import com.ronnyml.sweetplayer.models.Song;
import com.ronnyml.sweetplayer.services.PlayerService;
import com.ronnyml.sweetplayer.utils.Utils;

public class PlayerFragment extends Fragment {
    private static final String TAG = PlayerFragment.class.getSimpleName();
    private OnFragmentInteractionListener mListener;
    private SeekBar mPlayerSeekBar;
    private TextView mPlayerCurrentDurationTextView;
    private TextView mPlayerTotalDurationTextView;

    private static Activity mActivity;
    private static ImageButton mPlayPauseImageButton;
    private static ImageLoader mImageLoader;
    private static NetworkImageView mPlayerArtistImageView;
    private static TextView mPlayerArtistTextView;
    private static TextView mPlayerSongTextView;
    private boolean mBroadcastIsRegistered;

    public static RelativeLayout playerRelativeLayout;

    public static PlayerFragment newInstance() {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putString(Constants.SCREEN, Constants.PLAYER);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        mActivity = this.getActivity();
        mImageLoader = MyImageLoader.getInstance(mActivity).getImageLoader();
        mPlayerArtistImageView = (NetworkImageView) rootView.findViewById(R.id.player_artist_image_view);
        mPlayerArtistTextView = (TextView) rootView.findViewById(R.id.player_artist_textview);
        mPlayerCurrentDurationTextView = (TextView) rootView.findViewById(R.id.player_current_duration_textview);
        mPlayerSeekBar = (SeekBar) rootView.findViewById(R.id.player_seek_bar);
        mPlayerSongTextView = (TextView) rootView.findViewById(R.id.player_song_textview);
        mPlayerTotalDurationTextView = (TextView) rootView.findViewById(R.id.player_total_duration_textview);
        mPlayPauseImageButton = (ImageButton) rootView.findViewById(R.id.player_play_pause_imagebutton);
        playerRelativeLayout = (RelativeLayout) rootView.findViewById(R.id.player_layout);

        setupPlayerPause();
        if (Globals.isPlayerServiceRunning) {
            setPlayerData();
        } else {
            playerRelativeLayout.setVisibility(View.GONE);
            mPlayPauseImageButton.setImageResource(R.drawable.play);
        }

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    private void setupPlayerPause() {
        mPlayPauseImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (PlayerService.checkSongsList()) {
                    if (PlayerService.isPlaying) {
                        mPlayPauseImageButton.setImageResource(R.drawable.play);
                        PlayerService.pause();
                    } else {
                        mPlayPauseImageButton.setImageResource(R.drawable.pause);
                        PlayerService.playSong();
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity.registerReceiver(
                playerBroadcastReceiver,
                new IntentFilter(PlayerService.BROADCAST_ACTION_UPDATE));
        mActivity.registerReceiver(
                playerBroadcastReceiverEnd,
                new IntentFilter(PlayerService.BROADCAST_ACTION_END));
        mBroadcastIsRegistered = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBroadcastIsRegistered) {
            if (playerBroadcastReceiver != null) {
                mActivity.unregisterReceiver(playerBroadcastReceiver);
                playerBroadcastReceiver = null;
            }
            if (playerBroadcastReceiverEnd != null) {
                mActivity.unregisterReceiver(playerBroadcastReceiverEnd);
                playerBroadcastReceiverEnd = null;
            }
            mBroadcastIsRegistered = false;
        }
    }

    private BroadcastReceiver playerBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int current_duration = intent.getIntExtra(Constants.PLAYER_CURRENT_DURATION, 0);
                int total_duration = intent.getIntExtra(Constants.PLAYER_TOTAL_DURATION, 0);
                mPlayerCurrentDurationTextView.setText(Utils.getTotalDuration(Utils.toSeconds(current_duration)));
                mPlayerTotalDurationTextView.setText(Utils.getTotalDuration(total_duration));
                mPlayerSeekBar.setMax(total_duration);
                mPlayerSeekBar.setProgress(Utils.toSeconds(current_duration));
            }
        }
    };

    private BroadcastReceiver playerBroadcastReceiverEnd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                setPlayerData();
            }
        }
    };

    public static void showPlayer(boolean isStream) {
        setPlayerData();
        startPlayerService(isStream);
    }

    private static void setPlayerData() {
        if (!Globals.currentSongs.isEmpty()) {
            Song songItem = Globals.currentSongs.get(Globals.currentPosition);
            if (songItem != null) {
                playerRelativeLayout.setVisibility(View.VISIBLE);
                mPlayerArtistImageView.setDefaultImageResId(R.mipmap.default_img);
                mPlayerArtistImageView.setImageUrl(
                        songItem.getImage(),
                        mImageLoader);
                mPlayerArtistTextView.setText(songItem.getArtistName());
                mPlayerSongTextView.setText(songItem.getSongName());
                mPlayPauseImageButton.setImageResource(R.drawable.pause);
            }
        }
    }

    private static void startPlayerService(boolean isStream) {
        Globals.playerIntent = new Intent(mActivity, PlayerService.class);
        Globals.playerIntent.putParcelableArrayListExtra(Constants.PLAYER_SONGS_LIST, Globals.currentSongs);
        Globals.playerIntent.putExtra(Constants.PLAYER_CURRENT_POSITION, Globals.currentPosition);
        Globals.playerIntent.putExtra(Constants.PLAYER_IS_STREAM, isStream);
        mActivity.startService(Globals.playerIntent);
    }
}