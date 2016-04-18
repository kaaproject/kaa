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

package org.kaaproject.kaa.server.admin.client.mvp.view.dialog;

import java.util.LinkedList;
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.widget.AlertPanel;
import org.kaaproject.avro.ui.gwt.client.widget.dialog.AvroUiDialog;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.TopicListBox;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AddTopicDialog extends AvroUiDialog implements ValueChangeHandler<List<TopicDto>>, HasErrorMessage {

    private AlertPanel errorPanel;

    private TopicListBox topic;

    private String endpointGroupId;

    private Button addButton;

    public static void showAddTopicDialog(final String endpointGroupId, final AsyncCallback<AddTopicDialog> callback) {
        KaaAdmin.getDataSource().loadVacantTopicsByEndpointGroupId(endpointGroupId, new AsyncCallback<List<TopicDto>>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(List<TopicDto> result) {
                AddTopicDialog dialog = new AddTopicDialog(endpointGroupId, result);
                dialog.center();
                callback.onSuccess(dialog);
                dialog.show();
            }
        });
    }

    public AddTopicDialog(String endpointGroupId, List<TopicDto> topics) {
        super(false, true);

        this.endpointGroupId = endpointGroupId;

        setWidth("500px");

        setTitle(Utils.constants.addTopicToEp());

        VerticalPanel dialogContents = new VerticalPanel();
        dialogContents.setSpacing(4);
        setWidget(dialogContents);

        errorPanel = new AlertPanel(AlertPanel.Type.ERROR);
        errorPanel.setVisible(false);
        dialogContents.add(errorPanel);

        FlexTable table  = new FlexTable();
        table.setCellSpacing(6);

        int row=0;

        Widget label = new Label(Utils.constants.selectNotificationTopics());
        label.addStyleName(Utils.avroUiStyle.requiredField());
        topic = new TopicListBox();
        topic.setWidth("200px");
        topic.setAcceptableValues(topics);
        topic.addValueChangeHandler(this);

        table.setWidget(row, 0, label);
        table.setWidget(row, 1, topic);
        table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);

        dialogContents.add(table);

        addButton = new Button(Utils.constants.add(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                performAdd();
            }
        });

        Button closeButton = new Button(Utils.constants.close(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        addButton(addButton);
        addButton(closeButton);

        addButton.setEnabled(false);
    }

    @Override
    public void onValueChange(ValueChangeEvent<List<TopicDto>> event) {
        boolean valid = validate();
        addButton.setEnabled(valid);
    }

    private void performAdd () {
          LinkedList<String> topicIds = new LinkedList<>();

          for (TopicDto topicDto : topic.getValue()) {
              topicIds.add(topicDto.getId());
          }
          addTopics(topicIds);
    }

    private void addTopics(final LinkedList<String> topicIds) {
        if (!topicIds.isEmpty()) {
            String topicId = topicIds.removeLast();
            KaaAdmin.getDataSource().addTopicToEndpointGroup(endpointGroupId, topicId,
                    new AsyncCallback<Void>() {
                      @Override
                      public void onFailure(Throwable caught) {
                          Utils.handleException(caught, AddTopicDialog.this);
                      }

                      @Override
                      public void onSuccess(Void result) {
                          clearError();
                          addTopics(topicIds);
                      }
            });
        } else {
            hide();
        }
    }

    private boolean validate() {
        return topic.getValue() != null && !topic.getValue().isEmpty();
    }

    @Override
    public void clearError() {
        errorPanel.setMessage("");
        errorPanel.setVisible(false);
    }

    @Override
    public void setErrorMessage(String message) {
        errorPanel.setMessage(message);
        errorPanel.setVisible(true);
    }

}
