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
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.ronnyml.sweetplayer.utils.Utils;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class SongsAdapter extends BaseAdapter {
    private Activity mActivity;
    private ArrayList<Song> mSongs;
    private ImageLoader mImageLoader;
    private LayoutInflater mLayoutInflater;
    private boolean mHasImage = true;

    public SongsAdapter(Activity activity, ArrayList<Song> songs, boolean hasImage) {
        mActivity = activity;
        mLayoutInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mHasImage = hasImage;
        mImageLoader = MyImageLoader.getInstance(mActivity).getImageLoader();
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

    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class ViewHolder {
        public ImageButton downloadButton;
        public LinearLayout progress_bar_layout;
        public NetworkImageView artistImageView;
        public RelativeLayout item_song_layout;
        public SeekBar progressBar;
        public TextView artistNameTextView;
        public TextView songNameTextView;
        public TextView downloadProgressTextView;
        public String song;
        public String mp3_url;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_song, null);
            viewHolder = new ViewHolder();
            viewHolder.artistImageView = (NetworkImageView) convertView.findViewById(R.id.artist_imageview);
            viewHolder.artistNameTextView = (TextView) convertView.findViewById(R.id.artist_name);
            viewHolder.downloadButton = (ImageButton) convertView.findViewById(R.id.download_image_button);
            viewHolder.downloadProgressTextView = (TextView) convertView.findViewById(R.id.download_progress_textview);
            viewHolder.item_song_layout = (RelativeLayout) convertView.findViewById(R.id.item_song_layout);
            viewHolder.progressBar = (SeekBar) convertView.findViewById(R.id.progress_bar);
            viewHolder.progress_bar_layout = (LinearLayout) convertView.findViewById(R.id.progress_bar_layout);
            viewHolder.songNameTextView = (TextView) convertView.findViewById(R.id.song_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Song song = mSongs.get(position);
        final String artist_name = song.getArtistName();
        final String song_name = song.getSongName();

        viewHolder.artistNameTextView.setText(artist_name);
        viewHolder.songNameTextView.setText(song_name);
        viewHolder.song = song.getSongArtist();
        viewHolder.mp3_url = song.getMP3Url();

        String image = song.getImage();
        if (mHasImage) {
            viewHolder.artistImageView.setImageUrl(image, mImageLoader);
        } else {
            viewHolder.artistImageView.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewHolder.progress_bar_layout.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, R.id.artist_name);
            viewHolder.progress_bar_layout.setLayoutParams(params);
        }

        viewHolder.downloadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                viewHolder.item_song_layout.setBackgroundColor(mActivity.getResources().getColor(R.color.download_item_downloading_color));
                FileDownloadTask fileDownloadTask = new FileDownloadTask();
                fileDownloadTask.execute(viewHolder);

                v.setVisibility(View.GONE);
                viewHolder.artistNameTextView.setTextColor(mActivity.getResources().getColor(R.color.download_progress_color));
                viewHolder.songNameTextView.setTextColor(mActivity.getResources().getColor(R.color.download_progress_color));
                viewHolder.downloadProgressTextView.setVisibility(View.VISIBLE);
                viewHolder.downloadProgressTextView.setText("0 %");
                viewHolder.progressBar.setVisibility(View.VISIBLE);

                Utils.showUserMessage(
                        mActivity,
                        mActivity.getString(R.string.download_downloading) + " " + song.getSongArtist());

                Globals.current_downloads.add(song);
            }
        });

        viewHolder.progressBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        return convertView;
    }

    public class FileDownloadTask extends AsyncTask<ViewHolder, Integer, ViewHolder> {
        private final String  TAG = FileDownloadTask.class.getSimpleName();
        private boolean isDownloadComplete = false;
        private ViewHolder viewHolder;

        @Override
        protected void onPreExecute() {
            Utils.LogI(TAG, "Start Downloading");
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            viewHolder.downloadProgressTextView.setText(progress[0] + " %");
            viewHolder.progressBar.setProgress(progress[0]);
        }

        @Override
        protected ViewHolder doInBackground(ViewHolder...params) {
            viewHolder = params[0];
            String song_artist = viewHolder.song;
            String mp3_url = viewHolder.mp3_url;

            OkHttpClient httpClient = new OkHttpClient();
            Request request = new Request.Builder().url(mp3_url).get().build();
            Call call = httpClient.newCall(request);

            try {
                String mp3_file = (
                        Constants.DOWNLOAD_FULL_DIRECTORY + song_artist + Constants.MP3_EXTENSION);
                Utils.LogD(TAG, "Downloading: " + mp3_file);
                Response response = call.execute();

                if (response.code() == 200) {
                    InputStream inputStream;

                    try {
                        inputStream = response.body().byteStream();
                        OutputStream output = new FileOutputStream(mp3_file);

                        byte[] buffer = new byte[1024 * 4];
                        long downloaded = 0;
                        long file_length = response.body().contentLength();
                        int count;

                        while ((count = inputStream.read(buffer)) != -1) {
                            downloaded += count;
                            int progress = (int) (downloaded * 100 / file_length);
                            publishProgress(progress);
                            output.write(buffer, 0, count);
                        }

                        output.flush();
                        output.close();
                        inputStream.close();
                        if (downloaded == file_length) isDownloadComplete = true;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            return viewHolder;
        }

        @Override
        protected void onPostExecute(ViewHolder result) {
            Utils.LogI(TAG, "Download finished");
            String downloadMessage =
                    isDownloadComplete ? mActivity.getString(R.string.download_complete) : mActivity.getString(R.string.download_failed);
            Utils.showUserMessage(mActivity, downloadMessage + " " + viewHolder.song);
        }
    }
}
