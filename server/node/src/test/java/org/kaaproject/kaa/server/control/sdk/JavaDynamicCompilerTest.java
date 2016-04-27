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

package org.kaaproject.kaa.server.control.sdk;

import org.junit.Test;
import org.kaaproject.kaa.server.control.service.sdk.compiler.JavaDynamicCompiler;
import org.kaaproject.kaa.server.control.service.sdk.compiler.JavaDynamicException;
import org.mockito.Mockito;

/**
 * The Class JavaDynamicCompilerTest.
 */
public class JavaDynamicCompilerTest {

    /**
     * Test compiler not found.
     */
    @Test(expected = JavaDynamicException.class)
    public void testCompilerNotFound() {
        JavaDynamicCompiler compiler = new JavaDynamicCompiler();
        JavaDynamicCompiler compilerStubbed = Mockito.spy(compiler);
        Mockito.doReturn(null).when(compilerStubbed).getCompiler();
        compilerStubbed.init();
    }
 
}
