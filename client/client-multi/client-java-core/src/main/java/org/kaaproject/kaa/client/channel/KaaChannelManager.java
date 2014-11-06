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

import java.util.List;

import org.kaaproject.kaa.client.channel.connectivity.ConnectivityChecker;
import org.kaaproject.kaa.client.channel.impl.channels.DefaultBootstrapChannel;
import org.kaaproject.kaa.client.channel.impl.channels.DefaultOperationHttpChannel;
import org.kaaproject.kaa.client.channel.impl.channels.DefaultOperationsChannel;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;

/**
 * Channel manager establishes/removes channels' links between client and
 * server.<br>
 * <br>
 * Use this manager to add or remove specific network channel implementation
 * for client-server communication.<br>
 * <br>
 * <pre>
 * {@code
 * class SpecificDataChannel implements KaaDataChannel {...}
 * ...
 * SpecificDataChannel dataChannel1 = new SpecificDataChannel();
 * kaaClient.getChannelManager().addChannel(dataChannel1);
 * }
 * </pre>
 * The code above registers new data channel in the KaaChannelManager instance.
 * This channel will be used by each transport abstraction which is supported by
 * this channel (See {@link KaaDataChannel#getSupportedTransportTypes()}).<br>
 * <br>
 * Channel manager will use the latest added channel for each {@link TransportType}
 * for data transferring. For example, if there are two {@link KaaDataChannel}
 * implementations <b>ChannelA</b> and <b>ChannelB</b> such as:
 * <br>
 * <il>
 *      <li>
 *          <b>ChannelA</b> is data transceiver ({@link ChannelDirection#BIDIRECTIONAL})
 *          for transport types [{@link TransportType#EVENT}, {@link TransportType#LOGGING}];
 *      </li>
 *      <li>
 *          <b>ChannelB</b> is data transmitter ({@link ChannelDirection#UP})
 *          for [{@link TransportType#EVENT}].
 *      </li>
 * <il>
 * <br>
 * and they are added to {@link #addChannel(KaaDataChannel)} in the following order:
 * <br>
 * <pre>
 * {@code
 * ChannelA channelA = new ChannelA();
 * ChannelB channelB = new ChannelB();
 * kaaClient.getChannelManager().addChannel(channelA);
 * kaaClient.getChannelManager().addChannel(channelB);
 * }
 * </pre>
 * then <b>ChannelA</b> instance will be used to receive {@link TransportType#EVENT}
 * and {@link TransportType#LOGGING} data, but to transmit only {@link TransportType#LOGGING}
 * data. For {@link TransportType#EVENT} data transmission will be used
 * <b>ChannelB</b> instance.<br>
 * <b>NOTE:</b> If mentioned above channels will be added in reverse order,
 * <b>ChannelB</b> instance will not be used until channelA will not be removed
 * using {@link #removeChannel(KaaDataChannel)}<br>
 * <br>
 * On Kaa initialization KaaChannelManager instance is populated with
 * {@link DefaultBootstrapChannel}, {@link DefaultOperationsChannel} and
 * {@link DefaultOperationHttpChannel} (in given order).<br>
 * <br>
 * Calling {@link #removeChannel(KaaDataChannel)} forces KaaChannelManager to
 * remove given channel and reset TransportType-to-Channel internal mapping with
 * applicable channel if such exists.<br>
 * <br>
 * Call to {@link #clearChannelList()} removes <b>all</b> existing channels.<br>
 * <br>
 * If physical connection to remote server failed, call {@link #onServerFailed(ServerInfo)}
 * to switch to another available server.
 *
 * @author Yaroslav Zeygerman
 *
 * @see KaaDataChannel
 */
public interface KaaChannelManager {

    /**
     * Updates the manager by setting the channel to specified {@link TransportType}.
     *
     * @param transport
     *            transport type which is going to receive updates using the specified channel.
     * @param channel
     *            channel to be added.
     * @see KaaDataChannel
     *
     */
    void setChannel(TransportType transport, KaaDataChannel channel) throws KaaInvalidChannelException;

    /**
     * Updates the manager by adding the channel.
     *
     * @param channel
     *            channel to be added.
     * @see KaaDataChannel
     *
     */
    void addChannel(KaaDataChannel channel);

    /**
     * Updates the manager by removing the channel from the manager.
     *
     * @param channel channel to be removed.
     * @see KaaDataChannel
     *
     */
    void removeChannel(KaaDataChannel channel);

    /**
     * Updates the manager by removing the channel from the manager.
     *
     * @param id the channel's id.
     * @see KaaDataChannel
     *
     */
    void removeChannel(String id);

    /**
     * Retrieves the list of current channels.
     *
     * @return the channels' list.
     * @see KaaDataChannel
     *
     */
    List<KaaDataChannel> getChannels();

    /**
     * Retrieves the list of channels by the specific type (HTTP, HTTP_LP etc.).
     *
     * @param type type of the channel.
     * @return the channels' list.
     *
     * @see ChannelType
     * @see KaaDataChannel
     *
     */
    List<KaaDataChannel> getChannelsByType(ChannelType type);

    /**
     * Retrieves the list of channels by the specific transport type.
     *
     * @param type the transport's type.
     * @return the channels' list.
     *
     * @see TransportType
     * @see KaaDataChannel
     *
     */
    KaaDataChannel getChannelByTransportType(TransportType type);

    /**
     * Retrieves channel by the unique channel id.
     *
     * @param id the channel's id.
     * @return channel object.
     *
     * @see KaaDataChannel
     *
     */
    KaaDataChannel getChannel(String id);

    /**
     * Reports to Channel Manager in case link with server was not established.
     *
     * @param server the parameters of server that was not connected.
     * @see ServerInfo
     *
     */
    void onServerFailed(ServerInfo server);

    /**
     * Reports to Channel Manager about the new server.
     *
     * @param newServer the parameters of the new server.
     * @see ServerInfo
     *
     */
    void onServerUpdated(ServerInfo newServer);

    /**
     * Clears the list of channels.
     */
    void clearChannelList();

    /**
     * Sets connectivity checker to the existing channels.
     *
     * @param checker platform-dependent connectivity checker.
     * @see ConnectivityChecker
     *
     */
    void setConnectivityChecker(ConnectivityChecker checker);
}
