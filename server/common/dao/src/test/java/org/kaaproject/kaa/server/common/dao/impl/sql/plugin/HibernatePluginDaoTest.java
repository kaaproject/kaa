package org.kaaproject.kaa.server.common.dao.impl.sql.plugin;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.plugin.ContractItemDto;
import org.kaaproject.kaa.common.dto.plugin.ContractMessageDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractDirection;
import org.kaaproject.kaa.common.dto.plugin.PluginContractDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractItemDto;
import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;
import org.kaaproject.kaa.common.dto.plugin.PluginScope;
import org.kaaproject.kaa.server.common.dao.impl.sql.HibernateAbstractTest;
import org.kaaproject.kaa.server.common.dao.model.sql.plugin.Plugin;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernatePluginDaoTest extends HibernateAbstractTest {

    private static final String NAME = "Plugin name";
    private static final String CLASS_NAME = "ClassName";
    private static final Integer VERSION = 1;

    @Test
    public void testSavePlugin() {
        PluginDto pluginDto = generatePlugin();
        Plugin p = new Plugin(pluginDto);
        p = pluginDao.save(p);
        Assert.assertEquals(pluginDto, p.toDto());
    }

    @Test
    public void testFindPluginByNameAndVersion() {
        PluginDto pluginDto = generatePlugin();
        Plugin p = new Plugin(pluginDto);
        pluginDao.save(p);
        Plugin found = pluginDao.findByNameAndVersion(NAME, VERSION);
        Assert.assertEquals(pluginDto, found.toDto());
    }

    @Test
    public void testFindPluginByClassName() {
        PluginDto pluginDto = generatePlugin();
        Plugin p = new Plugin(pluginDto);
        p = pluginDao.save(p);
        Plugin found = pluginDao.findByClassName(CLASS_NAME);
        Assert.assertEquals(pluginDto, found.toDto());
    }

    private PluginDto generatePlugin() {
        PluginDto pluginDto = new PluginDto();
        pluginDto.setClassName(CLASS_NAME);
        pluginDto.setName(NAME);
        pluginDto.setVersion(VERSION);
        pluginDto.setScope(PluginScope.ENDPOINT);
        pluginDto.setConfSchema("{Schema}");
        pluginDto.setPluginContracts(generateContracts());
        pluginDto.setPluginInstances(generatePluginInstances());
        return pluginDto;
    }

    private Set<PluginContractDto> generateContracts() {
        Set<PluginContractDto> contracts = new HashSet<>();
        PluginContractDto pluginContractDto = new PluginContractDto();
        pluginContractDto.setPluginContractItems(generatePluginContractItems());
        pluginContractDto.setDirection(PluginContractDirection.IN);
        contracts.add(pluginContractDto);
        return contracts;
    }

    private Set<PluginContractItemDto> generatePluginContractItems() {
        Set<PluginContractItemDto> pluginContracts = new HashSet<>();
        PluginContractItemDto pluginContractItemDto = new PluginContractItemDto();
        pluginContractItemDto.setConfigSchema("{ConfigurationSchema}");
        pluginContractItemDto.setContractItem(generateContractItem());
        pluginContracts.add(pluginContractItemDto);
        return pluginContracts;
    }

    private ContractItemDto generateContractItem() {
        ContractItemDto contractItemDto = new ContractItemDto();
        contractItemDto.setName("contract item name");
        ContractMessageDto inMessage = new ContractMessageDto();
        inMessage.setVersion(1);
        inMessage.setFqn("a.b.c");
        ContractMessageDto outMessage = new ContractMessageDto();
        outMessage.setVersion(2);
        outMessage.setFqn("c.d.e");
        contractItemDto.setInMessage(inMessage);
        contractItemDto.setOutMessage(inMessage);
        return contractItemDto;
    }

    private Set<PluginInstanceDto> generatePluginInstances() {
        Set<PluginInstanceDto> pluginInstances = new HashSet<>();
        PluginInstanceDto pluginInstance = new PluginInstanceDto();
        pluginInstances.add(pluginInstance);
        return pluginInstances;
    }
}
