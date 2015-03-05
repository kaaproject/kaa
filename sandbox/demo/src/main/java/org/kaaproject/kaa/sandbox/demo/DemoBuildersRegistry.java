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

package org.kaaproject.kaa.sandbox.demo;

import java.util.ArrayList;
import java.util.List;

public class DemoBuildersRegistry {

    private static final List<DemoBuilder> demoBuilders = new ArrayList<>();
    
    static {
        demoBuilders.add(new CellMonitorDemoBuilder());
        demoBuilders.add(new CityGuideDemoBuilder());
        demoBuilders.add(new PhotoFrameDemoBuilder());
        demoBuilders.add(new SmartHouseDemoBuilder());
//      demoBuilders.add(new RobotRunDemoBuilder());
        demoBuilders.add(new JConfigurationDemoBuilder());
        demoBuilders.add(new JNotificationDemoBuilder());
    }
    
    public static List<DemoBuilder> getRegisteredDemoBuilders() {
        return demoBuilders;
    }
    
}
