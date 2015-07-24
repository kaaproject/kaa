/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.sandbox.web.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;

public interface SandboxResources extends ClientBundle {

    public interface KaaTheme extends CssResource {
        
        String DEFAULT_CSS = "KaaTheme.css";

    }

    public interface SandboxStyle extends SandboxTheme {
        
        String DEFAULT_CSS = "SandboxTheme.css";

    }
    
    @NotStrict
    @Source(KaaTheme.DEFAULT_CSS)
    KaaTheme kaaTheme();
    
    @NotStrict
    @Source(SandboxStyle.DEFAULT_CSS)
    SandboxStyle sandboxStyle();
    
    @ImageOptions(width = 77, height = 50)
    @Source("images/kaa_logo.png")
    ImageResource kaaLogo();

    @ImageOptions(width = 128, height = 128)
    @Source("images/android.png")
    ImageResource android();
    
    @ImageOptions(width = 128, height = 128)
    @Source("images/java.png")
    ImageResource java();

    @ImageOptions(width = 128, height = 128)
    @Source("images/c.png")
    ImageResource c();
    
    @ImageOptions(width = 128, height = 128)
    @Source("images/c.png")
    ImageResource esp8266();

    @ImageOptions(width = 114, height = 128)
    @Source("images/cpp.png")
    ImageResource cpp();
    
    @ImageOptions(width = 32, height = 28)
    @Source("images/config_feature.png")
    ImageResource configFeature();
    
    @ImageOptions(width = 32, height = 28)
    @Source("images/profiling_feature.png")
    ImageResource profilingFeature();

    @ImageOptions(width = 32, height = 28)
    @Source("images/notification_feature.png")
    ImageResource notificationFeature();
    
    @ImageOptions(width = 32, height = 28)
    @Source("images/event_feature.png")
    ImageResource eventFeature();

    @ImageOptions(width = 32, height = 28)
    @Source("images/user_verifier_feature.png")
    ImageResource userVerifierFeature();

    @ImageOptions(width = 32, height = 28)
    @Source("images/data_collection_feature.png")
    ImageResource dataCollectionFeature();

    @ImageOptions(width = 28, height = 28)
    @Source("images/android_platform.png")
    ImageResource androidPlatform();

    @ImageOptions(width = 24, height = 24)
    @Source("images/android_platform.png")
    ImageResource androidPlatformFilter();

    @ImageOptions(width = 28, height = 28)
    @Source("images/java_platform.png")
    ImageResource javaPlatform();

    @ImageOptions(width = 24, height = 24)
    @Source("images/c_platform.png")
    ImageResource cPlatform();
    
    @ImageOptions(width = 24, height = 24)
    @Source("images/c_platform.png")
    ImageResource esp8266Platform();

    @ImageOptions(width = 25, height = 28)
    @Source("images/cpp_platform.png")
    ImageResource cppPlatform();

}
