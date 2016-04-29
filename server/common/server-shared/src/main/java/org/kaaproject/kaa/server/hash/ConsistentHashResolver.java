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

package org.kaaproject.kaa.server.hash;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.resolve.OperationsServerResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Implementation of {@link OperationsServerResolver} based on consistent hash
 * function and MD5 digest.
 * 
 * @author Andrew Shvayka
 *
 */
public class ConsistentHashResolver implements OperationsServerResolver {

    private static final Logger LOG = LoggerFactory.getLogger(ConsistentHashResolver.class);

    private static final int SIZE_OF_INT = 4;
    private static final String MD5 = "MD5";
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final ThreadLocal<MessageDigest> md5 = new ThreadLocal<MessageDigest>() { //NOSONAR
        @Override
        protected MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance(MD5);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    };
    private final int replicas;
    private final SortedMap<byte[], OperationsNodeInfo> circle;

    public ConsistentHashResolver(List<OperationsNodeInfo> nodes, int replicas) {
        this.replicas = replicas;
        this.circle = new ConcurrentSkipListMap<byte[], OperationsNodeInfo>(new ByteArrayComparator());
        for (OperationsNodeInfo node : nodes) {
            onNodeAdded(node);
        }
    }

    @Override
    public OperationsNodeInfo getNode(String user) {
        if (user == null) {
            throw new RuntimeException("user id is null");
        }
        return getNearest(hash(user));
    }

    @Override
    public void onNodeAdded(OperationsNodeInfo node) {
        for (int i = 0; i < replicas; i++) {
            LOG.trace("Adding node {} replica {} to the circle", node.getConnectionInfo(), i);
            circle.put(hash(node, i), node);
        }
    }

    @Override
    public void onNodeRemoved(OperationsNodeInfo node) {
        for (int i = 0; i < replicas; i++) {
            LOG.trace("Removing node {} replica {} from the circle", node.getConnectionInfo(), i);
            circle.remove(hash(node, i));
        }
    }

    @Override
    public void onNodeUpdated(OperationsNodeInfo node) {
        onNodeRemoved(node);
        onNodeAdded(node);
    }

    private OperationsNodeInfo getNearest(byte[] hash) {
        if (circle.isEmpty()) {
            return null;
        }
        if (circle.size() == 1) {
            return circle.get(circle.firstKey());
        }
        SortedMap<byte[], OperationsNodeInfo> tailMap = circle.tailMap(hash);
        hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
        return circle.get(hash);
    }

    private byte[] hash(OperationsNodeInfo node, int replica) {
        byte[] key = node.getConnectionInfo().getPublicKey().array();
        ByteBuffer data = ByteBuffer.wrap(new byte[key.length + SIZE_OF_INT]);
        data.put(key);
        data.putInt(replica);
        return md5.get().digest(data.array());
    }

    private byte[] hash(String data) {
        return md5.get().digest(data.getBytes(UTF8));
    }

    private final class ByteArrayComparator implements Comparator<byte[]> {
        @Override
        public int compare(byte[] a1, byte[] a2) {
            if (a1 == a2) {
                return 0;
            } else {
                if (a1 == null) {
                    return -1;
                }
                if (a2 == null) {
                    return 1;
                }
                if (a1.length == a2.length) {
                    for (int i = 0; i < a1.length; i++) {
                        if (a1[i] != a2[i]) {
                            return a1[i] - a2[i];
                        }
                    }
                    return 0;
                } else {
                    return a1.length - a2.length;
                }
            }
        }
    }
}
