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

package com.ronnyml.sweetplayer.ui;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ronnyml.sweetplayer.Constants;
import com.ronnyml.sweetplayer.R;
import com.ronnyml.sweetplayer.adapters.DrawerAdapter;
import com.ronnyml.sweetplayer.fragments.CommonFragment;
import com.ronnyml.sweetplayer.fragments.ExploreFragment;
import com.ronnyml.sweetplayer.fragments.LegalFragment;
import com.ronnyml.sweetplayer.fragments.MyMusicFragment;
import com.ronnyml.sweetplayer.fragments.PlayerFragment;
import com.ronnyml.sweetplayer.fragments.SearchFragment;
import com.ronnyml.sweetplayer.models.DrawerItem;
import com.ronnyml.sweetplayer.utils.Utils;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements PlayerFragment.OnFragmentInteractionListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static Activity mActivity;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayList<DrawerItem> mDrawerList = new ArrayList<>();
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;

    private CharSequence mCurrentTitle;
    private String[] mDrawerTitlesArray;
    private String mDetailTitle;

    public static ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;
        handleIntent(getIntent());

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerListView = (ListView) findViewById(R.id.navigation_drawer_listview);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!preferences.getBoolean(getString(R.string.is_first_time), false)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(getString(R.string.is_first_time), true);
            editor.apply();
            showLegalMessage();
        }

        setupNavigationDrawer();
        if (savedInstanceState == null) {
            int fragment = 1;
            Intent intent = getIntent();
            if (intent != null) {
                if (intent.getBooleanExtra(Constants.IS_SEARCH, false)) {
                    fragment = 0;
                } else if (intent.getBooleanExtra(Constants.IS_DOWNLOAD, false)) {
                    fragment = 2;
                } else if (
                        intent.getBooleanExtra(Constants.IS_PLAYLIST, false) ||
                        intent.getBooleanExtra(Constants.IS_ARTIST, false) ||
                        intent.getBooleanExtra(Constants.IS_GENRE, false)) {
                    fragment = 5;
                    mDetailTitle = intent.getStringExtra(Constants.NAME);
                }
            }

            selectItem(fragment);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerListView);
        menu.findItem(R.id.action_search).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    public void onFragmentInteraction(Uri uri){

    }

    public static void finishActivity() {
        mActivity.finish();
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String search_text = intent.getStringExtra(SearchManager.QUERY);
            if (search_text.equals("") || search_text.length() == 0) {
                Utils.showUserMessage(
                        mActivity.getApplicationContext(),
                        getString(R.string.search_text_empty));
            } else {
                Intent newIntent = new Intent(mActivity.getApplicationContext(), MainActivity.class);
                newIntent.putExtra(Constants.IS_SEARCH, true);
                newIntent.putExtra(Constants.SEARCH_TEXT, search_text);
                mActivity.startActivity(newIntent);
            }
            Utils.LogD("Main", search_text);
        }
    }

    private void setupNavigationDrawer() {
        mDrawerLayout.setDrawerShadow(R.mipmap.drawer_shadow, GravityCompat.START);

        mDrawerTitlesArray = getResources().getStringArray(R.array.drawer_titles_array);
        TypedArray drawerIconsArray = getResources().obtainTypedArray(R.array.drawer_icons_array);
        for (int i = 0; i < mDrawerTitlesArray.length; i++) {
            String icon = String.valueOf(drawerIconsArray.getResourceId(i, 0));
            String title = mDrawerTitlesArray[i];
            DrawerItem drawerItem = new DrawerItem(icon, title);
            mDrawerList.add(drawerItem);
        }
        drawerIconsArray.recycle();

        DrawerAdapter drawerAdapter = new DrawerAdapter(this, mDrawerList);
        mDrawerListView.setAdapter(drawerAdapter);
        mDrawerListView.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {
                    public void onDrawerClosed(View view) {
                        getSupportActionBar().setTitle(mCurrentTitle);
                        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                    }

                    public void onDrawerOpened(View drawerView) {
                        getSupportActionBar().setTitle(getString(R.string.app_name));
                        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                    }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void showLegalMessage() {
        Utils.showAlertDialog(
                mActivity,
                getString(R.string.legal_title),
                getString(R.string.legal_description));
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mCurrentTitle != null) {
                if (!mCurrentTitle.equals(getDrawerTitle(position))) {
                    selectItem(position);
                } else {
                    mDrawerLayout.closeDrawer(mDrawerListView);
                }
            }
        }
    }

    public void selectItem(int position) {
        if (position == 4) {
            Utils.exitApp(mActivity);
        } else {
            Fragment fragment;
            switch (position) {
                case 0:
                    fragment = SearchFragment.newInstance(position, getDrawerTitle(position));
                    break;
                default:
                case 1:
                    fragment = ExploreFragment.newInstance(position, getDrawerTitle(position));
                    break;
                case 2:
                    fragment = MyMusicFragment.newInstance(position, getDrawerTitle(position));
                    break;
                case 3:
                    fragment = LegalFragment.newInstance(position, getDrawerTitle(position));
                    break;
                case 5:
                    fragment = CommonFragment.newInstance(position, getDetailTitle());
                    break;
            }

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, fragment);

            Fragment playerFragment = PlayerFragment.newInstance();
            fragmentTransaction.replace(R.id.player_fragment, playerFragment);
            fragmentTransaction.commit();

            mDrawerListView.setItemChecked(position, true);
            mDrawerListView.setSelection(position);
            setTitle(getCurrentScreen(fragment));
            mDrawerLayout.closeDrawer(mDrawerListView);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mCurrentTitle = title;
        getSupportActionBar().setTitle(mCurrentTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private String getCurrentScreen(Fragment fragment) {
        return fragment.getArguments().getString(Constants.SCREEN);
    }

    private String getDrawerTitle(int position) {
        return mDrawerTitlesArray[position] != null ? mDrawerTitlesArray[position] : mActivity.getString(R.string.app_name);
    }

    private String getDetailTitle() {
        return mDetailTitle != null ? mDetailTitle : mActivity.getString(R.string.app_name);
    }
}
