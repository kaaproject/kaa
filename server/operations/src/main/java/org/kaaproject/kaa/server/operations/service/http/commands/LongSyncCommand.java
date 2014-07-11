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
 *
 */
package org.kaaproject.kaa.server.operations.service.http.commands;



/**
 * The Class UpdateEndpointCommand.
 */
public class LongSyncCommand extends SyncCommand {

    static {
        COMMAND_NAME = LONG_SYNC_COMMAND;
        LOG.info("CommandName: " + COMMAND_NAME);
    }

    @Override
    public ChannelType getChannelType() {
        return ChannelType.HTTP_LP;
    }

    /**
     * Instantiates a new sync command.
     */
    public LongSyncCommand() {
        super();
        LOG.trace("CommandName: " + COMMAND_NAME + ": Created..");
    }

    public static String getCommandName() {
        return LONG_SYNC_COMMAND;
    }
}
