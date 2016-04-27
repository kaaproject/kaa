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

package org.kaaproject.kaa.server.operations.service.cache;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.server.operations.service.cache.Computable;
import org.kaaproject.kaa.server.operations.service.cache.concurrent.CacheTemporaryMemorizer;

public class CacheTemporaryMemorizerTest {

    
    @Test
    public void testSingleCancellation(){
        Computable<Integer, Integer> slowCancelable = new Computable<Integer, Integer>() {
            @Override
            public Integer compute(Integer key) {
                throw new CancellationException("I am too slow");
            }
        };
        
        CacheTemporaryMemorizer<Integer, Integer> cache = new CacheTemporaryMemorizer<>();
        try{
            cache.compute(42, slowCancelable);
        }catch(RuntimeException e){
            //ignore
        }
        Assert.assertEquals(0, cache.getCacheSize());
    }
    
    @Test
    public void testParallelCancellation(){      
        final Computable<Integer, Integer> slowCancelable = new Computable<Integer, Integer>() {
            @Override
            public Integer compute(Integer key) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                throw new CancellationException("I am too slow");
            }
        };
        
        final CacheTemporaryMemorizer<Integer, Integer> cache = new CacheTemporaryMemorizer<>();
        ConcurrentCacheServiceTest.launchCodeInParallelThreads(10, new Runnable() {
            
            @Override
            public void run() {
                try{
                    cache.compute(42, slowCancelable);
                }catch(CancellationException e){
                    //ignore
                }
            }
        });

        Assert.assertEquals(0, cache.getCacheSize());
    } 
    
    @Test
    public void testParallelInterruption(){      
        final Computable<Integer, Integer> slowCancelable = new Computable<Integer, Integer>() {
            @Override
            public Integer compute(Integer key) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return key;
            }
        };
        
        final CacheTemporaryMemorizer<Integer, Integer> cache = new CacheTemporaryMemorizer<>();
        Thread executionThread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    launchCodeInParallelThreads(10, new Runnable() {
                        
                        @Override
                        public void run() {
                            try{
                                cache.compute(42, slowCancelable);
                            }catch(CancellationException e){
                                //ignore
                            }
                        }
                    });
                } catch (Exception e) {
                    Assert.assertTrue(e.getMessage().equals("Cache Operation Exception"));
                }
            }
        });

        executionThread.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        executionThread.interrupt();
        try {
            executionThread.join();
        } catch (RuntimeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }    
    
    public static void launchCodeInParallelThreads(final int nThreads, final Runnable task) throws InterruptedException{
        final CountDownLatch startGate = new CountDownLatch(1);
        final CountDownLatch endGate = new CountDownLatch(nThreads);

        for (int i = 0; i < nThreads; i++) {
            Thread t = new Thread() {
                public void run() {
                    try {
                        startGate.await();
                        try {
                            task.run();
                        } finally {
                            endGate.countDown();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            t.start();
        }

        startGate.countDown();
        endGate.await();
    }    
    
}
