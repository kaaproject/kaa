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

/**
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
package org.kaaproject.kaa.examples.robotrun.controller.log;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.AbstractKaaClient;
import org.kaaproject.kaa.client.logging.LogCollector;
import org.kaaproject.kaa.examples.robotrun.gen.Borders;
import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderType;
import org.kaaproject.kaa.examples.robotrun.labyrinth.impl.BasicBorderUpdate;
import org.mockito.Mockito;

public class DefaultLogManagerTest {
    @Test(expected = IllegalArgumentException.class)
    public void testInitLogManager() {
        LogManager manager = new DefaultLogManager(null);
    }

    public void testBordersReport() {
        AbstractKaaClient client = Mockito.mock(AbstractKaaClient.class);
        LogCollector collector = Mockito.mock(LogCollector.class);
        Mockito.when(client.getLogCollector()).thenReturn(collector);

        LogManager manager = new DefaultLogManager(client);
        BasicBorderUpdate update = BasicBorderUpdate.hBorder(1, 1, BorderType.FREE);

        manager.reportBorders(update);

        try {
            Mockito.verify(collector, Mockito.times(1)).addLogRecord(Mockito.any(Borders.class));
        } catch (IOException e) {
            Assert.assertTrue(false);
        }
    }
}
