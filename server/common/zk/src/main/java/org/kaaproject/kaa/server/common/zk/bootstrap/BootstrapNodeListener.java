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

package org.kaaproject.kaa.server.common.zk.bootstrap;

import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;


/**
 * The listener interface for receiving bootstrapNode events.
 * The class that is interested in processing a bootstrapNode
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addBootstrapNodeListener<code> method. When
 * the bootstrapNode event occurs, that object's appropriate
 * method is invoked.
 *
 * @see BootstrapNodeEvent
 */
public interface BootstrapNodeListener {

    /**
     * On node added.
     *
     * @param nodeInfo the node info
     */
    void onNodeAdded(BootstrapNodeInfo nodeInfo);

    /**
     * Invoked when on node update occurs.
     *
     * @param nodeInfo the node info
     */
    void onNodeUpdated(BootstrapNodeInfo nodeInfo);

    /**
     * On node removed.
     *
     * @param nodeInfo the node info
     */
    void onNodeRemoved(BootstrapNodeInfo nodeInfo);
}
