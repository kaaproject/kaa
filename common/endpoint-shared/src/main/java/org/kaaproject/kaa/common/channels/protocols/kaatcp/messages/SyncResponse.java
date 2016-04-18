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

package org.kaaproject.kaa.common.channels.protocols.kaatcp.messages;

import org.kaaproject.kaa.common.channels.protocols.kaatcp.KaaTcpProtocolException;

/**
 * SyncResponse Class.
 * Extend Sync and set request flag to false.
 * @author Andrey Panasenko
 *
 */
public class SyncResponse extends Sync {

    /**
     * @param avroObject the avro object
     * @param isZipped   the is zipped
     * @param isEcrypted the is ecrypted
     */
    public SyncResponse(byte[] avroObject, boolean isZipped, boolean isEcrypted) {
        super(false, avroObject, isZipped, isEcrypted);
    }

    /**
     * Constructor used to migrate from KaaSync after Variable Header decode.
     * @param   old KaaSync object from which new object should be created.
     * @throws  KaaTcpProtocolException the kaa tcp protocol exception
     */
    public SyncResponse(KaaSync old) throws KaaTcpProtocolException {
        super(old);
        setRequest(false);
        decodeAvroObject();
    }
    
    /**
     * Default constructor.
     */
    public SyncResponse() {
        super();
        setRequest(false);
    }

}
