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

package org.kaaproject.kaa.server.admin.client.mvp.view.base;

import org.kaaproject.avro.ui.gwt.client.AvroUiResources.AvroUiStyle;
import org.kaaproject.avro.ui.gwt.client.input.InputEvent;
import org.kaaproject.avro.ui.gwt.client.input.InputEventHandler;
import org.kaaproject.avro.ui.gwt.client.widget.AlertPanel;
import org.kaaproject.avro.ui.gwt.client.widget.AlertPanel.Type;
import org.kaaproject.kaa.server.admin.client.KaaAdminResources.KaaAdminStyle;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseDetailsView;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class BaseDetailsViewImpl extends Composite implements InputEventHandler, ChangeHandler, BaseDetailsView {

    interface BaseDetailsViewImplUiBinder extends UiBinder<Widget, BaseDetailsViewImpl> { }
    private static BaseDetailsViewImplUiBinder uiBinder = GWT.create(BaseDetailsViewImplUiBinder.class);

    protected static final String FULL_WIDTH = "100%";
    protected static final int DEFAULT_TEXTBOX_SIZE = 255;

    @UiField public DockLayoutPanel dockPanel;
    @UiField public VerticalPanel northPanel;
    @UiField public HorizontalPanel topPanel;
    @UiField public Label subTitleLabel;
    @UiField public FlexTable detailsTable;
    @UiField public HTMLPanel requiredFieldsNoteLabel;
    @UiField (provided=true) public final AlertPanel errorPanel;
    @UiField public FlowPanel footer;
    @UiField(provided = true) public final KaaAdminStyle kaaAdminStyle;
    @UiField(provided = true) public final AvroUiStyle avroUiStyle;

    protected final boolean create;

    private boolean hasChanged = false;

    protected Presenter presenter;

    protected final boolean editable;

    public BaseDetailsViewImpl(boolean create) {
        this(create, true);
    }

    public BaseDetailsViewImpl(boolean create, boolean editable) {
        this.create = create;
        this.editable = editable;
        errorPanel = new AlertPanel(Type.ERROR);
        kaaAdminStyle = Utils.kaaAdminStyle;
        avroUiStyle = Utils.avroUiStyle;
        initWidget(uiBinder.createAndBindUi(this));
        constructTopPanel();

        getTitileLabelWidget().setText(Utils.constants.title());
        getSaveButtonWidget().setText(Utils.constants.save());
        getCancelButtonWidget().setText(Utils.constants.cancel());
        requiredFieldsNoteLabel.getElement().setInnerSafeHtml(
                SafeHtmlUtils.fromSafeConstant(Utils.messages
                        .requiredFieldsNote(Utils.avroUiStyle
                                .requiredField())));

        if (create) {
            getTitileLabelWidget().setText(getCreateTitle());
            getCancelButtonWidget().setVisible(true);
        } else {
            getTitileLabelWidget().setText(getViewTitle());
            getBackButtonPanelWidget().setVisible(true);
        }
        subTitleLabel.setText(getSubTitle());

        updateSaveButton(false, false);

        detailsTable.getColumnFormatter().setWidth(0, "200px");
        detailsTable.getColumnFormatter().setWidth(1, "300px");
        if (!create) {
            detailsTable.getColumnFormatter().setWidth(2, "300px");
        }

        getSaveButtonWidget().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateSaveButton(false, false);
                hasChanged = false;
            }
        });
        getSaveButtonWidget().setVisible(editable);
        requiredFieldsNoteLabel.setVisible(editable);

        initDetailsTable();
        
        clearError();
    }
    
    protected void updateNorthSize(int sizePx) {
        dockPanel.setWidgetSize(northPanel, sizePx);
    }
    
    protected Widget getBackButtonPanelWidget() {
        return backButtonPanelWidget;
    }
    
    protected Button getBackButtonWidget() {
        return backButtonWidget;
    }
    
    protected Label getTitileLabelWidget() {
        return titleLabelWidget;
    }
    
    protected Button getSaveButtonWidget() {
        return saveButtonWidget;
    }

    protected Button getCancelButtonWidget() {
        return cancelButtonWidget;
    }

    private Widget backButtonPanelWidget;
    private Button backButtonWidget;
    private Label titleLabelWidget;
    private Button saveButtonWidget;
    private Button cancelButtonWidget;
    
    protected void constructTopPanel() {
        FlowPanel panel = new FlowPanel();
        panel.setSize("100%", "100%");
        topPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        topPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        topPanel.add(panel);
        topPanel.setCellHeight(panel, "100%");
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.setHeight("100%");
        panel.add(horizontalPanel);
        
        HorizontalPanel backButtonPanel = new HorizontalPanel();
        backButtonPanel.setHeight("100%");
        backButtonPanel.addStyleName(Utils.kaaAdminStyle.bAppPaddedPanel());
        backButtonPanel.setVisible(false);        
        horizontalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        horizontalPanel.add(backButtonPanel);
        horizontalPanel.setCellHeight(backButtonPanel, "100%");
        
        backButtonPanelWidget = backButtonPanel;
        
        Button backButton = new Button();
        backButton.addStyleName(Utils.kaaAdminStyle.bAppBackButton());        
        backButtonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        backButtonPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        backButtonPanel.add(backButton);
        backButtonPanel.setCellHeight(backButton, "100%");
        
        backButtonWidget = backButton;
        
        Label titleLabel = new Label();
        titleLabel.addStyleName(Utils.kaaAdminStyle.bAppContentTitle());
        horizontalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        horizontalPanel.add(titleLabel);
        horizontalPanel.setCellHeight(titleLabel, "100%");
        
        titleLabelWidget = titleLabel;
        
        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.setHeight("100%");
        buttonsPanel.addStyleName(Utils.avroUiStyle.buttonsPanel());
        horizontalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        horizontalPanel.add(buttonsPanel);
        horizontalPanel.setCellHeight(buttonsPanel, "100%");
        
        Button saveButton = new Button();
        buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        buttonsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        buttonsPanel.add(saveButton);
        buttonsPanel.setCellHeight(saveButton, "100%");
        
        saveButtonWidget = saveButton;
        
        Button cancelButton = new Button();
        cancelButton.setVisible(false);
        buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        buttonsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        buttonsPanel.add(cancelButton);
        buttonsPanel.setCellHeight(cancelButton, "100%");
        
        cancelButtonWidget = cancelButton;
    }

    @Override
    public void reset() {
        clearError();
        resetImpl();
        updateSaveButton(false, false);
        hasChanged = false;
    }

    @Override
    public boolean hasChanged() {
        return hasChanged;
    }

    @Override
    public void setBackEnabled(boolean enabled) {
        getBackButtonPanelWidget().setVisible(!create && enabled);
    }

    @Override
    public void setCancelEnabled(boolean enabled) {
        getCancelButtonWidget().setVisible(create && enabled);
    }

    @Override
    public HasClickHandlers getBackButton() {
        return getBackButtonWidget();
    }

    @Override
    public HasClickHandlers getSaveButton() {
        return getSaveButtonWidget();
    }

    @Override
    public HasClickHandlers getCancelButton() {
        return getCancelButtonWidget();
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setTitle(String title) {
        getTitileLabelWidget().setText(title);
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

    @Override
    public void onInputChanged(InputEvent event) {
        fireChanged();
    }

    @Override
    public void onChange(ChangeEvent event) {
        fireChanged();
    }

    protected void fireChanged() {
        boolean valid = true;
        valid &= validate();
        updateSaveButton(valid, !valid);
        hasChanged = true;
    }

    protected abstract String getCreateTitle();

    protected abstract String getViewTitle();

    protected abstract String getSubTitle();

    protected abstract void initDetailsTable();

    protected abstract void resetImpl();

    protected abstract boolean validate();


    protected void updateSaveButton(boolean enabled, boolean invalid) {
        if (create || invalid) {
            getSaveButtonWidget().setText(create ? Utils.constants.add() : Utils.constants.save());
        } else {
            getSaveButtonWidget().setText(enabled ? Utils.constants.save() : Utils.constants.saved());
        }
        getSaveButtonWidget().setEnabled(enabled);
    }

    protected FlowPanel getFooter() {
        return footer;
    }

}
