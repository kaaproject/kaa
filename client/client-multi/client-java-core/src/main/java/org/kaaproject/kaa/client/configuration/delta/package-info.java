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

/**
 * Provides interfaces to deal with configuration deltas.
 *
 * <p>Assume, received root delta looks as follows:</p>
 * <pre>
 * {@code
 * {
 *     "schemaName": "testT",
 *     "schemaNamespace": "org.kaa.config",
 *     "testField1": "abc",
 *     "testField2": {
 *          "schemaName": "testRecordT",
 *          "schemaNamespace": "org.kaa.config",
 *          "testField3": 456,
 *          "__uuid": {
 *              "schemaName": "uuidT",
 *              "schemaNamespace": "org.kaaproject.configuration",
 *              "value": [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1]
 *          }
 *     },
 *     "__uuid": {
 *     "schemaName": "uuidT",
 *     "schemaNamespace": "org.kaaproject.configuration",
 *     "value": [1,2,3,4,5,6,7,1,1,1,11,1,1,14,15,16]
 *     }
 * }
 * }
 * </pre>
 *
 * <p>In order to access to the received data, do following:</p>
 * <pre>
 * {@code
 * ConfigurationDelta rootDelta = // obtained from a delta receiver;
 *
 * if (rootDelta.hasChanged("testField1")) {
 *     DeltaType dt = rootDelta.getDeltaType("testField1");
 *     String testField1 = (String)dt.getNewValue();
 *     System.out.println("testField1: " + testField1);
 * }
 *
 * DeltaType subrecordDT = rootDelta.getDeltaType("testField2");
 *
 * if (subrecordDT != null) {
 *     ConfigurationDelta testField2 = (ConfigurationDelta)subrecordDT.getNewValue();
 *
 *     if (testField2 != null) {
 *         Integer testField3 = (Integer)testField2.getDeltaType("testField3").getNewValue();
 *         System.out.println("testField3: " + testField3);
 *     }
 * }
 * }
 * </pre>
 *
 * @see org.kaaproject.kaa.client.configuration.delta.ConfigurationDelta
 * @see org.kaaproject.kaa.client.configuration.delta.DeltaType
 * @see org.kaaproject.kaa.client.configuration.delta.manager.DeltaManager
 * @see org.kaaproject.kaa.client.configuration.delta.manager.DeltaReceiver
 */
package org.kaaproject.kaa.client.configuration.delta;