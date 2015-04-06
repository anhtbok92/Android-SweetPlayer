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

package com.ronnyml.sweetplayer;

import android.os.Environment;

public class Constants {
    // URLs
    private static final String SERVER = "yourserver.com";
    private static final String BASE_URL = "http://api." + SERVER + "/";
    public static final String DOMAIN = "http://www." + SERVER;
    public static final String ARTISTS_URL = BASE_URL + "artists.json";
    public static final String ARTISTS_DETAIL_URL = BASE_URL + "artist-songs.json?artistid=";
    public static final String GENRES_URL = BASE_URL + "genres.json";
    public static final String GENRE_DETAIL_URL = BASE_URL + "genre-songs.json?genreid=";
    public static final String TOP_URL = BASE_URL + "top.json";
    public static final String PLAYLIST_URL = BASE_URL + "playlist.json";
    public static final String PLAYLIST_DETAIL_URL = BASE_URL + "playlist-songs.json?playlistid=";
    public static final String SEARCH_MUSIC_URL = BASE_URL + "music-search.json?q=";

    // Downloads
    public static final String MP3_EXTENSION = ".mp3";
    public static final String DOWNLOAD_DIRECTORY = Environment.DIRECTORY_DOWNLOADS + "/SweetPlayer/";
    public static final String DOWNLOAD_FULL_DIRECTORY = Environment.getExternalStorageDirectory().toString() + "/" + DOWNLOAD_DIRECTORY;

    // JSON Strings
    public static final String ARTISTS_ITEM = "artists";
    public static final String ARTIST_NAME = "artist";
    public static final String DURATION = "duration";
    public static final String GENRES_ITEM = "genres";
    public static final String GENRE_NAME = "genre";
    public static final String ID = "id";
    public static final String IMAGE = "image1";
    public static final String MP3 = "mp3";
    public static final String NAME = "name";
    public static final String PLAYLISTS_ITEM = "playlists";
    public static final String SEARCH_TEXT = "search_text";
    public static final String SONGS_ITEM = "songs";
    public static final String SONG_NAME = "song";

    // Google Play
    public static final String GOOGLE_PLAY_URL = "";

    // Player
    public static final String PLAYER = "Player";
    public static final String PLAYER_CURRENT_DURATION = "current_duration";
    public static final String PLAYER_CURRENT_POSITION = "player_current_position";
    public static final String PLAYER_IS_STREAM = "player_is_stream";
    public static final String PLAYER_SONGS_LIST = "player_list";
    public static final String PLAYER_TOTAL_DURATION = "total_duration";
    public static final String POSITION = "position";

    // Screens
    public static final String IS_ARTIST = "is_artist";
    public static final String IS_DOWNLOAD = "is_download";
    public static final String IS_GENRE = "is_genre";
    public static final String IS_PLAYLIST = "is_playlist";
    public static final String IS_SEARCH = "is_search";
    public static final String SCREEN = "screen";

}
