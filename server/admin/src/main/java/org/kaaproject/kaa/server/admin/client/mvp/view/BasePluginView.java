package org.kaaproject.kaa.server.admin.client.mvp.view;

import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.plugin.PluginInfoDto;

import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ValueListBox;

public interface BasePluginView extends BaseDetailsView {

    HasValue<String> getName();

    ValueListBox<PluginInfoDto> getPluginInfo();

    HasValue<String> getDescription();

    HasValue<String> getCreatedDateTime();

    HasValue<String> getCreatedUsername();

    HasValue<RecordField> getConfiguration();

}
