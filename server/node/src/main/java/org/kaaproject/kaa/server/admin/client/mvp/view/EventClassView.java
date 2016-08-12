package org.kaaproject.kaa.server.admin.client.mvp.view;


import com.google.gwt.user.client.ui.ValueListBox;

public interface EventClassView extends BaseCtlSchemaView {

    ValueListBox<String> getEventClassTypes();

}
