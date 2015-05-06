package org.kaaproject.kaa.demo.powerplant.data;

import java.util.List;

import org.kaaproject.kaa.demo.powerplant.pojo.DataReport;

public interface DataEndpoint {

    DataReport getLatestData();
    
    List<DataReport> getHistoryData(long fromTime);
    
}
