package org.kaaproject.kaa.server.operations.pojo;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;

public class GetDeltaRequestTest {

    @Test
    public void getDeltaRequestTest() {
        int sequenceNumber = 1;
        GetDeltaRequest deltaRequest = new GetDeltaRequest("token", sequenceNumber);
        deltaRequest.setFetchSchema(true);
        Assert.assertTrue(deltaRequest.isFetchSchema());
        Assert.assertEquals(sequenceNumber, deltaRequest.getSequenceNumber());
    }

    @Test
    public void getDeltaExceptionTest() {
        GetDeltaException getDeltaException;
        getDeltaException = new GetDeltaException("");
        getDeltaException = new GetDeltaException(new Exception());
    }

}
