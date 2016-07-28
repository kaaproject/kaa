package org.kaaproject.kaa.server.admin.client.mvp.data;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;

import java.util.List;

/**
 * Created by pyshankov on 22.07.16.
 */
public class TenantAdminDataProvider extends AbstractDataProvider<UserDto, String> {

   private String tenantId;


    public TenantAdminDataProvider(AbstractGrid<UserDto, String> dataGrid, HasErrorMessage hasErrorMessage,String tenantId) {
        super(dataGrid, hasErrorMessage);
        this.tenantId=tenantId;
    }

    @Override
    protected void loadData(final LoadCallback callback) {
        KaaAdmin.getDataSource().loadAllTenantAdminsByTenantId(tenantId, new AsyncCallback<List<UserDto>>() {
            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(throwable);
            }

            @Override
            public void onSuccess(List<UserDto> userDtos) {
                callback.onSuccess(userDtos);
            }
        });
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
