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

package org.kaaproject.kaa.server.admin.client.mvp.event.data;

import com.google.gwt.event.shared.GwtEvent;

public class DataEvent extends GwtEvent<DataEventHandler>{
  private static Type<DataEventHandler> TYPE;

  private final Class<?> clazz;
  private boolean refreshTree = false;

  public DataEvent(Class<?> clazz) {
      this.clazz = clazz;
  }

  public DataEvent(Class<?> clazz, boolean refreshTree) {
      this.clazz = clazz;
      this.refreshTree = refreshTree;
  }

  public Class<?> getClazz() {
      return clazz;
  }

  public boolean refreshTree() {
      return refreshTree;
  }

  public boolean checkClass(Class<?> clazz) {
      return this.clazz.equals(clazz);
  }

  public static Type<DataEventHandler> getType() {
      if (TYPE == null) {
          TYPE = new Type<DataEventHandler>();
      }
    return TYPE;
  }

  @Override
  public Type<DataEventHandler> getAssociatedType() {
      return TYPE;
  }

  @Override
  protected void dispatch(DataEventHandler handler) {
    handler.onDataChanged(this);
  }

}

