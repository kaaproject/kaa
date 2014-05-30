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

package org.kaaproject.kaa.client.update.commands;

import java.util.List;
import java.util.Set;

import org.kaaproject.kaa.client.TransportExceptionHandler;
import org.kaaproject.kaa.client.transport.OperationsTransport;
import org.kaaproject.kaa.client.update.commands.Command;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionCommand;

/**
 * Factory of Command objects.
 *
 * @author Yaroslav Zeygerman
 *
 */
public interface CommandFactory {

    /**
     * Sets handler for the {@link TransportException}.
     *
     * @param handler the new handler.
     *
     */
    void setTransportExceptionHandler(TransportExceptionHandler handler);

    /**
     * Creates new registration command.
     *
     * @param transport the endpoint transport for this command.
     * @param profile serialized profile.
     * @return new {@link RegisterCommand}.
     *
     */
    Command createRegisterCommand(OperationsTransport transport, byte [] profile);

    /**
     * Creates new profile update command.
     *
     * @param transport the endpoint transport for this command.
     * @param profile serialized profile.
     * @param acceptedUnicastNotificationIds
     * @return new {@link ProfileUpdateCommand}.
     *
     */
    Command createProfileUpdateCommand(OperationsTransport transport, byte [] profile, Set<String> acceptedUnicastNotificationIds);

    /**
     * Creates new poll command.
     *
     * @param transport the endpoint transport for this command.
     * @param acceptedUnicastNotificationIds accepted unicast notifications from the last response.
     * @param notificationCommands the list of subscription commands.
     * @return new {@link PollCommand}.
     *
     */
    Command createPollCommand(OperationsTransport transport,
            Set<String> acceptedUnicastNotificationIds,
            List<SubscriptionCommand> notificationCommands);

    /**
     * Creates new long poll command.
     *
     * @param transport the endpoint transport for this command.
     * @param timeout the timeout for the long poll request.
     * @param acceptedUnicastNotificationIds accepted unicast notifications from the last response.
     * @param notificationCommands the list of subscription commands.
     * @return new {@link LongPollCommand}.
     *
     */
    Command createLongPollCommand(OperationsTransport transport, Long timeout,
            Set<String> acceptedUnicastNotificationIds,
            List<SubscriptionCommand> notificationCommands);
}
