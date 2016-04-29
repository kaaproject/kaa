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

package org.kaaproject.kaa.server.common.thrift.cli.client;

import org.apache.thrift.protocol.TProtocol;
import org.kaaproject.kaa.server.common.thrift.gen.cli.CliThriftService;

/**
 * The Class CliClient.<br>
 * Wraps CLI Thrift Client Interface.
 */
public class CliClient extends CliThriftService.Client {

    /**
     * Instantiates a new cli client.
     * 
     * @param prot
     *            the Trift Protocol
     */
    public CliClient(TProtocol prot) {
        super(prot, prot);
    }

}
