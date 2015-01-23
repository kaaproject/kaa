package org.kaaproject.kaa.server.admin.client.mvp.view.struct;

import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.config.ConfigurationRecordFormDto;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;

public class ConfigFormStructView extends BaseStructView<ConfigurationRecordFormDto, RecordField> {

    public ConfigFormStructView(HasErrorMessage hasErrorMessage) {
        super(hasErrorMessage);
    }

    @Override
    protected HasValue<RecordField> createBody(HasErrorMessage hasErrorMessage) {
        RecordPanel field = new RecordPanel(Utils.constants.configurationBody(), hasErrorMessage, false, false);
        field.getRecordWidget().setForceNavigation(true);
        field.setWidth("800px");
        field.setHeight("400px");
        return field;
    }

    @Override
    protected boolean hasLabel() {
        return false;
    }

    @Override
    protected void setBodyReadOnly(boolean readOnly) {
        ((RecordPanel)body).setReadOnly(readOnly);
    }

    @Override
    protected void setBodyValue(ConfigurationRecordFormDto struct) {
        body.setValue(struct.getConfigurationRecord());
    }

    @Override
    protected HandlerRegistration addBodyChangeHandler() {
        return body.addValueChangeHandler(new ValueChangeHandler<RecordField>() {
            @Override
            public void onValueChange(ValueChangeEvent<RecordField> event) {
                fireChanged();
            }
        });
    }

    @Override
    protected boolean validateBody() {
        return ((RecordPanel)body).validate();
    }

 

}
