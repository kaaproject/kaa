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

package org.kaaproject.kaa.client.transact;

import java.util.UUID;

/**
 * Class representing unique transaction id for transactions initiated using
 * {@link Transactable}.<br>
 */
public final class TransactionId {
  private final String id;

  /**
   * Default constructor. Generates random unique transaction id.
   */
  public TransactionId() {
    this.id = UUID.randomUUID().toString();
  }

  /**
   * Copy constructor. Copies id from other TransactionId object.
   *
   * @param trxId the trx id
   */
  public TransactionId(TransactionId trxId) {
    this.id = trxId.id;
  }

  /**
   * Constructs object with predefined id.
   *
   * @param id the id
   */
  public TransactionId(String id) {
    this.id = id;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    TransactionId other = (TransactionId) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return id;
  }

}
