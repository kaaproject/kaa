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

package org.kaaproject.kaa.client.channel;

import org.kaaproject.kaa.client.channel.failover.FailoverManager;
import org.kaaproject.kaa.client.channel.failover.FailoverStatus;
import org.kaaproject.kaa.client.channel.impl.channels.DefaultBootstrapChannel;
import org.kaaproject.kaa.client.channel.impl.channels.DefaultOperationHttpChannel;
import org.kaaproject.kaa.client.channel.impl.channels.DefaultOperationsChannel;
import org.kaaproject.kaa.common.TransportType;

import java.util.List;

/**
 * Channel manager establishes/removes channels' links between client and
 * server.<br>
 * <br>
 * Use this manager to add or remove specific network channel implementation for
 * client-server communication.<br>
 * <br>
 *
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
 * Channel manager will use the latest added channel for each
 * {@link TransportType} for data transferring. For example, if there are two
 * {@link KaaDataChannel} implementations <b>ChannelA</b> and <b>ChannelB</b>
 * such as: <br>
 * <b>ChannelA</b> is data transceiver ({@link ChannelDirection#BIDIRECTIONAL})
 * for transport types [{@link TransportType#EVENT},
 * {@link TransportType#LOGGING}];
 * <b>ChannelB</b> is data transmitter ({@link ChannelDirection#UP}) for [
 * {@link TransportType#EVENT}]. <br>
 * and they are added to {@link #addChannel(KaaDataChannel)} in the following
 * order: <br>
 *
 * <pre>
 * {
 *     &#064;code
 *     ChannelA channelA = new ChannelA();
 *     ChannelB channelB = new ChannelB();
 *     kaaClient.getChannelManager().addChannel(channelA);
 *     kaaClient.getChannelManager().addChannel(channelB);
 * }
 * </pre>
 * then <b>ChannelA</b> instance will be used to receive
 * {@link TransportType#EVENT} and {@link TransportType#LOGGING} data, but to
 * transmit only {@link TransportType#LOGGING} data. For
 * {@link TransportType#EVENT} data transmission will be used <b>ChannelB</b>
 * instance.<br>
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
 * If physical connection to remote server failed, call
 * {@link #onServerFailed(TransportConnectionInfo, FailoverStatus)} to switch to another
 * available server.
 *
 * @author Yaroslav Zeygerman
 * @see KaaDataChannel
 */
public interface KaaChannelManager {

  /**
   * Updates the manager by setting the channel to the specified
   * {@link TransportType}.
   *
   * @param transport the type of the transport which is going to receive updates using the
   *                  specified channel.
   * @param channel   the channel to be added.
   * @throws KaaInvalidChannelException the kaa invalid channel exception
   * @see KaaDataChannel
   */
  void setChannel(TransportType transport, KaaDataChannel channel)
          throws KaaInvalidChannelException;

  /**
   * Updates the manager by adding the channel.
   *
   * @param channel sending/receiving data for endpoint server
   */
  void addChannel(KaaDataChannel channel);

  /**
   * Updates the manager by removing the channel from the manager.
   *
   * @param channel the channel to be removed.
   * @see KaaDataChannel
   */
  void removeChannel(KaaDataChannel channel);

  /**
   * Updates the manager by removing the channel from the manager.
   *
   * @param id the channel's id.
   * @see KaaDataChannel
   */
  void removeChannel(String id);

  /**
   * Retrieves the list of current channels.
   *
   * @return the channels' list.
   * @see KaaDataChannel
   */
  List<KaaDataChannel> getChannels();

  /**
   * Retrieves channel by the unique channel id.
   *
   * @param id the channel's id.
   * @return channel object.
   * @see KaaDataChannel
   */
  KaaDataChannel getChannel(String id);

  /**
   * Reports to Channel Manager in case link with server was not established.
   *
   * @param server the parameters of server that was not connected.
   * @param status failover status
   * @see TransportConnectionInfo
   */
  void onServerFailed(TransportConnectionInfo server, FailoverStatus status);

  /**
   * Clears the list of channels.
   */
  void clearChannelList();

  /**
   * Invoke sync on active channel by specified transport type.
   *
   * @param type the type
   */
  void sync(TransportType type);

  /**
   * Invoke sync acknowledgement on active channel by specified transport type.
   *
   * @param type the type
   */
  void syncAck(TransportType type);

  /**
   * Invoke sync acknowledgement on active channel.
   *
   * @param type - type that is used to identify active channel
   */
  void syncAll(TransportType type);

  /**
   * Returns information about server that is used for data transfer for specified {@link
   * TransportType}.
   *
   * @param type - type that is used to identify active channel
   * @return TransportConnectionInfo active server
   */
  TransportConnectionInfo getActiveServer(TransportType type);

  /**
   * Sets a new failover manager.
   *
   * @param failoverManager the failover manager
   */
  void setFailoverManager(FailoverManager failoverManager);
}
