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
package org.kaaproject.kaa.server.operations.service.akka.actors.io;

import java.io.IOException;

import org.kaaproject.kaa.common.endpoint.protocol.ClientSync;
import org.kaaproject.kaa.common.endpoint.protocol.ServerSync;

public interface PlatformEncDec {

    ClientSync decode(byte[] data) throws IOException;
    
    byte[] encode(ServerSync sync) throws IOException;
    
}
