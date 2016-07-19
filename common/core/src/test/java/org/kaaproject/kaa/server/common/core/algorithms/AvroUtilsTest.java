package org.kaaproject.kaa.server.common.core.algorithms;


import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class AvroUtilsTest {
    private JsonNode data;
    private JsonNode dataWithUUIDs;

    @Before
    public void setUp() throws IOException {
        data = new ObjectMapper().readTree(AvroUtilsTest.class.getClassLoader().getResourceAsStream("uuids/data.json"));
        dataWithUUIDs = new ObjectMapper().readTree(AvroUtilsTest.class.getClassLoader().getResourceAsStream("uuids/data_with_uuids.json"));
    }

    @Test
    public void testInjectUuids() throws IOException {
        AvroUtils.injectUuids(data);
        String jsonWithUUIds = data.toString();
        Assert.assertTrue("Generated json is not equal json with UUIDs", jsonWithUUIds.equals(dataWithUUIDs.toString()));
    }


    @Test
    public void testRemoveUuids() throws IOException {
        AvroUtils.removeUuids(dataWithUUIDs);
        String json = dataWithUUIDs.toString();
        Assert.assertTrue("Generated json is not equal json without UUIDs", json.equals(data.toString()));
    }

}
