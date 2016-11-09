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

package org.kaaproject.kaa.server.operations.service.cache.concurrent;

import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.kaaproject.kaa.server.operations.service.cache.Computable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;


/**
 * The Class CacheTemporaryMemorizer.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class CacheTemporaryMemorizer<K, V> {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CacheTemporaryMemorizer.class);

  /**
   * The cache.
   */
  private final ConcurrentMap<K, Future<V>> cache = new ConcurrentHashMap<K, Future<V>>();

  /**
   * If the Throwable is an Error, throw it; if it is a RuntimeException
   * return it, otherwise throw IllegalStateException.
   *
   * @param throwable the t
   * @return the runtime exception
   */
  public static RuntimeException launderThrowable(Throwable throwable) {
    if (throwable instanceof RuntimeException) {
      return (RuntimeException) throwable;
    } else if (throwable instanceof Error) {
      throw (Error) throwable;
    } else {
      throw new IllegalStateException("Cache Operation Exception", throwable);
    }
  }

  /**
   * Compute.
   *
   * @param key    the key
   * @param worker the worker
   * @return the v
   */
  public V compute(final K key, final Computable<K, V> worker) {
    if (key == null) {
      throw new InvalidParameterException("Cache key can't be null");
    }
    while (true) {
      Future<V> future = cache.get(key);
      if (future == null) {
        Callable<V> eval = new Callable<V>() {
          public V call() throws InterruptedException {
            return worker.compute(key);
          }
        };
        FutureTask<V> ft = new FutureTask<V>(eval);
        future = cache.putIfAbsent(key, ft);
        if (future == null) {
          future = ft;
          try {
            ft.run();
            //the idea is not to cache permanently but only for the time of execution.
            //thus, technically, if time of calculation >> time of external
            // cache put -> we will run calculation maximum 2 times.
          } catch (Throwable ex) {
            LOG.error("Exception catched: ", ex);
            throw ex;
          } finally {
            cache.remove(key, ft);
          }
        }
      }
      try {
        return future.get();
      } catch (CancellationException ex) {
        LOG.error("Exception catched: ", ex);
        cache.remove(key, future);
      } catch (ExecutionException | InterruptedException ex) {
        LOG.error("Exception catched: ", ex);
        throw launderThrowable(ex);
      }
    }
  }

  /**
   * Gets the cache size.
   *
   * @return the cache size
   */
  public int getCacheSize() {
    return cache.size();
  }

}
