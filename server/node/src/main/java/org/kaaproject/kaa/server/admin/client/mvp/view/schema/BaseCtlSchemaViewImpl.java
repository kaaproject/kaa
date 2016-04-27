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

package org.kaaproject.kaa.server.admin.client.mvp.view.schema;

import org.kaaproject.avro.ui.gwt.client.widget.AvroWidgetsConfig;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextArea;
import org.kaaproject.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseCtlSchemaView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.CtlSchemaReferenceBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.RecordPanel;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaReferenceDto;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.layout.client.Layout.Alignment;
import com.google.gwt.layout.client.Layout.AnimationCallback;
import com.google.gwt.layout.client.Layout.Layer;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.HasConstrainedValue;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ToggleButton;

public abstract class BaseCtlSchemaViewImpl extends BaseDetailsViewImpl implements BaseCtlSchemaView {

    private static final String CTL_REFERENCE_PANEL_HEIGHT = "280px";
    private static final String SCHEMA_FORM_PANEL_HEIGHT = "700px";
    private static final int CTL_PANEL_ANIMATION_DURATION = 500;

    private static final String REQUIRED = Utils.avroUiStyle.requiredField();
    
    private SizedTextBox version;
    private SizedTextBox name;
    private SizedTextArea description;
    private SizedTextBox createdUsername;
    private SizedTextBox createdDateTime;

    private ToggleButton existingCtlButton;
    private ToggleButton newCtlButton;
    private FragmentLayoutPanel ctlSchemaPanel;
    private CtlSchemaReferenceBox ctlSchemaReferenceBox;
    private RecordPanel schemaForm;

    public BaseCtlSchemaViewImpl(boolean create) {
        super(create);
    }

    @Override
    protected void initDetailsTable() {

        Label versionLabel = new Label(Utils.constants.version());
        version = new KaaAdminSizedTextBox(-1, false);
        version.setWidth("100%");
        detailsTable.setWidget(0, 0, versionLabel);
        detailsTable.setWidget(0, 1, version);
        versionLabel.setVisible(!create);
        version.setVisible(!create);

        Label authorLabel = new Label(Utils.constants.author());
        createdUsername = new KaaAdminSizedTextBox(-1, false);
        createdUsername.setWidth("100%");
        detailsTable.setWidget(1, 0, authorLabel);
        detailsTable.setWidget(1, 1, createdUsername);

        authorLabel.setVisible(!create);
        createdUsername.setVisible(!create);

        Label dateTimeCreatedLabel = new Label(Utils.constants.dateTimeCreated());
        createdDateTime = new KaaAdminSizedTextBox(-1, false);
        createdDateTime.setWidth("100%");
        detailsTable.setWidget(2, 0, dateTimeCreatedLabel);
        detailsTable.setWidget(2, 1, createdDateTime);

        dateTimeCreatedLabel.setVisible(!create);
        createdDateTime.setVisible(!create);

        name = new KaaAdminSizedTextBox(DEFAULT_TEXTBOX_SIZE);
        name.setWidth("100%");
        Label nameLabel = new Label(Utils.constants.name());
        nameLabel.addStyleName(REQUIRED);
        detailsTable.setWidget(3, 0, nameLabel);
        detailsTable.setWidget(3, 1, name);
        name.addInputHandler(this);

        description = new SizedTextArea(1024);
        description.setWidth("100%");
        description.getTextArea().getElement().getStyle().setPropertyPx("minHeight", 80);
        Label descriptionLabel = new Label(Utils.constants.description());
        detailsTable.setWidget(4, 0, descriptionLabel);
        detailsTable.setWidget(4, 1, description);
        description.addInputHandler(this);

        detailsTable.getCellFormatter().setVerticalAlignment(4, 0, HasVerticalAlignment.ALIGN_TOP);
        
        HorizontalPanel ctlSwitchPanel = new HorizontalPanel();
        detailsTable.setWidget(5, 0, ctlSwitchPanel);
        detailsTable.getFlexCellFormatter().setColSpan(5, 0, 2);
        ctlSwitchPanel.getElement().getStyle().setPaddingTop(10, Unit.PX);
        ctlSwitchPanel.getElement().getStyle().setPaddingBottom(10, Unit.PX);
        existingCtlButton = new ToggleButton(Utils.constants.selectExistingType());        
        newCtlButton = new ToggleButton(Utils.constants.createNewType());
        newCtlButton.getElement().getStyle().setMarginLeft(10, Unit.PX);
        existingCtlButton.setValue(true);
        ctlSwitchPanel.add(existingCtlButton);
        ctlSwitchPanel.add(newCtlButton);
        
        ctlSwitchPanel.setVisible(create);
        
        final LayoutPanel rootPanel = new LayoutPanel();
        ctlSchemaPanel = new FragmentLayoutPanel();
        
        if (create) {
            existingCtlButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    ctlSchemaPanel.showWidget(0);
                    existingCtlButton.setValue(true);
                    newCtlButton.setValue(false);
                    fireChanged();
                }
            });
            ctlSchemaPanel.setAnimationCallback(new AnimationCallback() {
                @Override
                public void onAnimationComplete() {
                    if (ctlSchemaPanel.getVisibleWidgetIndex() == 0) {
                        rootPanel.setHeight(CTL_REFERENCE_PANEL_HEIGHT);
                    }
                }
                @Override
                public void onLayout(Layer layer, double progress) {}
            });
        }
        
        ctlSchemaPanel.setAnimationDuration(create ? CTL_PANEL_ANIMATION_DURATION : 0);
                
        ctlSchemaReferenceBox = new CtlSchemaReferenceBox();
        ctlSchemaReferenceBox.setWidth("500px");

        schemaForm = new RecordPanel(new AvroWidgetsConfig.Builder().
                recordPanelWidth(900).createConfig(),
                Utils.constants.schema(), this, !create, !create);
        
        if (create) {
            schemaForm.addValueChangeHandler(new ValueChangeHandler<RecordField>() {
                @Override
                public void onValueChange(ValueChangeEvent<RecordField> event) {
                    fireChanged();
                }
            });
            ctlSchemaReferenceBox.addValueChangeHandler(new ValueChangeHandler<CtlSchemaReferenceDto>() {
                @Override
                public void onValueChange(ValueChangeEvent<CtlSchemaReferenceDto> event) {
                    fireChanged();                    
                }
            });
        }
        
        ctlSchemaPanel.add(ctlSchemaReferenceBox);
        ctlSchemaPanel.add(schemaForm);
        
        rootPanel.add(ctlSchemaPanel);
        rootPanel.setWidgetLeftRight(ctlSchemaPanel, 0, Unit.PX, 0, Unit.PX);
        rootPanel.setWidgetTopBottom(ctlSchemaPanel, 0, Unit.PX, 0, Unit.PX);    
        rootPanel.setWidgetVerticalPosition(ctlSchemaPanel, Alignment.STRETCH);
        
        rootPanel.setSize("1000px", create ? CTL_REFERENCE_PANEL_HEIGHT : SCHEMA_FORM_PANEL_HEIGHT);
        
        getFooter().addStyleName(Utils.kaaAdminStyle.bAppContentDetailsTable());
        getFooter().addStyleName(Utils.avroUiStyle.fieldWidget());
        getFooter().setWidth("1000px");
        getFooter().add(rootPanel);
        
        ctlSchemaPanel.showWidget(create ? 0 : 1);
        name.setFocus(true);
    }
    
    @Override
    protected void resetImpl() {
        version.setValue("");
        version.setValue("");
        name.setValue("");
        description.setValue("");
        createdUsername.setValue("");
        createdDateTime.setValue("");
        ctlSchemaReferenceBox.reset();
        schemaForm.reset();
        if (create) {
            existingCtlButton.setValue(true);
            newCtlButton.setValue(false);
            ctlSchemaPanel.setAnimationDuration(0);
            ctlSchemaPanel.showWidget(0);
            ctlSchemaPanel.setAnimationDuration(CTL_PANEL_ANIMATION_DURATION);
        }
    }

    @Override
    public HasValue<String> getVersion() {
        return version;
    }

    @Override
    protected boolean validate() {
        boolean result = name.getValue().length()>0;
        if (create) {
            if (ctlSchemaPanel.getVisibleWidgetIndex() == 0) {
                result &= ctlSchemaReferenceBox.getValue() != null;
            } else {
                result &= schemaForm.validate();
            }
        }
        return result;
    }

    @Override
    public RecordPanel getSchemaForm() {
        return schemaForm;
    }
    
    @Override
    public HasValue<String> getName() {
        return name;
    }

    @Override
    public HasValue<String> getDescription() {
        return description;
    }

    @Override
    public HasValue<String> getCreatedUsername() {
        return createdUsername;
    }

    @Override
    public HasValue<String> getCreatedDateTime() {
        return createdDateTime;
    }
    
    @Override
    public HasConstrainedValue<CtlSchemaReferenceDto> getCtlSchemaReference() {
        return ctlSchemaReferenceBox;
    }
    
    @Override
    public boolean useExistingCtlSchema() {
        return ctlSchemaPanel.getVisibleWidgetIndex() == 0;
    }
    
    public HasClickHandlers getNewCtlButton() {
        return newCtlButton;
    }
    
    private class FragmentLayoutPanel extends DeckLayoutPanel {
        
        private AnimationCallback animationCallback;
        
        private FragmentLayoutPanel() {
            super();
        }
        
        private void setAnimationCallback(AnimationCallback animationCallback) {
            this.animationCallback = animationCallback;
        }
        
        @Override
        public void animate(int duration) {
            super.animate(duration, animationCallback);
        }
        
    }

}
