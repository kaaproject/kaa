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

package org.kaaproject.kaa.client.event;

import org.kaaproject.kaa.client.context.ExecutorContext;
import org.kaaproject.kaa.client.transact.TransactionId;

import javax.annotation.Generated;

/**
 * Factory for accessing supported event families.
 * DO NOT edit it, this class is auto-generated.
 *
 * @author Taras Lemkin
 * @author Andrew Shvayka
 */
@Generated("EventFamilyFactory.java.template")
public class EventFamilyFactory {
  private final EventManager eventManager;
  private final ExecutorContext executorContext;

  public EventFamilyFactory(EventManager eventManager, ExecutorContext executorContext) {
    this.eventManager = eventManager;
    this.executorContext = executorContext;
  }

  public TransactionId startEventsBlock() {
    return eventManager.beginTransaction();
  }

  public void submitEventsBlock(TransactionId trxId) {
    eventManager.commit(trxId);
  }

  public void removeEventsBlock(TransactionId trxId) {
    eventManager.rollback(trxId);
  }
}
