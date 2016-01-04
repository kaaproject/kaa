package org.kaaproject.kaa.server.control.service.modularization;

import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginDefDtoConverter;
import org.kaaproject.kaa.server.common.core.plugin.def.Plugin;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginDef;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaPlugin;
import org.kaaproject.kaa.server.common.dao.PluginService;
import org.kaaproject.kaa.server.control.service.exception.KaaPluginLoadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class KaaPluginLoadServiceImpl implements KaaPluginLoadService {

    private static final Logger LOG = LoggerFactory.getLogger(KaaPluginLoadServiceImpl.class);

    @Value("#{properties[kaa_plugins_location]}")
    private String pathStr;

    @Autowired
    private PluginService pluginService;

    @Override
    public void load() throws KaaPluginLoadException {
        if (pathStr == null) {
            throw new RuntimeException("Plugin path is null, can't continue");
        }
        LOG.debug("Loading kaa plugin definitions from: [{]}", pathStr);
        List<Class<? extends KaaPlugin>> kaaPluginClasses = scan();
        Set<PluginDto> pluginDefinitions = new HashSet<>();
        for (Class<? extends KaaPlugin> clazz : kaaPluginClasses) {
            Plugin plugin = clazz.getAnnotation(Plugin.class);
            Class<? extends PluginDef> pluginDefClass = plugin.value();
            try {
                PluginDef pluginDef = pluginDefClass.newInstance();
                PluginDto pluginDto = BasePluginDefDtoConverter.convertBasePluginDef(pluginDef, pluginDefClass.getName());
                pluginDefinitions.add(pluginDto);
            } catch (InstantiationException | IllegalAccessException e) {
                LOG.warn("Unable to instantiate plugin definition:", e);
            }
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("Created plugin instances: {}", pluginDefinitions);
        } else {
            LOG.info("Created {} plugin instances", pluginDefinitions.size());
        }

        List<PluginDto> savedPlugins = pluginService.findAllPlugins();

        for (PluginDto savedPlugin : savedPlugins) {
            if (!pluginDefinitions.contains(savedPlugin)) {
                throw new KaaPluginLoadException("Plugin " + savedPlugin + " isn't present in the filesystem by path: " + pathStr);
            }
        }

        for (PluginDto pluginDto : pluginDefinitions) {
            PluginDto found = pluginService.findPluginByClassName(pluginDto.getClassName());
            if (found == null) {
                PluginDto registeredPluginDto = pluginService.registerPlugin(pluginDto);
                saveHardCodedInstances(registeredPluginDto);
            }
        }
    }

    private void saveHardCodedInstances(PluginDto pluginDto) {
        PluginInstanceDto pluginInstanceDto =
                HarcodedInstanceFactory.create(HarcodedInstanceFactory.Type.MESSAGING, pluginDto);
        pluginService.saveInstance(pluginInstanceDto);
    }

    private List<Class<? extends KaaPlugin>> scan() throws KaaPluginLoadException {
        Path path = Paths.get(pathStr);
        KaaPluginScanner kaaPluginScanner = new KaaPluginScanner();
        try {
            Files.walkFileTree(path.toAbsolutePath(), kaaPluginScanner);
        } catch (IOException e) {
            LOG.error("Unable to scan plugin jar files", e);
            throw new KaaPluginLoadException(e);
        }
        List<Class<? extends KaaPlugin>> scannedKaaPlugins = kaaPluginScanner.getScannedKaaPlugins();
        LOG.info("Discovered {} plugin(s): {}", scannedKaaPlugins.size(), scannedKaaPlugins);
        return scannedKaaPlugins;
    }
}
