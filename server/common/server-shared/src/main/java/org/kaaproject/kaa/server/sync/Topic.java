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

package org.kaaproject.kaa.server.sync;

public final class Topic {
    private String id;
    private String name;
    private SubscriptionType subscriptionType;

    public Topic() {
    }

    /**
     * All-args constructor.
     */
    public Topic(String id, String name, SubscriptionType subscriptionType) {
        this.id = id;
        this.name = name;
        this.subscriptionType = subscriptionType;
    }

    /**
     * Gets the value of the 'id' field.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the value of the 'id' field.
     */
    public long getIdAsLong() {
        return Long.parseLong(id);
    }

    /**
     * Sets the value of the 'id' field.
     *
     * @param value
     *            the value to set.
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Sets the value of the 'id' field.
     *
     * @param value
     *            the value to set.
     */
    public void setId(Long value) {
        this.id = Long.toString(value);
    }

    /**
     * Gets the value of the 'name' field.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the 'name' field.
     *
     * @param value
     *            the value to set.
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the 'subscriptionType' field.
     */
    public SubscriptionType getSubscriptionType() {
        return subscriptionType;
    }

    /**
     * Sets the value of the 'subscriptionType' field.
     *
     * @param value
     *            the value to set.
     */
    public void setSubscriptionType(SubscriptionType value) {
        this.subscriptionType = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Topic topic = (Topic) o;

        if (id != null ? !id.equals(topic.id) : topic.id != null) {
            return false;
        }
        if (name != null ? !name.equals(topic.name) : topic.name != null) {
            return false;
        }
        if (subscriptionType != topic.subscriptionType) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (subscriptionType != null ? subscriptionType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Topic [id=");
        builder.append(id);
        builder.append(", name=");
        builder.append(name);
        builder.append(", subscriptionType=");
        builder.append(subscriptionType);
        builder.append("]");
        return builder.toString();
    }

}
