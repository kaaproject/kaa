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

package org.kaaproject.kaa.server.admin.client.mvp.activity;

import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.SendNotificationPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.TopicPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.TopicView;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class TopicActivity
        extends
        AbstractDetailsActivity<TopicDto, TopicView, TopicPlace> {

    private String applicationId;

    public TopicActivity(TopicPlace place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
        this.applicationId = place.getApplicationId();
    }

    protected void bind(final EventBus eventBus) {
        super.bind(eventBus);
        if (!create) {
            registrations.add(detailsView.getSendNotificationButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                sendNotification();
            }
          }));
        }
    }

    @Override
    protected String getEntityId(TopicPlace place) {
        return place.getTopicId();
    }

    @Override
    protected TopicView getView(boolean create) {
        if (create) {
            return clientFactory.getCreateTopicView();
        } else {
            return clientFactory.getTopicView();
        }
    }

    @Override
    protected TopicDto newEntity() {
        TopicDto topic = new TopicDto();
        topic.setApplicationId(applicationId);
        return topic;
    }

    @Override
    protected void onEntityRetrieved() {
        detailsView.getName().setValue(entity.getName());
        detailsView.getMandatory().setValue(entity.getType()==TopicTypeDto.MANDATORY);
        detailsView.getDescription().setValue(entity.getDescription());
        detailsView.getCreatedUsername().setValue(entity.getCreatedUsername());
        detailsView.getCreatedDateTime().setValue(Utils.millisecondsToDateTimeString(entity.getCreatedTime()));
    }

    @Override
    protected void onSave() {
        entity.setName(detailsView.getName().getValue());
        entity.setType(detailsView.getMandatory().getValue() ? TopicTypeDto.MANDATORY : TopicTypeDto.OPTIONAL);
        entity.setDescription(detailsView.getDescription().getValue());
    }

    @Override
    protected void getEntity(String id, AsyncCallback<TopicDto> callback) {
        KaaAdmin.getDataSource().getTopic(id, callback);
    }

    @Override
	protected void editEntity(TopicDto entity, AsyncCallback<TopicDto> callback) {
		KaaAdmin.getDataSource().editTopic(entity, callback);
	}

    private void sendNotification() {
        SendNotificationPlace sendNotificationPlace = new SendNotificationPlace(applicationId, entityId);
        sendNotificationPlace.setPreviousPlace(place);
        goTo(sendNotificationPlace);
    }

}
