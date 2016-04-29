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

package org.kaaproject.kaa.server.operations.service.delta;

import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;

/**
 * The Class HistoryDelta is used to model history of changes of particular
 * application. It also has configurationChanged property that is used to
 * quickly check if current changes affect particular endpoint configuration.
 * Changes may not affect endpoint configuration in case they are not related to
 * any of endpoint groups
 * 
 * @author ashvayka
 */
public class HistoryDelta {

    /**
     * Instantiates a new history delta.
     * 
     * @param endpointGroupStates
     *            the endpoint group states
     * @param configurationChanged
     *            the configuration changed
     * @param topicListChanged
     *            the topic list changed
     * @param seqNumberChanged
     *            the sequence number changed flag
     */
    public HistoryDelta(List<EndpointGroupStateDto> endpointGroupStates, boolean configurationChanged, boolean topicListChanged, boolean seqNumberChanged) {
        super();
        this.endpointGroupStates = endpointGroupStates;
        this.configurationChanged = configurationChanged;
        this.topicListChanged = topicListChanged;
        this.seqNumberChanged = seqNumberChanged;
    }

    /**
     * Instantiates a new history delta.
     */
    public HistoryDelta() {
        super();
    }

    /** The endpoint group states. */
    private List<EndpointGroupStateDto> endpointGroupStates;

    /** The seq number changed. */
    boolean seqNumberChanged;

    /** The configuration changed. */
    boolean configurationChanged;

    /** The topic list changed. */
    boolean topicListChanged;

    /**
     * Gets the endpoint group states.
     * 
     * @return the endpoint group states
     */
    public List<EndpointGroupStateDto> getEndpointGroupStates() {
        return endpointGroupStates;
    }

    /**
     * Sets the endpoint group states.
     * 
     * @param endpointGroupStates
     *            the new endpoint group states
     */
    public void setEndpointGroupStates(List<EndpointGroupStateDto> endpointGroupStates) {
        this.endpointGroupStates = endpointGroupStates;
    }

    /**
     * Checks if is configuration changed.
     * 
     * @return true, if is configuration changed
     */
    public boolean isConfigurationChanged() {
        return configurationChanged;
    }

    /**
     * Sets the configuration changed.
     * 
     * @param configurationChanged
     *            the new configuration changed
     */
    public void setConfigurationChanged(boolean configurationChanged) {
        this.configurationChanged = configurationChanged;
    }

    /**
     * Checks if is topic list changed.
     * 
     * @return true, if is topic list changed
     */
    public boolean isTopicListChanged() {
        return topicListChanged;
    }

    /**
     * Sets the topic list changed.
     * 
     * @param topicListChanged
     *            the new topic list changed
     */
    public void setTopicListChanged(boolean topicListChanged) {
        this.topicListChanged = topicListChanged;
    }

    /**
     * Checks if is seq number changed.
     * 
     * @return true if seq number changed, false otherwise
     */
    public boolean isSeqNumberChanged() {
        return seqNumberChanged;
    }

    /**
     * Sets the sequence number changed.
     * 
     * @param seqNumberChanged
     *            the sequence number changed
     */
    public void setSeqNumberChanged(boolean seqNumberChanged) {
        this.seqNumberChanged = seqNumberChanged;
    }

    /**
     * Sets the all changed.
     */
    public void setAllChanged() {
        this.configurationChanged = true;
        this.topicListChanged = true;
        this.seqNumberChanged = true;
    }

    /**
     * Checks if smth is changed.
     * 
     * @return true, if smth is changed
     */
    public boolean isSmthChanged() {
        return topicListChanged || configurationChanged || seqNumberChanged;
    }
}
