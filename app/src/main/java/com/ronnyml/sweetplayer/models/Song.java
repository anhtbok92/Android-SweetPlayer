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

package com.ronnyml.sweetplayer.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.ronnyml.sweetplayer.Constants;
import com.ronnyml.sweetplayer.utils.Utils;

public class Song implements Parcelable {
    private String artist_name;
    private String duration;
    private String image;
    private String mp3_url;
    private String song_name;

    public Song(Parcel source) {
        artist_name = source.readString();
        duration = source.readString();
        image = source.readString();
        mp3_url = source.readString();
        song_name = source.readString();
    }

    public Song(String _artist_name,
                String _duration,
                String _image,
                String _mp3_url,
                String _song_name) {

        this.artist_name = _artist_name;
        this.duration = _duration;
        this.image = _image;
        this.mp3_url = _mp3_url;
        this.song_name = _song_name;
    }

    public Song(String _artist_name,
                String _duration,
                String _mp3_url,
                String _song_name) {

        this.artist_name = _artist_name;
        this.duration = _duration;
        this.mp3_url = _mp3_url;
        this.song_name = _song_name;
    }

    public Song(String _artist_name,
                String _mp3_url,
                String _song_name) {

        this.artist_name = _artist_name;
        this.mp3_url = _mp3_url;
        this.song_name = _song_name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artist_name);
        dest.writeString(duration);
        dest.writeString(image);
        dest.writeString(mp3_url);
        dest.writeString(song_name);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public String getArtistName() {
        return this.artist_name;
    }

    public String getDuration() {
        return this.duration;
    }

    public String getImage() {
        return Utils.replaceImage(this.image, 2) ;
    }

    public String getLocalMP3Path() {
        return Constants.DOWNLOAD_FULL_DIRECTORY + getMP3Url();
    }

    public String getMP3Url() {
        return this.mp3_url;
    }

    public String getSongName() {
        return this.song_name;
    }

    public String getSongArtist() {
        return this.song_name + " - " + this.artist_name;
    }
}
