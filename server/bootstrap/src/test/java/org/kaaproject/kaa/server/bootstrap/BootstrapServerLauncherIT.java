package org.kaaproject.kaa.server.bootstrap;

import java.util.concurrent.TimeoutException;

import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.junit.Test;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BootstrapServerLauncherIT {
    private static final int STARTUP_TIMEOUT = 30000;

    private static final int SHUTDOWN_TIMEOUT = 30000;

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(BootstrapServerLauncherIT.class);

    /** The Constant HOST. */
    private static final String HOST = "localhost";

    /** The Constant PORT. */
    private static final int PORT = 19092;

    /**
     * Test start operations server application.
     *
     * @throws Exception the exception
     */
    @Test
    public void testStartOperationsServerWithZKApplication() throws Exception {
        TestingCluster zkCluster = null;
        TTransport transport = null;
        Thread operationsServerLauncherThread = null;
        OperationsThriftService.Client client = null;
        try {
            zkCluster = new TestingCluster(new InstanceSpec(null, 2185, -1, -1, true, -1, -1, -1));
            zkCluster.start();

            operationsServerLauncherThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    logger.info("Starting Operations Server ...");
                    BootstrapServerApplication.main(new String[]{"common-zk-test-context.xml"});
                    logger.info("Operations Server Stopped");
                }
            });

            operationsServerLauncherThread.start();

            Thread.sleep(STARTUP_TIMEOUT);

            transport = new TSocket(HOST, PORT);
            TProtocol protocol = new TBinaryProtocol(transport);
            client = new OperationsThriftService.Client(protocol);
            transport.open();
            client.shutdown();
        }
        finally {
            if (transport != null && transport.isOpen()) {
                try {
                    transport.close();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (operationsServerLauncherThread != null) {
                operationsServerLauncherThread.join(SHUTDOWN_TIMEOUT);
                if (operationsServerLauncherThread.isAlive()) {
                    throw new TimeoutException("Timeout (10 sec) occured while waiting endpoint server shutdown thread!");
                }
            }
            logger.info("ZKCluster Stop started");
            if (zkCluster != null) {
                logger.info("Closing zk cluster");
                zkCluster.close();
            }
            logger.info("ZKCluster Stop ended");
        }
    }
}
