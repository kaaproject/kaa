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

package org.kaaproject.kaa.client.connectivity;

import org.kaaproject.kaa.client.channel.connectivity.ConnectivityChecker;
import org.kaaproject.kaa.client.channel.impl.channels.DefaultBootstrapChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.MessageFormat;

public class PingConnectivityChecker implements ConnectivityChecker {
  public static final Logger LOG = LoggerFactory.getLogger(PingConnectivityChecker.class);
  private static final String DEFAULT_HOST = "http://www.google.com";

  private final String urlAddress;

  public PingConnectivityChecker() {
    this(DEFAULT_HOST);
  }

  public PingConnectivityChecker(String urlAddress) {
    this.urlAddress = urlAddress;
  }

  @Override
  public boolean checkConnectivity() {
    try {
      final URL url = new URL(urlAddress);
      final HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();
      urlConnect.connect();
      return true;

    } catch (UnknownHostException ex) {
      LOG.warn(MessageFormat.format("Host {0} is unreachable", urlAddress), ex);
      return false;
    } catch (IOException ex) {
      LOG.warn(MessageFormat.format("Host {0} is unreachable", urlAddress), ex);
      return false;
    }

  }

}
