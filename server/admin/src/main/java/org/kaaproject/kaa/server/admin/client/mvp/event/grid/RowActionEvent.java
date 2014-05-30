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

package org.kaaproject.kaa.server.admin.client.mvp.event.grid;

import com.google.gwt.event.shared.GwtEvent;

public class RowActionEvent<K> extends GwtEvent<RowActionEventHandler<K>>{

  private static Type<RowActionEventHandler<?>> TYPE;

  private final K clickedId;
  private final RowAction action;

  public RowActionEvent(K clickedId, RowAction action) {
    this.clickedId = clickedId;
    this.action = action;
  }

  public K getClickedId() {
      return clickedId;
  }

  public RowAction getAction() {
      return action;
  }

  public static Type<RowActionEventHandler<?>> getType() {
      if (TYPE == null) {
          TYPE = new Type<RowActionEventHandler<?>>();
      }
    return TYPE;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public Type<RowActionEventHandler<K>> getAssociatedType() {
      return (Type) TYPE;
  }

  @Override
  protected void dispatch(RowActionEventHandler<K> handler) {
    handler.onRowAction(this);
  }
}

