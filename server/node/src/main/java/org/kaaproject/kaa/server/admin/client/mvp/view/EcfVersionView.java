package org.kaaproject.kaa.server.admin.client.mvp.view;

import com.google.gwt.user.client.ui.Button;
import org.kaaproject.kaa.common.dto.event.EventClassDto;

public interface EcfVersionView extends BaseListView<EventClassDto> {

    Button addButtonEventClass();
    Button addButton();

}
