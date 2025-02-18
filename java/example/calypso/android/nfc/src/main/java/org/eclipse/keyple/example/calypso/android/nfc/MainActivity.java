/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.example.calypso.android.nfc;

import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;


/**
 * Example of @{@link SeProxyService} implementation based on the @{@link AndroidNfcPlugin}
 *
 */
public class MainActivity extends AppCompatActivity {

    private static final Logger LOG = LoggerFactory.getLogger(MainActivity.class);

    private static final String TAG_NFC_TEST_VIEW = "tagnfctestfragment";


    private DrawerLayout mDrawerLayout;


    /**
     * Setup navigation drawer
     * 
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /*
         * Define UI elements
         */
        LOG.debug("onCreate");
        setContentView(org.eclipse.keyple.example.calypso.android.nfc.R.layout.activity_main);

        setupNavDrawer();
        /*
         * end of Define UI elements
         */

        activateNFCTestView();


    }

    /**
     * Inject NFC Test View fragment into activity
     */
    private void activateNFCTestView() {
        // init NFC Test Fragment
        LOG.debug("Insert NFC Test View Fragment");

        FragmentManager fm = this.getSupportFragmentManager();
        Fragment nfcTestFragment = fm.findFragmentByTag(TAG_NFC_TEST_VIEW);

        if (nfcTestFragment == null) {
            LOG.debug("Create a new NFCTestFragment");
            fm.beginTransaction()
                    .replace(org.eclipse.keyple.example.calypso.android.nfc.R.id.fragment_container,
                            NFCTestFragment.newInstance(), TAG_NFC_TEST_VIEW)
                    .addToBackStack(null).commit();
        } else {
            LOG.debug("NFCTestFragment is already created");
        }
    }


    /**
     * Configure Navigation Drawer
     */
    private void setupNavDrawer() {
        mDrawerLayout = findViewById(org.eclipse.keyple.example.calypso.android.nfc.R.id.drawer_layout);

        // Drawer configuration
        NavigationView navigationView =
                findViewById(org.eclipse.keyple.example.calypso.android.nfc.R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        LOG.debug("Item selected from drawer: " + menuItem.getTitle());

                        switch (menuItem.getItemId()) {
                            case org.eclipse.keyple.example.calypso.android.nfc.R.id.nav_nfc:
                                activateNFCTestView();
                                break;

                            default:
                                throw new IllegalArgumentException("Invalid menuItem");

                        }
                        return true;
                    }
                });

        // Toolbar
        Toolbar toolbar = findViewById(org.eclipse.keyple.example.calypso.android.nfc.R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hamburger icon
        ActionBar actionbar = getSupportActionBar();
        assert actionbar != null;
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(
                org.eclipse.keyple.example.calypso.android.nfc.R.drawable.ic_menu_black_24dp);

    }

    // 'Open' event for navigation drawer
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
