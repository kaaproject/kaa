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

package org.kaaproject.kaa.server.admin.client.mvp.activity;

import org.kaaproject.avro.ui.gwt.client.util.BusyAsyncCallback;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEventHandler;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.StructureRecordDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.admin.StructureRecordKey;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.data.ConfigurationsDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.data.ProfileFiltersDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.data.TopicsDataProvider;
import org.kaaproject.kaa.server.admin.client.mvp.event.data.DataEvent;
import org.kaaproject.kaa.server.admin.client.mvp.event.data.DataEventHandler;
import org.kaaproject.kaa.server.admin.client.mvp.place.ConfigurationPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointGroupPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.ProfileFilterPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointGroupView;
import org.kaaproject.kaa.server.admin.client.mvp.view.dialog.AddTopicDialog;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class EndpointGroupActivity
        extends
        AbstractDetailsActivity<EndpointGroupDto, EndpointGroupView, EndpointGroupPlace> {

    private String applicationId;

    private ProfileFiltersDataProvider profileFiltersDataProvider;
    private ConfigurationsDataProvider configurationsDataProvider;
    private TopicsDataProvider topicsDataProvider;

    public EndpointGroupActivity(EndpointGroupPlace place,
            ClientFactory clientFactory) {
        super(place, clientFactory);
        this.applicationId = place.getApplicationId();
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        super.start(containerWidget, eventBus);
        if (!create) {
            AbstractGrid<StructureRecordDto<ProfileFilterDto>, StructureRecordKey> profileFiltersGrid = detailsView.getProfileFiltersGrid();
            profileFiltersDataProvider = new ProfileFiltersDataProvider(profileFiltersGrid.getSelectionModel(),
                    detailsView, entityId);
            profileFiltersDataProvider.setIncludeDeprecated(place.isIncludeDeprecatedProfileFilters());

            profileFiltersDataProvider.addDataDisplay(profileFiltersGrid.getDisplay());

            AbstractGrid<StructureRecordDto<ConfigurationDto>, StructureRecordKey> configurationsGrid = detailsView.getConfigurationsGrid();
            configurationsDataProvider = new ConfigurationsDataProvider(configurationsGrid.getSelectionModel(),
                    detailsView, entityId);
            configurationsDataProvider.setIncludeDeprecated(place.isIncludeDeprecatedConfigurations());

            configurationsDataProvider.addDataDisplay(configurationsGrid.getDisplay());

            AbstractGrid<TopicDto, String> topicsGrid = detailsView.getTopicsGrid();
            topicsDataProvider = new TopicsDataProvider(topicsGrid.getSelectionModel(),
                    detailsView, null, entityId);
            topicsDataProvider.addDataDisplay(topicsGrid.getDisplay());
        }
    }

    protected void bind(final EventBus eventBus) {
        super.bind(eventBus);

        registrations.add(detailsView.getAddProfileFilterButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                ProfileFilterPlace profileFilterPlace = new ProfileFilterPlace(applicationId, "", entityId, true, false, 0);
                profileFilterPlace.setPreviousPlace(place);
                goTo(profileFilterPlace);
            }
          }));

        registrations.add(detailsView.getProfileFiltersGrid().addRowActionHandler(new RowActionEventHandler<StructureRecordKey>() {
              @Override
              public void onRowAction(RowActionEvent<StructureRecordKey> event) {
                  StructureRecordKey id = event.getClickedId();
                  if (event.getAction()==RowActionEvent.CLICK) {
                      ProfileFilterPlace profileFilterPlace = new ProfileFilterPlace(applicationId, id.getSchemaId(), id.getEndpointGroupId(), false, true, 0);
                      profileFilterPlace.setPreviousPlace(place);
                      goTo(profileFilterPlace);
                  }
                  else if (event.getAction()==RowActionEvent.DELETE) {
                        KaaAdmin.getDataSource().deleteProfileFilterRecord(id.getSchemaId(), id.getEndpointGroupId(),
                                new BusyAsyncCallback<Void>() {
                                      @Override
                                      public void onFailureImpl(Throwable caught) {
                                          Utils.handleException(caught, detailsView);
                                      }

                                      @Override
                                      public void onSuccessImpl(Void result) {}
                                });
                  }
              }
          }));

        detailsView.getIncludeDeprecatedProfileFilters().setValue(place.isIncludeDeprecatedProfileFilters());

        registrations.add(detailsView.getIncludeDeprecatedProfileFilters().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                place.setIncludeDeprecatedProfileFilters(detailsView.getIncludeDeprecatedProfileFilters().getValue());
                profileFiltersDataProvider.setIncludeDeprecated(detailsView.getIncludeDeprecatedProfileFilters().getValue());
                profileFiltersDataProvider.reload(detailsView.getProfileFiltersGrid().getDisplay());
            }
        }));

        registrations.add(detailsView.getAddConfigurationButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                ConfigurationPlace configurationPlace = new ConfigurationPlace(applicationId, "", entityId, true, false, 0);
                configurationPlace.setPreviousPlace(place);
                goTo(configurationPlace);
            }
          }));

        registrations.add(detailsView.getConfigurationsGrid().addRowActionHandler(new RowActionEventHandler<StructureRecordKey>() {
              @Override
              public void onRowAction(RowActionEvent<StructureRecordKey> event) {
                  StructureRecordKey id = event.getClickedId();
                  if (event.getAction()==RowActionEvent.CLICK) {
                      ConfigurationPlace configurationPlace = new ConfigurationPlace(applicationId, id.getSchemaId(), id.getEndpointGroupId(), false, true, 0);
                      configurationPlace.setPreviousPlace(place);
                      goTo(configurationPlace);
                  }
                  else if (event.getAction()==RowActionEvent.DELETE) {
                        KaaAdmin.getDataSource().deleteConfigurationRecord(id.getSchemaId(), id.getEndpointGroupId(),
                                new BusyAsyncCallback<Void>() {
                                      @Override
                                      public void onFailureImpl(Throwable caught) {
                                          Utils.handleException(caught, detailsView);
                                      }

                                      @Override
                                      public void onSuccessImpl(Void result) {}
                                });
                  }
              }
          }));

        detailsView.getIncludeDeprecatedConfigurations().setValue(place.isIncludeDeprecatedConfigurations());

        registrations.add(detailsView.getIncludeDeprecatedConfigurations().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
             @Override
             public void onValueChange(ValueChangeEvent<Boolean> event) {
                 place.setIncludeDeprecatedConfigurations(detailsView.getIncludeDeprecatedConfigurations().getValue());
                 configurationsDataProvider.setIncludeDeprecated(detailsView.getIncludeDeprecatedConfigurations().getValue());
                 configurationsDataProvider.reload(detailsView.getConfigurationsGrid().getDisplay());
             }
         }));

         registrations.add(detailsView.getAddTopicButton().addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 addTopic();
             }
           }));

         registrations.add(detailsView.getTopicsGrid().addRowActionHandler(new RowActionEventHandler<String>() {
               @Override
               public void onRowAction(RowActionEvent<String> event) {
                   String id = event.getClickedId();
                   if (event.getAction()==RowActionEvent.CLICK) {
                       //do nothing
                   }
                   else if (event.getAction()==RowActionEvent.DELETE) {
                         KaaAdmin.getDataSource().removeTopicFromEndpointGroup(entityId, id,
                                 new BusyAsyncCallback<Void>() {
                                       @Override
                                       public void onFailureImpl(Throwable caught) {
                                           Utils.handleException(caught, detailsView);
                                       }

                                       @Override
                                       public void onSuccessImpl(Void result) {}
                                 });
                   }
               }
           }));

          registrations.add(eventBus.addHandler(DataEvent.getType(), new DataEventHandler() {
             @Override
             public void onDataChanged(DataEvent event) {
                 if (detailsView != null) {
                     if (event.checkClass(ProfileFilterDto.class) && profileFiltersDataProvider != null) {
                         profileFiltersDataProvider.reload(detailsView.getProfileFiltersGrid().getDisplay());
                     }
                     else if (event.checkClass(ConfigurationDto.class) && configurationsDataProvider != null) {
                         configurationsDataProvider.reload(detailsView.getConfigurationsGrid().getDisplay());
                     }
                     else if (event.checkClass(TopicDto.class) && topicsDataProvider != null) {
                         topicsDataProvider.reload(detailsView.getTopicsGrid().getDisplay());
                     }
                 }
             }
           }));
    }

    @Override
    protected String getEntityId(EndpointGroupPlace place) {
        return place.getEndpointGroupId();
    }

    @Override
    protected EndpointGroupView getView(boolean create) {
        if (create) {
            return clientFactory.getCreateEndpointGroupView();
        } else {
            return clientFactory.getEndpointGroupView();
        }
    }

    @Override
    protected EndpointGroupDto newEntity() {
        EndpointGroupDto endpointGroup = new EndpointGroupDto();
        endpointGroup.setApplicationId(applicationId);
        return endpointGroup;
    }

    @Override
    protected void onEntityRetrieved() {
        detailsView.getName().setValue(entity.getName());
        if (!create) {
            detailsView.getWeight().setValue(entity.getWeight());
        }
        detailsView.getDescription().setValue(entity.getDescription());
        detailsView.getCreatedUsername().setValue(entity.getCreatedUsername());
        detailsView.getCreatedDateTime().setValue(Utils.millisecondsToDateTimeString(entity.getCreatedTime()));
        detailsView.getEndpointCount().setValue(entity.getEndpointCount()+"");
        if (entity.getWeight()==0 && !create) {
            detailsView.setReadOnly();
        }
    }

    @Override
    protected void onSave() {
        entity.setName(detailsView.getName().getValue());
        entity.setWeight(detailsView.getWeight().getValue());
        entity.setDescription(detailsView.getDescription().getValue());
    }

    @Override
    protected void doSave(final EventBus eventBus) {
        onSave();

        editEntity(entity,
            new BusyAsyncCallback<EndpointGroupDto>() {
                public void onSuccessImpl(EndpointGroupDto result) {
                    if (create) {
                        goTo(new EndpointGroupPlace(applicationId, result.getId(), false, false));
                    }
                    else {
                        goTo(place.getPreviousPlace());
                    }
                }

                public void onFailureImpl(Throwable caught) {
                    Utils.handleException(caught, detailsView);
                }
            });
    }

    @Override
    protected void getEntity(String id, AsyncCallback<EndpointGroupDto> callback) {
        KaaAdmin.getDataSource().getEndpointGroup(id, callback);
    }

    @Override
    protected void editEntity(EndpointGroupDto entity,
            AsyncCallback<EndpointGroupDto> callback) {
        KaaAdmin.getDataSource().editEndpointGroup(entity, callback);
    }

    private void addTopic() {
        AddTopicDialog.showAddTopicDialog(entityId,
                new BusyAsyncCallback<AddTopicDialog>() {
                    @Override
                    public void onFailureImpl(Throwable caught) {
                        Utils.handleException(caught, detailsView);
                    }

                    @Override
                    public void onSuccessImpl(AddTopicDialog result) {}
        });
    }

}
