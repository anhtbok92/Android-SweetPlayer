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

package com.ronnyml.sweetplayer.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.ronnyml.sweetplayer.Constants;
import com.ronnyml.sweetplayer.Globals;
import com.ronnyml.sweetplayer.models.Song;
import com.ronnyml.sweetplayer.utils.CustomPhoneStateListener;
import com.ronnyml.sweetplayer.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;

public class PlayerService extends Service {
    private static final String TAG = "Player Service";
    private static MediaPlayer mPlayer;
    private Handler mHandler = new Handler();
    private Intent mBroadcastIntent;
    private Intent mBroadcastIntentEnd;
    private static boolean mIsStream = true;

    private static ArrayList<Song> playerSongs = null;
    private static int mCurrentPosition = 0;
    public static boolean isPlaying = false;
    private static boolean isCompleted = false;


    public static final String BROADCAST_ACTION_UPDATE = "com.ronnyml.sweetplayer.player_update";
    public static final String BROADCAST_ACTION_END = "com.ronnyml.sweetplayer.player_end";

    @Override
    public void onCreate() {
        super.onCreate();
        Globals.isPlayerServiceRunning = true;
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mBroadcastIntent = new Intent(BROADCAST_ACTION_UPDATE);
        mBroadcastIntentEnd = new Intent(BROADCAST_ACTION_END);

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService (Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new CustomPhoneStateListener(this), PhoneStateListener.LISTEN_CALL_STATE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Utils.LogI(TAG, "Starting Playing Service");
            isCompleted = false;
            playerSongs = intent.getParcelableArrayListExtra(Constants.PLAYER_SONGS_LIST);
            mCurrentPosition = intent.getExtras().getInt(Constants.PLAYER_CURRENT_POSITION);
            mIsStream = intent.getExtras().getBoolean(Constants.PLAYER_IS_STREAM);
            mPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
            mPlayer.setOnCompletionListener(mOnCompletionListener);
            mPlayer.setOnErrorListener(mOnErrorListener);
            mPlayer.setOnPreparedListener(mOnPreparedListener);
            play();
        }
        return START_NOT_STICKY;
    }

    private static void play() {
        try {
            if (mPlayer != null && checkSongsList()) {
                mPlayer.reset();
                Song songItem = playerSongs.get(mCurrentPosition);
                String mp3 = mIsStream ? songItem.getMP3Url() : songItem.getLocalMP3Path();
                mPlayer.setDataSource(mp3);
                mPlayer.prepareAsync();
                isPlaying = true;
            }
        } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void playSong() {
        try {
            if (mPlayer != null) {
                if (isCompleted) {
                    isPlaying = true;
                    mPlayer.start();
                }
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public static void pause() {
        try {
            if (mPlayer != null) {
                if (mPlayer.isPlaying()) {
                    isPlaying = false;
                    mPlayer.pause();
                }
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private static void next() {
        if (checkSongsList()) {
            int nextIndex;
            if (mCurrentPosition >= playerSongs.size() - 1) {
                nextIndex = 0;
                mCurrentPosition = nextIndex;
            } else {
                nextIndex = mCurrentPosition + 1;
                mCurrentPosition = nextIndex;
            }
            play();
        }
    }

    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            mPlayer.start();
            setupHandler();
            isCompleted = true;
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
        }
    };

    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return false;
        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer arg0) {
            if (mPlayer != null && checkSongsList()) {
                next();
                sendBroadcast(mBroadcastIntentEnd);
                Globals.currentPosition = mCurrentPosition;
            }
        }
    };

    public static boolean checkSongsList() {
        return playerSongs != null && playerSongs .size() > 0;
    }

    private void setupHandler() {
        mHandler.removeCallbacks(sendUpdatesToUI);
        mHandler.postDelayed(sendUpdatesToUI, 1000);
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            int total_duration;

            if (mIsStream) {
                String duration = playerSongs.get(mCurrentPosition).getDuration();
                total_duration = Integer.parseInt(duration);
            } else {
                total_duration = Utils.toSeconds(mPlayer.getDuration());
            }

            int current_position = mPlayer.getCurrentPosition();
            mBroadcastIntent.putExtra(Constants.PLAYER_CURRENT_DURATION, current_position);
            mBroadcastIntent.putExtra(Constants.PLAYER_TOTAL_DURATION, total_duration);
            sendBroadcast(mBroadcastIntent);
            mHandler.postDelayed(this, 1000);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Globals.isPlayerServiceRunning = false;
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            mPlayer.release();
            mHandler.removeCallbacks(sendUpdatesToUI);
        }
        super.onDestroy();
    }
}
