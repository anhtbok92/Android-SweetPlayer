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

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.ronnyml.sweetplayer.services.PlayerService;

public class CustomPhoneStateListener extends PhoneStateListener {
    private boolean isPause = false;
    private Context context;

    public CustomPhoneStateListener(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);

        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                if (isPause) {
                    isPause = false;
                    PlayerService.playSong();
                }
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //PlayerService.playSong();
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                if (PlayerService.isPlaying) {
                    isPause = true;
                    PlayerService.pause();
                }
                break;
            default:
                break;
        }
    }
}
