package org.kaaproject.kaa.server.common.dao.cassandra.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;

import static org.junit.Assert.*;

public class CassandraEndpointConfigurationTest {

    @Test
    public void hashCodeEqualsTest(){
        EqualsVerifier.forClass(CassandraEndpointConfiguration.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }
}