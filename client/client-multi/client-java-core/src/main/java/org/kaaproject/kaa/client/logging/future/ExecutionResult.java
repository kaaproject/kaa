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

package org.kaaproject.kaa.client.logging.future;

public class ExecutionResult<T> {

  private final T value;
  private final Exception exception;

  public ExecutionResult(T value, Exception exception) {
    super();
    this.value = value;
    this.exception = exception;
  }

  public T get() {
    return value;
  }

  public Exception getE() {
    return exception;
  }

}
