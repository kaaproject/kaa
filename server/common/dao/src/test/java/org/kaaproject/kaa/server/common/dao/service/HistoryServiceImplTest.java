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

package org.kaaproject.kaa.server.common.dao.service;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ChangeType;
import org.kaaproject.kaa.common.dto.HistoryDto;
import org.kaaproject.kaa.server.common.dao.AbstractTest;

@Ignore("This test should be extended and initialized with proper context in each NoSQL submodule")
public class HistoryServiceImplTest extends AbstractTest {

    private ApplicationDto application;

    @Before
    public void beforeTest() {
        application = generateApplicationDto();
        generateConfSchemaDto(null, application.getId(), 1);
        generateProfSchemaDto(application.getTenantId(), application.getId(), 1);
    }

    @Test
    public void findHistoriesByAppIdTest() {
        List<HistoryDto> historyRows = historyService.findHistoriesByAppId(application.getId());
        Assert.assertEquals(2, historyRows.size());
        Assert.assertEquals(ChangeType.ADD_CONF, historyRows.get(0).getChange().getType());
        Assert.assertEquals(ChangeType.ADD_CONF, historyRows.get(1).getChange().getType());
    }

    @Test
    public void findHistoryBySeqNumberTest() {
        HistoryDto history = historyService.findHistoryBySeqNumber(application.getId(), 2);
        Assert.assertEquals(ChangeType.ADD_CONF, history.getChange().getType());
    }

    @Test
    public void findHistoriesBySeqNumberStartTest() {
        List<HistoryDto> historyRows = historyService.findHistoriesBySeqNumberStart(application.getId(), 1);
        Assert.assertEquals(1, historyRows.size());
        Assert.assertEquals(ChangeType.ADD_CONF, historyRows.get(0).getChange().getType());
    }

    @Test
    public void findHistoriesBySeqNumberRangeTest() {
        List<HistoryDto> historyRows = historyService.findHistoriesBySeqNumberRange(application.getId(), 1, 2);
        Assert.assertEquals(1, historyRows.size());
        Assert.assertEquals(ChangeType.ADD_CONF, historyRows.get(0).getChange().getType());
    }

}
