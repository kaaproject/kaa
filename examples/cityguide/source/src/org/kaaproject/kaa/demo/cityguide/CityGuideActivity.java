/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.demo.cityguide;

import org.kaaproject.kaa.demo.cityguide.dialog.SetLocationDialog;
import org.kaaproject.kaa.demo.cityguide.dialog.SetLocationDialog.SetLocationCallback;
import org.kaaproject.kaa.demo.cityguide.fragment.AreasFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * The implementation of the {@link ActionBarActivity} class. 
 * Notifies the application of the activity lifecycle changes.
 * Implements the 'Set location' menu command to display {@link SetLocationDialog} and notify the application 
 * of the new location.    
 */
public class CityGuideActivity extends ActionBarActivity implements
        SetLocationCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_guide);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new AreasFragment()).commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        /*
         * Notify the application of the background state.
         */

        getCityGuideApplication().pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*
         * Notify the application of the foreground state.
         */

        getCityGuideApplication().resume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.city_guide, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            popBackStack();
            return true;
        } else if (id == R.id.action_set_location) {
            setLocation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void openFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void popBackStack() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
    }

    public CityGuideApplication getCityGuideApplication() {
        return (CityGuideApplication) getApplication();
    }

    private void setLocation() {
        SetLocationDialog dialog = new SetLocationDialog(this,
                getCityGuideApplication(), this);
        dialog.show();
    }

    @Override
    public void onLocationSelected(String area, String city) {
        getCityGuideApplication().updateLocation(area, city);
    }

}
