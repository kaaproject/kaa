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

package org.kaaproject.kaa.server.control;

import java.io.IOException;

import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.common.thrift.gen.control.FileData;

public class ControlServerRecordLibraryIT extends AbstractTestControlServer {

    /**
     * Test generate record library.
     *
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGenerateRecordLibrary() throws TException, IOException {
        ApplicationDto application = createApplication();
        LogSchemaDto logSchema = createLogSchema(application.getId());

        FileData library =
                client.generateRecordStructureLibrary(application.getId(), logSchema.getMajorVersion());
        Assert.assertNotNull(library);
        Assert.assertFalse(strIsEmpty(library.getFileName()));
        Assert.assertNotNull(library.getData());
    }

}
