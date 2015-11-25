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

package org.kaaproject.kaa.client.channel;


/**
 * Multiplexer collects the info about states from different services and
 * compiles it in one request.
 *
 * Required in user implementation of any kind of data channel.
 *
 * @author Yaroslav Zeygerman
 * @author Andrew Shvayka
 *
 */
public interface KaaDataMultiplexer {

    /**
     * Compiles request for given sync task.
     *
     * @param   task the sync task 
     * @return  the serialized request data.
     * @throws  Exception the exception
     *
     * @see ChannelSyncTask
     */
    byte[] compileRequest(ChannelSyncTask task) throws Exception;

}
