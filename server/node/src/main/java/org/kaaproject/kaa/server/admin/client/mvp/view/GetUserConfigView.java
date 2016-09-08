package org.kaaproject.kaa.server.admin.client.mvp.view;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ValueListBox;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;

/**
 * Created by pyshankov on 07.09.16.
 */
public interface GetUserConfigView extends BaseDetailsView {

    HasValue<String> getExternalUserId();

    ValueListBox<SchemaInfoDto> getConfigurationSchemaInfo();

    HasClickHandlers getDownloadUserCongigurationButton();
}
