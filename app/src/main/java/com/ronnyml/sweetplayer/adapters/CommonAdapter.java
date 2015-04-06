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
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ronnyml.sweetplayer.Constants;
import com.ronnyml.sweetplayer.R;
import com.ronnyml.sweetplayer.utils.MyImageLoader;
import com.ronnyml.sweetplayer.models.Common;
import com.ronnyml.sweetplayer.ui.MainActivity;

import java.util.ArrayList;

public class CommonAdapter extends RecyclerView.Adapter<CommonAdapter.ViewHolder> {
    private static final String TAG = CommonAdapter.class.getSimpleName();
    private Activity mActivity;
    private ArrayList<Common> mData;
    private ImageLoader mImageLoader;

    private boolean mIsGenre = false;
    private String mCurrentScreen = "";

    public CommonAdapter(Activity activity, String currentScreen,
                         ArrayList<Common> data, boolean isGenre) {
        mActivity = activity;
        mCurrentScreen = currentScreen;
        mData = data;
        mImageLoader = MyImageLoader.getInstance(mActivity).getImageLoader();
        mIsGenre = isGenre;
    }

    @Override
    public CommonAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_common, null);

        return new CommonAdapter.ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(CommonAdapter.ViewHolder viewHolder, int position) {
        Common data = mData.get(position);
        final String id = data.getId();
        final String image = data.getImage();
        final String name = data.getName();

        if (mIsGenre) {
            viewHolder.generalNameTextView.setVisibility(View.GONE);
        } else {
            viewHolder.generalNameTextView.setText(name);
        }

        viewHolder.generalImageView.setImageUrl(image, mImageLoader);
        viewHolder.generalImageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(mActivity.getApplicationContext(), MainActivity.class);
                intent.putExtra(mCurrentScreen, true);
                intent.putExtra(Constants.ID, id);
                intent.putExtra(Constants.IMAGE, image);
                intent.putExtra(Constants.NAME, name);
                mActivity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        NetworkImageView generalImageView;
        TextView generalNameTextView;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            generalImageView = (NetworkImageView) itemLayoutView.findViewById(R.id.general_image_view);
            generalNameTextView = (TextView) itemLayoutView.findViewById(R.id.general_name_textview);
        }
    }
}