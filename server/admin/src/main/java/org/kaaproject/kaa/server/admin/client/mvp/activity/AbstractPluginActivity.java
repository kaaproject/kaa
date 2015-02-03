
package org.kaaproject.kaa.server.admin.client.mvp.activity;

import java.util.List;

import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInfoDto;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.AbstractPluginPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.BasePluginView;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public abstract class AbstractPluginActivity<T extends PluginDto, V extends BasePluginView, P extends AbstractPluginPlace> extends AbstractDetailsActivity<T, V, P> {

    protected String applicationId;

    public AbstractPluginActivity(P place, ClientFactory clientFactory) {
        super(place, clientFactory);
        this.applicationId = place.getApplicationId();
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        super.start(containerWidget, eventBus);
    }

    @Override
    protected String getEntityId(P place) {
        return place.getPluginId();
    }
    
    protected abstract void loadPluginInfos(AsyncCallback<List<PluginInfoDto>> callback);

    @Override
    protected void onEntityRetrieved() {
        loadPluginInfos(new AsyncCallback<List<PluginInfoDto>>() {
            @Override
            public void onSuccess(List<PluginInfoDto> result) {
                detailsView.getPluginInfo().setAcceptableValues(result);
            }
            @Override
            public void onFailure(Throwable caught) {
                Utils.handleException(caught, detailsView);
            }
        });
        
        if (!create) {
            detailsView.getName().setValue(entity.getName());
            detailsView.getDescription().setValue(entity.getDescription());
            detailsView.getCreatedUsername().setValue(entity.getCreatedUsername());
            detailsView.getCreatedDateTime().setValue(Utils.millisecondsToDateTimeString(entity.getCreatedTime()));
            detailsView.getConfiguration().setValue(entity.getFieldConfiguration());
            PluginInfoDto appenderInfo = 
                    new PluginInfoDto(entity.getPluginTypeName(), entity.getFieldConfiguration(), entity.getPluginClassName());
            detailsView.getPluginInfo().setValue(appenderInfo);
        }
    }

    @Override
    protected void onSave() {
        entity.setName(detailsView.getName().getValue());
        entity.setDescription(detailsView.getDescription().getValue());
        PluginInfoDto appenderInfo = detailsView.getPluginInfo().getValue();
        entity.setPluginTypeName(appenderInfo.getPluginTypeName());
        entity.setPluginClassName(appenderInfo.getPluginClassName());
        entity.setFieldConfiguration(detailsView.getConfiguration().getValue());
    }

}
