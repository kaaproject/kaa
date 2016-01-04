package org.kaaproject.kaa.server.control.service.modularization;

import org.kaaproject.kaa.server.common.core.plugin.def.Plugin;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class KaaPluginScanner extends SimpleFileVisitor<Path> {

    private static final Logger LOG = LoggerFactory.getLogger(KaaPluginScanner.class);

    private List<Class<? extends KaaPlugin>> classes;

    public KaaPluginScanner() {
        classes = new ArrayList<>();
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
        URL url;
        try {
            url = file.toUri().toURL();
        } catch (MalformedURLException e) {
            LOG.error("Malformed url for: {}", file, e);
            return FileVisitResult.CONTINUE;
        }
        try (URLClassLoader classLoader = new URLClassLoader(new URL[] {url})) {
            if (attr.isRegularFile() && file.toString().toLowerCase().endsWith(".jar")) {
                LOG.debug("Scanning jar: {}", file.toAbsolutePath());
                try {
                    JarFile jarFile = new JarFile(file.toString());
                    Enumeration<JarEntry> e = jarFile.entries();
                    while (e.hasMoreElements()) {
                        JarEntry entry = e.nextElement();
                        addIfKaaPlugin(classLoader, entry.getName());
                    }
                } catch (IOException e) {
                    LOG.warn("Unable to read jar file: {}", file, e);
                }
            }
        } catch (IOException e) {
            LOG.warn("Unable to close URLClassLoader", e);
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException e) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException e) {
        LOG.warn("Unable to visit file: {}", file, e);
        return FileVisitResult.CONTINUE;
    }

    public List<Class<? extends KaaPlugin>> getScannedKaaPlugins() {
        return classes;
    }

    @SuppressWarnings("unchecked")
    private void addIfKaaPlugin(ClassLoader classLoader, String jarItemName) {
        String normalizedItemName = jarItemName.toLowerCase();
        if (!normalizedItemName.endsWith(".class") || normalizedItemName.contains("$")) {
            return;
        }

        // .class length = 6
        jarItemName = jarItemName.substring(0, jarItemName.length() - 6);
        jarItemName = jarItemName.replaceAll("/", ".");
        jarItemName = jarItemName.replaceAll("\\\\", ".");

        try {
            Class<?> clazz = classLoader.loadClass(jarItemName);
            if (isValidKaaPlugin(clazz)) {
                classes.add((Class<KaaPlugin>) clazz);
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            LOG.warn("Unable to scan class: {}", jarItemName, e);
        }
    }

    private boolean isValidKaaPlugin(Class<?> clazz) {
        if (!KaaPlugin.class.isAssignableFrom(clazz)) {
            return false;
        }
        Plugin pluginAnnotation = clazz.getAnnotation(Plugin.class);
        if (pluginAnnotation == null) {
            LOG.warn("Plugin: {} has no @Plugin annotation");
            return false;
        }
        return true;
    }
}
