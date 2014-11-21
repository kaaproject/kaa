/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.server.appenders.file.appender;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSystemLogEventServiceImplTest {
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemLogEventServiceImplTest.class);

    private static final String TEST_FILE = "/test";
    private static final String TEST_TEXT = "test text";

    private FileSystemLogEventService fileSystemLogEventService = new FileSystemLogEventServiceImpl();

    @Test
    public void createDirectoryAndRemoveAllTest() throws FileNotFoundException {
        if (System.getProperty("user.home") != null && new File(System.getProperty("user.home")).exists()) {
            String tempDir = System.getProperty("user.home") + "/temp_dir_" + System.currentTimeMillis();
            File file = new File(tempDir);

            Assert.assertFalse(file.exists());

            fileSystemLogEventService.createDirectory(tempDir);

            Assert.assertTrue(file.exists());

            PrintWriter writer = new PrintWriter(new File(tempDir + TEST_FILE));
            writer.write(TEST_TEXT);
            writer.close();

            fileSystemLogEventService.removeAll(tempDir);

            Assert.assertFalse(file.exists());
        }
    }

    @Test
    public void createDirectoryAlreadyExistsTest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        if (System.getProperty("user.home") != null && new File(System.getProperty("user.home")).exists()) {
            FileSystemLogEventService logEventService = new FileSystemLogEventServiceImpl();

            String tempDir = System.getProperty("user.home") + "/temp_dir_" + System.currentTimeMillis();
            File file = new File(tempDir);

            logEventService.createDirectory(tempDir);

            Logger testLogger = Mockito.mock(Logger.class);

            Field field = logEventService.getClass().getDeclaredField("LOG");

            field.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.set(null, testLogger);

            Assert.assertTrue(file.exists());

            logEventService.createDirectory(tempDir);

            Mockito.verify(testLogger, Mockito.atLeast(2)).debug(Mockito.anyString(), Mockito.eq(tempDir));

            logEventService.removeAll(tempDir);
        }
    }
}