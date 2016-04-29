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

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.server.control.service.sdk.compiler.JavaDynamicBean;
import org.kaaproject.kaa.server.control.service.sdk.compiler.JavaDynamicCompiler;
import org.kaaproject.kaa.server.control.service.sdk.compiler.JavaDynamicException;
import org.kaaproject.kaa.server.control.service.sdk.compiler.JavaDynamicUtils;

/**
 * The Class JavaDynamicCompilerCompilationTest.
 */
public class JavaDynamicCompilerCompilationTest {

    /**
     * Test compilation failure.
     */
    @Test
    public void testCompilationFailure() {
        JavaDynamicCompiler compiler = new JavaDynamicCompiler();
        compiler.init();
        JavaDynamicBean dummySource = new JavaDynamicBean("dummyClass", "dummyClass {}");
        
        JavaDynamicException expectedException = null;
        try {
            compiler.compile(Arrays.asList(dummySource));
        }
        catch (JavaDynamicException e) {
            expectedException = e;
        }
        Assert.assertNotNull(expectedException);
        Assert.assertFalse(strIsEmpty(expectedException.toString())); 
    }
    
    /**
     * Test compilation success.
     */
    @Test
    public void testCompilationSuccess() {
        JavaDynamicCompiler compiler = new JavaDynamicCompiler();
        compiler.init();
        JavaDynamicBean dummySource = new JavaDynamicBean("dummyClass", "class dummyClass {}");
        
        Collection<JavaDynamicBean> compiledClasses = compiler.compile(Arrays.asList(dummySource));
        Assert.assertEquals(compiledClasses.size(), 1);
        JavaDynamicBean compiledClass = compiledClasses.iterator().next();
        Assert.assertEquals(compiledClass.getName(), "dummyClass");
        Assert.assertNotNull(compiledClass.getBytes());
    }
    
    /**
     * Test invalid uri.
     */
    @Test(expected = RuntimeException.class)
    public void testInvalidUri() {
        JavaDynamicUtils.INSTANCE.createURI("\\test");
    }
    
    /**
     * Str is empty.
     * 
     * @param str
     *            the str
     * @return true, if successful
     */
    private static boolean strIsEmpty(String str) {
        return str == null || str.trim().equals("");
    }
}
