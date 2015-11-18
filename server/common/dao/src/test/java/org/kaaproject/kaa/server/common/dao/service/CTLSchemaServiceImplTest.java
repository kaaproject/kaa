package org.kaaproject.kaa.server.common.dao.service;

import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.server.common.dao.AbstractTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.Rollback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Ignore("This test should be extended and initialized with proper context in each NoSQL submodule")
public class CTLSchemaServiceImplTest extends AbstractTest {

    private static final Logger LOG = LoggerFactory.getLogger(CTLSchemaServiceImplTest.class);

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Test
    @Rollback(false)
    public void testUpdateCTLSchemaScope() {
    }

    @Test
    @Rollback(false)
    public void testRemoveCTLSchemaByFqnAndVerAndTenantId() {
    }

    @Test
    @Rollback(false)
    public void testFindCTLSchemaById() {
    }

    @Test
    @Rollback(false)
    public void testSaveCTLSchema() {
    }

    @Test
    @Rollback(false)
    public void testRemoveCTLSchemaById() {
    }

    @Test
    @Rollback(false)
    public void testFindCTLSchemaByFqnAndVerAndTenantId() {
    }

    @Test
    @Rollback(false)
    public void testFindCTLSchemasByApplicationId() {
    }

    @Test
    @Rollback(false)
    public void testFindCTLSchemasByTenantId() {
    }

    @Test
    @Rollback(false)
    public void testFindSystemCTLSchemas() {
    }

    @Test
    @Rollback(false)
    public void testFindSystemCTLSchemasMetaInfo() {
    }

    @Test
    @Rollback(false)
    public void testFindLatestCTLSchemaByFqn() {
    }

    @Test
    @Rollback(false)
    public void testFindCTLSchemas() {
    }

    @Test
    @Rollback(false)
    public void testFindCTLSchemasMetaInfoByApplicationId() {
    }

    @Test
    @Rollback(false)
    public void testFindCTLSchemasMetaInfoByTenantId() {
    }

    @Test
    @Rollback(false)
    public void testFindAvailableCTLSchemas() {
    }

    @Test
    @Rollback(false)
    public void testFindAvailableCTLSchemasMetaInfo() {
    }

    @Test
    @Rollback(false)
    public void testFindCTLSchemaDependents() {
    }

    @Test
    @Rollback(false)
    public void testFindCTLSchemaDependentsByFqnVersionTenantId() {
    }

    @Test
    @Rollback(false)
    public void multiThreadCTLSchemaSaveTest() throws InterruptedException, ExecutionException {
        List<Future<CTLSchemaDto>> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(executorService.submit(new Callable<CTLSchemaDto>() {
                @Override
                public CTLSchemaDto call() {
                    CTLSchemaDto sch = null;
                    try {
                        sch = ctlService.saveCTLSchema(generateCTLSchemaDto(generateTenantDto().getId()));
                    } catch (Throwable t) {
                        LOG.warn("---> Test Catch exception {}", t.getCause(), t);
                    }
                    return sch;
                }
            }));
        }
        for (Future<CTLSchemaDto> f : list) {
            LOG.debug("id {}", f.get());
        }
    }
}
