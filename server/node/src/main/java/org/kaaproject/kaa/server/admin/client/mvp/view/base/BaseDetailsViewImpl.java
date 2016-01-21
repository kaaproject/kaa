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
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public abstract class BaseDetailsViewImpl extends Composite implements InputEventHandler, ChangeHandler, BaseDetailsView {

    interface BaseDetailsViewImplUiBinder extends UiBinder<Widget, BaseDetailsViewImpl> { }
    private static BaseDetailsViewImplUiBinder uiBinder = GWT.create(BaseDetailsViewImplUiBinder.class);

    protected static final String FULL_WIDTH = "100%";
    protected static final int DEFAULT_TEXTBOX_SIZE = 255;

    @UiField public Label titleLabel;
    @UiField public Label subTitleLabel;
    @UiField public FlexTable detailsTable;
    @UiField public HorizontalPanel backButtonPanel;
    @UiField public HorizontalPanel buttonsPanel;
    @UiField public Button backButton;
    @UiField public Button saveButton;
    @UiField public Button cancelButton;
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

        titleLabel.setText(Utils.constants.title());
        saveButton.setText(Utils.constants.save());
        cancelButton.setText(Utils.constants.cancel());
        requiredFieldsNoteLabel.getElement().setInnerSafeHtml(
                SafeHtmlUtils.fromSafeConstant(Utils.messages
                        .requiredFieldsNote(Utils.avroUiStyle
                                .requiredField())));

        if (create) {
            titleLabel.setText(getCreateTitle());
            cancelButton.setVisible(true);
        } else {
            titleLabel.setText(getViewTitle());
            backButtonPanel.setVisible(true);
        }
        subTitleLabel.setText(getSubTitle());

        updateSaveButton(false, false);

        detailsTable.getColumnFormatter().setWidth(0, "200px");
        detailsTable.getColumnFormatter().setWidth(1, "300px");
        if (!create) {
            detailsTable.getColumnFormatter().setWidth(2, "300px");
        }

        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateSaveButton(false, false);
                hasChanged = false;
            }
        });
        saveButton.setVisible(editable);
        requiredFieldsNoteLabel.setVisible(editable);

        initDetailsTable();
        
        clearError();
    }
    
    protected void appendToolbarWidget(Widget widget) {
        buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        buttonsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        buttonsPanel.add(widget);
        buttonsPanel.setCellHeight(widget, "100%");
    }
    
    protected void prependToolbarWidget(Widget widget) {
        buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        buttonsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        buttonsPanel.insert(widget, 0);
        buttonsPanel.setCellHeight(widget, "100%");
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
        backButtonPanel.setVisible(!create && enabled);
    }

    @Override
    public void setCancelEnabled(boolean enabled) {
        cancelButton.setVisible(create && enabled);
    }

    @Override
    public HasClickHandlers getBackButton() {
        return backButton;
    }

    @Override
    public HasClickHandlers getSaveButton() {
        return saveButton;
    }

    @Override
    public HasClickHandlers getCancelButton() {
        return cancelButton;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setTitle(String title) {
        titleLabel.setText(title);
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
            saveButton.setText(create ? Utils.constants.add() : Utils.constants.save());
        } else {
            saveButton.setText(enabled ? Utils.constants.save() : Utils.constants.saved());
        }
        saveButton.setEnabled(enabled);
    }

    protected FlowPanel getFooter() {
        return footer;
    }

}
