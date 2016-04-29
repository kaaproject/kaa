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

package org.kaaproject.kaa.server.appenders.file.appender;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

public class FileSystemLogEventServiceImplTest {

    private static final String TEST_FILE = "/test";
    private static final String TEST_TEXT = "test text";
    private static final String USER_HOME = "user.home";

    private FileSystemLogEventService fileSystemLogEventService = new FileSystemLogEventServiceImpl();

    @Test
    public void createDirectoryAndRemoveAllTest() throws FileNotFoundException {
        if (System.getProperty(USER_HOME) != null && new File(System.getProperty(USER_HOME)).exists()) {
            String tempDir = System.getProperty(USER_HOME) + "/temp_dir_" + System.currentTimeMillis();
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
        if (System.getProperty(USER_HOME) != null && new File(System.getProperty(USER_HOME)).exists()) {
            FileSystemLogEventService logEventService = new FileSystemLogEventServiceImpl();

            String tempDir = System.getProperty(USER_HOME) + "/temp_dir_" + System.currentTimeMillis();
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

    @Test(expected = RuntimeException.class)
    public void executeCommandFailureTest() throws Throwable {
        String testDir = "testdir";
        File targetTestDir = new File("target", testDir);
        if (!targetTestDir.exists()) {
            targetTestDir.mkdirs();
        }
        FileSystemLogEventServiceImpl service = new FileSystemLogEventServiceImpl();
        Method executeCommand = FileSystemLogEventServiceImpl.class.getDeclaredMethod("executeCommand", File.class, String[].class);
        executeCommand.setAccessible(true);
        try {
            executeCommand.invoke(service, "target", new String[]{"mkdir", testDir});
        } catch (InvocationTargetException e){
            throw e.getCause();
        }
    }
}
