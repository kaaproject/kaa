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

package org.kaaproject.kaa.demo.powerplant.data;

import org.kaaproject.kaa.demo.powerplant.fragment.DashboardFragment;

public abstract class AbstractDataEndpoint implements DataEndpoint {

    private static final float CONSUME_VOLTAGE_DIV = 2000f;

    protected float getConsumption(int pannelCount) {
        return  pannelCount * DashboardFragment.NUM_ZONES * 
        		(DashboardFragment.MAX_VOLTAGE + (float)Math.random() * CONSUME_VOLTAGE_DIV) * 100;
    }
}
