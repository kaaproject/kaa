package org.kaaproject.kaa.server.common.core.plugin;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.plugin.ContractType;
import org.kaaproject.kaa.common.dto.plugin.PluginContractDirection;
import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.common.dto.plugin.PluginScope;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractItemDef;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginDef;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginDefDtoConverter;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractItemDef;
import org.kaaproject.kaa.server.common.dao.model.sql.plugin.Plugin;

import java.io.Serializable;

public class BasePluginDefDtoConverterTest {

    private static final String NAME = "name";
    private static final int VERSION = 1;
    private static final String SCHEMA = "{Schema}";
    private static final String TYPE = "type";
    private static final String CLASS_NAME = "ClassName";

    @Test
    public void testConvertBasePluginDef() {
        PluginDto pluginDto = BasePluginDefDtoConverter.convertBasePluginDef(createBasePluginDef(), CLASS_NAME);
        Assert.assertNotNull(pluginDto);
        Plugin plugin = new Plugin(pluginDto);
        Assert.assertNotNull(plugin);
    }

    private BasePluginDef createBasePluginDef() {
        return BasePluginDef
                .builder(NAME, VERSION)
                .withSchema(SCHEMA)
                .withScope(PluginScope.ENDPOINT)
                .withContract(createPluginContractDef("Contract 1"))
                .withContract(createPluginContractDef("Contract 2"))
                .withType(TYPE)
                .build();
    }

    private PluginContractDef createPluginContractDef(String name) {
        return BasePluginContractDef
                .builder(name, VERSION)
                .withType(ContractType.SDK)
                .withItem(createPluginContractItemDef(String.class))
                .withItem(createPluginContractItemDef(Integer.class))
                .withDirection(PluginContractDirection.IN)
                .build();
    }

    private PluginContractItemDef createPluginContractItemDef(Class<? extends Serializable> clazz) {
        return BasePluginContractItemDef
                .builder(NAME)
                .withInMessage(clazz)
                .withOutMessage(clazz)
                .withSchema(SCHEMA)
                .build();
    }
}
