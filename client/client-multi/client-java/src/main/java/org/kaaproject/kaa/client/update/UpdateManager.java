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

package org.kaaproject.kaa.client.update;

import java.io.IOException;
import java.util.List;

import org.kaaproject.kaa.client.TransportExceptionHandler;
import org.kaaproject.kaa.client.profile.SerializedProfileContainer;
import org.kaaproject.kaa.client.transport.OperationsTransport;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionCommand;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;

/**
 * Manager which is responsible for the communication with endpoint server.
 *
 * @author Yaroslav Zeygerman
 *
 */
public interface UpdateManager{

    /**
     * Update subscription info about voluntary topics
     *
     * @param subscriptions subscription info about each voluntary topic interesting user
     */
    void updateSubscriptionCommands(List<SubscriptionCommand> subscriptions);

    /**
     * Starts manager's polling thread.
     *
     */
    void start() throws IOException;

    void onSyncResponse(SyncResponse response);

    void setSerializedProfileContainer(SerializedProfileContainer container);

    /**
     * Called on profile update.
     *
     * @param newProfile byte array with serialized profile.
     *
     */
    void onProfileChange(byte[] newProfile);

    /**
     * Adds the update listener.
     *
     * @param listener listener which is going to be added.
     *
     */
    boolean addUpdateListener(UpdateListener listener);

    /**
     * Removes the update listener.
     *
     * @param listener listener which is going to be removed.
     *
     */
    boolean removeUpdateListener(UpdateListener listener);

    /**
     * Sets the endpoint transport.
     *
     * @param trapsport transport implementation which is going to be set.
     *
     */
    void setTransport(OperationsTransport t);

    /**
     * Sets the exception handler for the {@link TransportException}.
     *
     * @param handler handler which is going to be set.
     *
     */
    void setTransportExceptionHandler(TransportExceptionHandler handler);

    /**
     * Stops update manager and interrupts current tasks.
     *
     */
    void stop();

    /**
     * Suspends update processes.
     *
     * @param milliseconds time for suspend.
     *
     */
    void failover(Long milliseconds) throws IOException;
}
