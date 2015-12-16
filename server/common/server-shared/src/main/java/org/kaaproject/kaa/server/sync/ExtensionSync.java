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

/**
 * Holds extension id that identifies plugin instance within certain SDK profile
 * and data to be transfers between SDK and plugin instance.
 * 
 * @author Andrew Shvayka
 *
 */
public class ExtensionSync {

    private final int extensionId;
    private final byte[] data;

    public ExtensionSync(int extensionId, byte[] data) {
        super();
        this.extensionId = extensionId;
        this.data = data;
    }

    public int getExtensionId() {
        return extensionId;
    }

    public byte[] getData() {
        return data;
    }

}
