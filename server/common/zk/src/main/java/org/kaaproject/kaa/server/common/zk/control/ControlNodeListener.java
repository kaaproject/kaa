/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.common.zk.control;

import org.kaaproject.kaa.server.common.zk.gen.ControlNodeInfo;


/**
 * The listener interface for receiving controlNode events.
 * The class that is interested in processing a controlNode
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addControlNodeListener<code> method. When
 * the controlNode event occurs, that object's appropriate
 * method is invoked.
 *
 * @see ControlNodeEvent
 */
public interface ControlNodeListener {
    
    /**
     * On control node down.
     */
    void onControlNodeDown();

    /**control
     * On control node change.
     *
     * @param nodeInfo the node info
     */
    void onControlNodeChange(ControlNodeInfo nodeInfo);
}
