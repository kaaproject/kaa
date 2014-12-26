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
package org.kaaproject.kaa.server.operations.pojo.sync;

public class RedirectServerSync {
    private String dnsName;

    public RedirectServerSync() {
    }

    /**
     * All-args constructor.
     */
    public RedirectServerSync(String dnsName) {
        this.dnsName = dnsName;
    }

    /**
     * Gets the value of the 'dnsName' field.
     */
    public String getDnsName() {
        return dnsName;
    }

    /**
     * Sets the value of the 'dnsName' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setDnsName(String value) {
        this.dnsName = value;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RedirectServerSync [dnsName=");
        builder.append(dnsName);
        builder.append("]");
        return builder.toString();
    }

}
