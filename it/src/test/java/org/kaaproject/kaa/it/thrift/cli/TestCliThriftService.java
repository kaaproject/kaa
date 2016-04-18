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

package org.kaaproject.kaa.it.thrift.cli;

import org.kaaproject.kaa.server.common.thrift.cli.server.BaseCliThriftService;

public class TestCliThriftService extends BaseCliThriftService {

    private final String thriftCliServerName;
    
    /**
     * Instantiates a new test cli thrift service.
     *
     * @param thriftCliServerName the thrift cli server name
     */
    public TestCliThriftService(String thriftCliServerName) {
        this.thriftCliServerName = thriftCliServerName;
    }
    
    @Override
    protected String getServerShortName() {
        return thriftCliServerName;
    }

    @Override
    protected void initServiceCommands() {
    }

}
