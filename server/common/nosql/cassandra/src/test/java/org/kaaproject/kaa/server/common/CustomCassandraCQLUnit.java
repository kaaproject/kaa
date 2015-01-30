package org.kaaproject.kaa.server.common;

import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.CQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;

import java.util.concurrent.TimeUnit;

public class CustomCassandraCQLUnit extends CassandraCQLUnit {

    private static long DEFAULT_CASSANDRA_TIMEOUT = 20000L;

    private CQLDataSet dataSet;


    public CustomCassandraCQLUnit(CQLDataSet dataSet) {
        super(dataSet);
        this.dataSet = dataSet;
    }

    public CustomCassandraCQLUnit(CQLDataSet dataSet, String configurationFileName) {
        super(dataSet, configurationFileName);
        this.dataSet = dataSet;
    }

    public CustomCassandraCQLUnit(CQLDataSet dataSet, String configurationFileName, String hostIp, int port) {
        super(dataSet, configurationFileName, hostIp, port);
        this.dataSet = dataSet;
    }

    @Override
    protected void before() throws Exception {
        /* start an embedded Cassandra */
        if (configurationFileName != null) {
            EmbeddedCassandraServerHelper.startEmbeddedCassandra(configurationFileName, DEFAULT_CASSANDRA_TIMEOUT);
        } else {
            EmbeddedCassandraServerHelper.startEmbeddedCassandra(DEFAULT_CASSANDRA_TIMEOUT);
        }

        /* create structure and load data */
        load();
    }

    @Override
    protected void load() {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.load();
    }
}
