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

/**
 *
 */
package org.kaaproject.kaa.server.transports.http.transport.commands;

import org.kaaproject.kaa.common.endpoint.CommonEPConstans;
import org.kaaproject.kaa.server.transport.channel.ChannelType;

/**
 * The Class SyncCommand.
 */
public class SyncCommand extends AbstractHttpSyncCommand implements CommonEPConstans {

    @Override
    public ChannelType getChannelType() {
        return ChannelType.SYNC;
    }

    /**
     * Instantiates a new sync command.
     */
    public SyncCommand() {
        super();
        LOG.trace("CommandName: " + COMMAND_NAME + ": Created..");
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.http.server.CommandProcessor#isNeedConnectionClose()
     */
    @Override
    public boolean isNeedConnectionClose() {
        return true;
    }

    public static String getCommandName() {
        return SYNC_COMMAND;
    }

}
