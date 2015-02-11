package org.kaaproject.kaa.client.logging;

public interface LogFailoverCommand extends AccessPointCommand{

    void retryLogUpload();

    void retryLogUpload(int delay);

}
