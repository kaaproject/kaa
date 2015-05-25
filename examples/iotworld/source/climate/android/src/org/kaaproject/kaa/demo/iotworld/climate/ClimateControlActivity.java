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
package org.kaaproject.kaa.demo.iotworld.climate;

import org.kaaproject.kaa.demo.iotworld.climate.fragment.ThermostatFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;

public class ClimateControlActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_climate);
        
        if (savedInstanceState == null) {
            showThermostat();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();

        /*
         * Notify the application about the background state.
         */

        getClimateControlApplication().pause();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        /*
         * Notify the application about the foreground state.
         */

        getClimateControlApplication().resume();
    }
    
    private void showThermostat() {
        ThermostatFragment thermostatFragment = new ThermostatFragment();
        replaceFragment(thermostatFragment, thermostatFragment.getFragmentTag());
    }
    
    public void replaceFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction().
            replace(R.id.container, fragment, tag).commit();
    }

    public ClimateControlApplication getClimateControlApplication() {
        return (ClimateControlApplication) getApplication();
    }

    public ClimateController getController() {
        return getClimateControlApplication().getController();
    }

}
