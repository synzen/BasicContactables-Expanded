/*
 * Copyright (C) 2012 The Android Open Source Project
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
package com.example.android.basiccontactables;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.util.Log;
import android.widget.TextView;

import java.util.Map;

/**
    Michael Tan
    ID: 23399558
 */
public class MainActivity extends Activity {
    public static final int CONTACT_QUERY_LOADER = 0;
    public static final String QUERY_KEY = "query";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_main);
        // Request permission to read contacts if it doesn't have
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 1);

    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; ++i) {
            String perm = permissions[i];
            int res = grantResults[i];

            if (perm.equals(Manifest.permission.READ_CONTACTS)) {
                if (res == PackageManager.PERMISSION_GRANTED) {
                    TextView loading = findViewById(R.id.loadingText);
                    if (res == PackageManager.PERMISSION_GRANTED) loading.setText("Permission Granted! You may now enter queries at the top right.");
                    else {
                        loading.setText("This app is unable to function without permission to read contacts. Click me to try again.");
                        loading.setTextColor(this.getColor(R.color.red));
                        loading.setOnClickListener(v -> {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
                            loading.setOnClickListener(null);
                            loading.setTextColor(loading.getTextColors().getDefaultColor());
                        });
                    }
                    ProgressBar loadingIcon = findViewById(R.id.loadingIcon);
                    loadingIcon.setVisibility(View.GONE);
                    return;
                } else {

                }
            }
        }
//        if (requestCode == 1 && grantResults)
    }
    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    /**
     * Assuming this activity was started with a new intent, process the incoming information and
     * react accordingly.
     * @param intent
     */
    private void handleIntent(Intent intent) {
        // Special processing of the incoming intent only occurs if the if the action specified
        // by the intent is ACTION_SEARCH.
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // SearchManager.QUERY is the key that a SearchManager will use to send a query string
            // to an Activity.
            String query = intent.getStringExtra(SearchManager.QUERY);

            // We need to create a bundle containing the query string to send along to the
            // LoaderManager, which will be handling querying the database and returning results.
            Bundle bundle = new Bundle();
            bundle.putString(QUERY_KEY, query);

            ContactablesLoaderCallbacks loaderCallbacks = new ContactablesLoaderCallbacks(this);

            // Start the loader with the new query, and an object that will handle all callbacks.
            getLoaderManager().restartLoader(CONTACT_QUERY_LOADER, bundle, loaderCallbacks);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        Log.d("DEBUG ME", "SEARCHED! 3");

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setLayoutParams(new ActionBar.LayoutParams(Gravity.CENTER));

        // Hide the keyboard after clicking search
        searchView.setOnSearchClickListener(v -> {
            View view = MainActivity.this.getCurrentFocus();
            if (view == null) return;
            InputMethodManager man = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            man.hideSoftInputFromWindow(view.getWindowToken(), 0);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Bundle bundle = new Bundle();
                bundle.putString(QUERY_KEY, newText);
                ContactablesLoaderCallbacks loaderCallbacks = new ContactablesLoaderCallbacks(MainActivity.this);

                // Start the loader with the new query, and an object that will handle all callbacks.
                getLoaderManager().restartLoader(CONTACT_QUERY_LOADER, bundle, loaderCallbacks);
                return true;
            }
        });
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // Automatically start search for all if permission is granted, otherwise ask for permission to read contacts
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
        else searchView.setQuery("", true);

        return true;
    }



}
