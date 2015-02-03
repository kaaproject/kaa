package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class CassandraNotificationTest {

    @Test
    public void hashCodeEqualsTest(){
        EqualsVerifier.forClass(CassandraNotification.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

}