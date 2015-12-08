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
package org.kaaproject.kaa.server.sync;

import java.util.List;

/**
 * Basic sync structure that holds list of extension sync objects
 * 
 * @author Andrew Shvayka
 * 
 * @see ExtensionSync
 *
 */
public abstract class PluginSync {

    private final List<ExtensionSync> extSyncList;

    public PluginSync(List<ExtensionSync> extSyncList) {
        super();
        if (extSyncList == null) {
            throw new IllegalArgumentException("Extenstion sync list can't be null!");
        } else {
            this.extSyncList = extSyncList;
        }
    }

    public List<ExtensionSync> getExtSyncList() {
        return extSyncList;
    }

}
