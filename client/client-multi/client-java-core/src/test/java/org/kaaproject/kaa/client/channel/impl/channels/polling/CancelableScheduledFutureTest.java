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

package org.kaaproject.kaa.client.channel.impl.channels.polling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.mockito.Mockito;

public class CancelableScheduledFutureTest {

    @Test
    public void test() throws InterruptedException, ExecutionException, TimeoutException {
        CancelableRunnable cancelableRunnable = Mockito.mock(CancelableRunnable.class);
        RunnableScheduledFuture futureTask = Mockito.mock(RunnableScheduledFuture.class);

        CancelableScheduledFuture<Object> future = new CancelableScheduledFuture<Object>(cancelableRunnable, futureTask);
        future.cancel(false);
        Mockito.verify(futureTask, Mockito.times(1)).cancel(false);

        future.run();
        Mockito.verify(futureTask, Mockito.times(1)).run();

        future.cancel(true);
        Mockito.verify(cancelableRunnable, Mockito.times(1)).cancel();
        Mockito.verify(futureTask, Mockito.times(1)).cancel(true);

        future.isDone();
        Mockito.verify(futureTask, Mockito.times(1)).isDone();

        future.isPeriodic();
        Mockito.verify(futureTask, Mockito.times(1)).isPeriodic();

        future.get();
        Mockito.verify(futureTask, Mockito.times(1)).get();

        future.get(100L, TimeUnit.MICROSECONDS);
        Mockito.verify(futureTask, Mockito.times(1)).get(100L, TimeUnit.MICROSECONDS);

        future.getDelay(TimeUnit.HOURS);
        Mockito.verify(futureTask, Mockito.times(1)).getDelay(TimeUnit.HOURS);

        CancelableScheduledFuture<Object> future2 = new CancelableScheduledFuture<Object>(cancelableRunnable, futureTask);

        assertTrue(future.equals(future));
        assertTrue(future.equals(future2));

        assertEquals(future.hashCode(), future.hashCode());
        assertEquals(future.hashCode(), future2.hashCode());

    }

}
