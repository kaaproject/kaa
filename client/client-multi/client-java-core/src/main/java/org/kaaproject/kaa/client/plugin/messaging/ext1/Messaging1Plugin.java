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
package org.kaaproject.kaa.client.plugin.messaging.ext1;

import java.util.concurrent.Future;

import org.kaaproject.kaa.client.plugin.PluginAdapter;
import org.kaaproject.kaa.client.plugin.PluginContext;
import org.kaaproject.kaa.client.plugin.PluginInitializationException;
import org.kaaproject.kaa.client.plugin.PluginInstance;
import org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassA;
import org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassB;
import org.kaaproject.kaa.client.plugin.messaging.ext1.avro.ClassC;

public class Messaging1Plugin implements PluginInstance<Messaging1PluginAPI>, PluginAdapter, Messaging1PluginAPI {

    @Override
    public void init(PluginContext context) throws PluginInitializationException {
        // TODO Auto-generated method stub
    }

    @Override
    public PluginAdapter getPluginAdapter() {
        return this;
    }

    @Override
    public Messaging1PluginAPI getPluginAPI() {
        return this;
    }

    /**
     * Generated based on contract items
     */
    @Override
    public byte[] getPendingData() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void processData(byte[] data) {
        // arrays of objects with fields: 
        // UUID - ID
        // int - methodID
        // data - avro byte[]
    }

    @Override
    public void setMethodAListener(MethodAListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setMethodBListener(MethodBListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public Future<ClassC> getC(ClassA msg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<ClassB> getB(ClassA msg) {
        // TODO Auto-generated method stub
        return null;
    }

}
