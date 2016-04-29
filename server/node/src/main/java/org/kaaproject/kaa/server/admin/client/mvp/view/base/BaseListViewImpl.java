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
import org.kaaproject.avro.ui.gwt.client.widget.AlertPanel;
import org.kaaproject.avro.ui.gwt.client.widget.AlertPanel.Type;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.HasRowActionEventHandlers;
import org.kaaproject.kaa.common.dto.HasId;
import org.kaaproject.kaa.server.admin.client.KaaAdminResources.KaaAdminStyle;
import org.kaaproject.kaa.server.admin.client.mvp.view.BaseListView;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.ImageTextButton;
import org.kaaproject.kaa.server.admin.client.util.Utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;

public abstract class BaseListViewImpl<T extends HasId> extends ResizeComposite implements BaseListView<T> {

    interface BaseListViewImplUiBinder extends UiBinder<Widget, BaseListViewImpl<?>> { }
    private static BaseListViewImplUiBinder uiBinder = GWT.create(BaseListViewImplUiBinder.class);

    @UiField public DockLayoutPanel dockPanel;
    @UiField public HorizontalPanel backButtonPanel;
    @UiField public Button backButton;
    @UiField public Label titleLabel;
    @UiField public HorizontalPanel buttonsPanel;
    @UiField (provided=true) public final ImageTextButton addButton;
    @UiField (provided=true) public final AlertPanel errorPanel;
    @UiField (provided=true) public final KaaAdminStyle kaaAdminStyle;
    @UiField (provided=true) public final AvroUiStyle avroUiStyle;

    protected AbstractGrid<T, String> grid;

    protected Presenter presenter;

    protected boolean editable;

    public BaseListViewImpl(boolean editable) {
        this.editable = editable;
        addButton = new ImageTextButton(Utils.resources.plus(), addButtonString());
        errorPanel = new AlertPanel(Type.ERROR);
        kaaAdminStyle = Utils.kaaAdminStyle;
        avroUiStyle = Utils.avroUiStyle;

        initWidget(createAndBindUi());

        grid = createGrid();

        dockPanel.add(grid);

        titleLabel.setText(titleString());
        addButton.setVisible(editable);

        clearError();
    }
    
    protected void appendToolbarWidget(Widget widget) {
        buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        buttonsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        buttonsPanel.add(widget);
        buttonsPanel.setCellHeight(widget, "100%");
    }

    protected Widget createAndBindUi() {
        return uiBinder.createAndBindUi(this);
    }

    @Override
    public HasClickHandlers getAddButton() {
        return addButton;
    }
    
    @Override
    public HasClickHandlers getBackButton() {
        return backButton;
    }

    @Override
    public void setBackEnabled(boolean enabled) {
        backButtonPanel.setVisible(enabled);
    }

    @Override
    public MultiSelectionModel<T> getSelectionModel() {
        return grid.getSelectionModel();
    }

    @Override
    public AbstractGrid<T,String> getListWidget() {
        return grid;
    }

    @Override
    public HasRowActionEventHandlers<String> getRowActionsSource() {
        return grid;
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
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    protected abstract AbstractGrid<T, String> createGrid();

    protected abstract String titleString();

    protected abstract String addButtonString();

}
