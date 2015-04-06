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

import com.ronnyml.sweetplayer.utils.Utils;

public class Common implements Parcelable {
    private String id;
    private String image;
    private String name;

    public Common(Parcel source) {
        id = source.readString();
        image = source.readString();
        name = source.readString();
    }

    public Common(String _id, String _image, String _name) {
        this.id = _id;
        this.image = _image;
        this.name = _name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeString(image);
        parcel.writeString(name);
    }

    public static final Creator CREATOR = new Creator() {
        public Common createFromParcel(Parcel in) {
            return new Common(in);
        }

        public Common[] newArray(int size) {
            return new Common[size];
        }
    };

    public String getId() {
        return this.id;
    }

    public String getImage() {
        return Utils.replaceImage(this.image, 2);
    }

    public String getName() {
        return this.name;
    }
}
