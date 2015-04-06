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

import android.content.Intent;

import com.ronnyml.sweetplayer.models.Song;

import java.util.ArrayList;

public class Globals {
    public static ArrayList<Song> current_downloads = new ArrayList<>();
    public static ArrayList<Song> currentSongs = new ArrayList<>();
    public static Intent playerIntent;

    public static boolean isPlayerServiceRunning = false;
    public static int currentPosition = 0;
}
