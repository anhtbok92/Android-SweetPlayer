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

package com.ronnyml.sweetplayer.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.ronnyml.sweetplayer.BuildConfig;
import com.ronnyml.sweetplayer.Constants;
import com.ronnyml.sweetplayer.Globals;
import com.ronnyml.sweetplayer.models.Song;
import com.ronnyml.sweetplayer.ui.MainActivity;
import com.ronnyml.sweetplayer.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Utils {
    private static final boolean DEBUG = BuildConfig.DEBUG;

    // Logs
    public static void LogD(String tag, String message) {
        if (DEBUG) {
            Log.d(tag, message);
        }
    }

    public static void LogE(String tag, String message) {
        if (DEBUG) {
            Log.e(tag, message);
        }
    }

    public static void LogI(String tag, String message) {
        if (DEBUG) {
            Log.i(tag, message);
        }
    }

    public static void createDownloadDirectory() {
        File sdCard = Environment.getExternalStorageDirectory();
        File downloadMP3Dir = new File(sdCard, Constants.DOWNLOAD_DIRECTORY);
        if (!downloadMP3Dir.exists()) {
            downloadMP3Dir.mkdirs();
        }
    }

    public static void exitApp(final Context context){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(context.getResources().getString(R.string.app_name));
        alert.setMessage(context.getResources().getString(R.string.dialog_exit));
        alert.setPositiveButton(
                context.getResources().getString(R.string.dialog_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (Globals.playerIntent != null && Globals.isPlayerServiceRunning)
                            context.stopService(Globals.playerIntent);

                        Globals.isPlayerServiceRunning = false;
                        MainActivity.finishActivity();
                    }
                });
        alert.setNegativeButton(context.getResources().getString(R.string.dialog_cancel),
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        alert.show();
    }

    public static String getTotalDuration(int duration) {
        String secondsPref = "";
        String minutesPref = "";
        int minutes;
        int seconds;

        if (duration < 60) {
            minutes = 0;
            seconds = duration;
        } else {
            minutes = duration / 60;
            seconds = duration % 60;
        }

        if (seconds < 10)
            secondsPref = "0";

        if (minutes < 10)
            minutesPref = "0";

        return minutesPref + minutes + ":" + secondsPref + seconds;
    }

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean isConnectedToInternet(Context context) {
        CheckInternetConnection internetConnection = new CheckInternetConnection(context);
        return internetConnection.isConnectingToInternet();
    }

    public static boolean isExternalStoragePresent() {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        switch (state) {
            case Environment.MEDIA_MOUNTED:
                mExternalStorageAvailable = mExternalStorageWriteable = true;
                break;
            case Environment.MEDIA_MOUNTED_READ_ONLY:
                mExternalStorageAvailable = true;
                mExternalStorageWriteable = false;
                break;
            default:
                mExternalStorageAvailable = mExternalStorageWriteable = false;
                break;
        }
        return (mExternalStorageAvailable) && (mExternalStorageWriteable);
    }

    public static String replaceImage(String artistImage, int value) {
        String finalImage = "";
        String image_1 = "-1.jpg";
        String image_2 = "-2.jpg";
        String image_3 = "-3.jpg";
        String image_5 = "-5.jpg";

        if (validateString(artistImage)) {
            switch (value) {
                case 1:
                    finalImage = artistImage.replace(image_3, image_1);
                    return finalImage;
                case 2:
                    String image = artistImage.replace(image_3, image_2);
                    finalImage = image.replace(image_1, image_2);
                    return finalImage;
                case 3:
                    finalImage = artistImage.replace(image_1, image_3);
                    return finalImage;
                case 5:
                    finalImage = artistImage.replace(image_1, image_5);
                    return finalImage;
            }
        }
        return finalImage;
    }

    public static void shareUrl(Context context, String song) {
        String url_to_share =
                context.getString(R.string.share_text) +
                        " " + song + " " +
                        context.getString(R.string.share_android_app) +
                        " " + Constants.GOOGLE_PLAY_URL;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, url_to_share);
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, context.getString(R.string.share_title));
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
    }

    public static void showAlertDialog(Context context, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                context.getString(R.string.dialog_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                }
        });
        alertDialog.show();
    }

    public static void showUserMessage(Context context, CharSequence message) {
        Toast playingMessage = Toast.makeText(context, message, Toast.LENGTH_LONG);
        playingMessage.setGravity(Gravity.CENTER, 0, 0);
        playingMessage.show();
    }

    public static void sortArrayList(ArrayList<Song> songs) {
        Collections.sort(songs, new Comparator<Song>() {
            @Override
            public int compare(Song first, Song second) {
                return first.getSongArtist().compareTo(second.getSongArtist());
            }
        });
    }

    public static boolean validateString(String myString) {
        return myString != null && myString.length() > 0;
    }

    public static int toSeconds(int miliseconds) {
        return miliseconds / 1000;
    }

}