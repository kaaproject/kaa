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

package org.kaaproject.kaa.server.datamigration;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.kaaproject.kaa.server.datamigration.model.Ctl;
import org.kaaproject.kaa.server.datamigration.model.CtlMetaInfo;
import org.kaaproject.kaa.server.datamigration.model.FlatCtl;
import org.kaaproject.kaa.server.datamigration.model.Schema;
import org.kaaproject.kaa.server.datamigration.utils.datadefinition.DataDefinition;
import org.kaaproject.kaa.server.common.core.algorithms.generation.ConfigurationGenerationException;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithmImpl;
import org.kaaproject.kaa.server.common.core.configuration.RawData;
import org.kaaproject.kaa.server.common.core.configuration.RawDataFactory;
import org.kaaproject.kaa.server.common.core.schema.RawSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static java.lang.String.format;
import static java.util.Arrays.asList;

//TODO simplify logic of aggregation and add promotion
public class CTLAggregation {
    private Connection connection;
    private QueryRunner runner;
    private DataDefinition dd;

    private static final Logger LOG = LoggerFactory.getLogger(CTLAggregation.class);
    private Map<Ctl, List<Schema>> schemasToCTL;
    private Set<Ctl> ctls;


    public CTLAggregation(Connection connection) {
        this.connection = connection;
        runner = new QueryRunner();
        dd = new DataDefinition(connection);
        schemasToCTL = new HashMap<>();
        ctls = new HashSet<>();
    }


    /*
    * Return schemas and CTLs map that further used for creating new records in base_schema table
    * */
    public Map<Ctl, List<Schema>> aggregate(List<Schema> schemas) throws SQLException, ConfigurationGenerationException, IOException {
        Long currentCTLMetaId = runner.query(connection, "select max(id) as max_id from ctl_metainfo", rs -> rs.next() ? rs.getLong("max_id") : null);
        Long currentCtlId = runner.query(connection, "select max(id) as max_id from ctl", rs -> rs.next() ? rs.getLong("max_id") : null);

        List<FlatCtl> flatCtls = runner.query(connection, "select c.id as ctlId, body, m.id as metaInfoId, fqn, application_id as appId, tenant_id as tenantId from ctl c join ctl_metainfo m on c.metainfo_id = m.id", new BeanListHandler<>(FlatCtl.class));

        // fetch existed CTLs
        for (FlatCtl flatCtl : flatCtls) {
            Ctl ctl = flatCtl.toCtl();
            ctl.setExistInDb(true);
            ctls.add(ctl);
            schemasToCTL.put(ctl, new ArrayList<>());
        }

        // CTL creation
        for (Schema schema : schemas) {
            currentCTLMetaId++;
            currentCtlId++;
            org.apache.avro.Schema schemaBody = new org.apache.avro.Schema.Parser().parse(schema.getSchems());
            String fqn = schemaBody.getFullName();
            RawSchema rawSchema = new RawSchema(schemaBody.toString());
            DefaultRecordGenerationAlgorithm<RawData> algotithm = new DefaultRecordGenerationAlgorithmImpl<>(rawSchema, new RawDataFactory());
            String defaultRecord = algotithm.getRootData().getRawData();
            Long tenantId = null;
            if (schema.getAppId() != null) {
                tenantId = runner.query(connection, "select tenant_id from application where id = " + schema.getAppId(), rs -> rs.next() ? rs.getLong("tenant_id") : null);
            } else {
                tenantId = runner.query(connection, "select tenant_id from events_class where id = " + schema.getId() , rs -> rs.next() ? rs.getLong("tenant_id") : null);
            }

            Ctl ctl = new Ctl(currentCtlId, new CtlMetaInfo(currentCTLMetaId, fqn, schema.getAppId(), tenantId), defaultRecord);

            // aggregation
            if (ctls.isEmpty()) {
                schemasToCTL.put(ctl, new ArrayList<>(asList(schema)));
                ctls.add(ctl);
            } else {
                Ctl ctlToCompare = sameFqn(ctls, ctl);

                if (ctlToCompare != null) {

                    if (bothAppIdNull(ctlToCompare, ctl)) {

                        if (sameTenant(ctlToCompare, ctl)) {
                            aggregateSchemas(ctlToCompare, ctl, schema);
                        } else {
                            putToMapSchema(ctlToCompare, ctl, schema, "tenant");
                        }

                    } else {

                        if (sameAppId(ctlToCompare, ctl)) {
                            aggregateSchemas(ctlToCompare, ctl, schema);
                        } else {
                            putToMapSchema(ctlToCompare, ctl, schema, "application");
                        }

                    }

                } else {
                    ctlToCompare = sameBody(ctls, ctl);
                    if (ctlToCompare != null) {
                        LOG.warn("Schemas {} and {} have different fqn but same body {}", ctl.getMetaInfo().getFqn(), ctlToCompare.getMetaInfo().getFqn(), ctl.getDefaultRecord());
                    }
                    schemasToCTL.put(ctl, new ArrayList<>(asList(schema)));
                    ctls.add(ctl);
                }
            }

        }


        // add CTLs to database
        for (Ctl ctl : ctls) {
            if(ctl.isExistInDb()) {
                continue;
            }

            CtlMetaInfo mi = ctl.getMetaInfo();
            Schema s = schemasToCTL.get(ctl).get(0);
            runner.insert(connection, "insert into ctl_metainfo values(?, ?, ?, ?)", new ScalarHandler<Long>(), mi.getId(), mi.getFqn(), mi.getAppId(), mi.getTenantId());
            runner.insert(connection, "insert into ctl values(?, ?, ?, ?, ?, ?, ?)", new ScalarHandler<Long>(), ctl.getId(), s.getSchems(), s.getCreatedTime(),
                    s.getCreatedUsername(), ctl.getDefaultRecord(), s.getVersion(), mi.getId());

        }

        return schemasToCTL;

    }

    private Ctl sameFqn(Set<Ctl> set, Ctl ctl) {
        for (Ctl ctl1 : set) {
            if (ctl1.getMetaInfo().getFqn().equals(ctl.getMetaInfo().getFqn())) {
                return ctl1;
            }
        }
        return null;
    }


    private Ctl sameBody(Set<Ctl> set, Ctl ctl) {
        for (Ctl ctl1 : set) {
            if (ctl1.getDefaultRecord().equals(ctl.getDefaultRecord())) {
                return ctl1;
            }
        }
        return null;
    }

    private boolean bothAppIdNull(Ctl c1, Ctl c2) {
        return c1.getMetaInfo().getAppId() == null && c2.getMetaInfo().getAppId() == null;
    }


    private boolean sameBody(Ctl c1, Ctl c2) {
        return c1.getDefaultRecord().equals(c2.getDefaultRecord());
    }

    private boolean sameAppId(Ctl c1, Ctl c2) {
        return c1.getMetaInfo().getAppId().equals(c2.getMetaInfo().getAppId());
    }


    private boolean sameTenant(Ctl c1, Ctl c2) {
        return c1.getMetaInfo().getTenantId().equals(c2.getMetaInfo().getTenantId());
    }


    private void putToMapSchema(Ctl c1, Ctl newCtl, Schema schema, String scope) {
        if (!sameBody(c1, newCtl)) {
            LOG.warn("Schemas in different {}s' scopes have different bodies {} and {} but the same fqn {}", scope, newCtl.getDefaultRecord(), c1.getDefaultRecord(), newCtl.getMetaInfo().getFqn());
        } else {
            LOG.debug("Schemas with fqn {} in {}s {} and {} can be promoted to {} scope",
                    newCtl.getMetaInfo().getFqn(), scope,
                    newCtl.getMetaInfo().getAppId(),
                    c1.getMetaInfo().getAppId(),
                    scope.equals("application") ? "tenant" : "system"
            );
        }

        schemasToCTL.put(newCtl, new ArrayList<>(asList(schema)));
        ctls.add(newCtl);
    }

    private void aggregateSchemas(Ctl c1, Ctl c2, Schema schema) {
        if (!sameBody(c1, c2)) {
            CtlMetaInfo mi = c1.getMetaInfo();
            String message = format("Unable to do migrate due to schemas with same fqn[%s] and scope[appId=%d, tenant=%d] but different bodies", mi.getFqn(), mi.getAppId(), mi.getTenantId());
            throw new MigrationException(message);
        }
        LOG.debug("Schemas with fqn {} were aggregated.", c1.getMetaInfo().getFqn());
        List<Schema> sc = schemasToCTL.get(c1);
        sc.add(schema);
    }
}
