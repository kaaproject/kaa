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

package org.kaaproject.kaa.sandbox.web.client.mvp.view.main;

import java.util.List;

import org.kaaproject.kaa.sandbox.demo.projects.Project;
import org.kaaproject.kaa.sandbox.web.client.mvp.event.project.HasProjectActionEventHandlers;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.MainView;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.widget.AlertPanel;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.widget.AlertPanel.Type;
import org.kaaproject.kaa.sandbox.web.client.mvp.view.widget.DemoProjectsView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public class MainViewImpl extends Composite implements MainView {

    interface MainViewImplUiBinder extends UiBinder<Widget, MainViewImpl> { }
    private static MainViewImplUiBinder uiBinder = GWT.create(MainViewImplUiBinder.class);

    protected static final int DEFAULT_TEXTBOX_SIZE = 255;

    @UiField public Label titleLabel;
    @UiField public FlexTable detailsTable;
    @UiField (provided=true) public AlertPanel errorPanel;
    @UiField (provided=true) public AlertPanel infoPanel;

    private Presenter presenter;
    
    private Anchor goToKaaAdminWeb;
    private Anchor goToAvroUiSandboxWeb;
    private HTML changeKaaHostLabel;
    private TextBox kaaHost;
    private Button changeKaaHostButton;
    private DemoProjectsView demoProjectsView;
    
    public MainViewImpl() {
        errorPanel = new AlertPanel(Type.ERROR);
        infoPanel =  new AlertPanel(Type.INFO);
        initWidget(uiBinder.createAndBindUi(this));

        titleLabel.setText("Main console");

        detailsTable.getColumnFormatter().setWidth(0, "200px");
        detailsTable.getColumnFormatter().setWidth(1, "300px");

        initDetailsTable();

        clearMessages();
    }

    @Override
    public void reset() {
        clearMessages();
        resetImpl();
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
    public void clearMessages() {
        errorPanel.setMessage("");
        UIObject.setVisible(DOM.getParent(errorPanel.getElement()), false);
        infoPanel.setMessage("");
        UIObject.setVisible(DOM.getParent(infoPanel.getElement()), false);
    }

    @Override
    public void setErrorMessage(String message) {
        errorPanel.setMessage(message);
        UIObject.setVisible(DOM.getParent(errorPanel.getElement()), true);
    }
    
    @Override
    public void setInfoMessage(String message) {
        infoPanel.setMessage(message);
        UIObject.setVisible(DOM.getParent(infoPanel.getElement()), true);
    }

    @Override
    public void setChangeKaaHostEnabled(boolean enabled) {
        changeKaaHostLabel.setVisible(enabled);
        kaaHost.setVisible(enabled);
        changeKaaHostButton.setVisible(enabled);
    }
    
    private Widget constructGotoLink (Anchor anchor) {
        HTML label = new HTML("Go to");
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(label);
        label.getElement().getStyle().setPaddingRight(15, Unit.PX);
        label.getElement().getStyle().setFontSize(16, Unit.PX);
        anchor.getElement().getStyle().setFontSize(16, Unit.PX);
        panel.add(anchor);
        panel.getElement().getStyle().setPaddingBottom(40, Unit.PX);
        return panel;
    }
 
    private void initDetailsTable() {
        
        int row = -1;
        
        goToKaaAdminWeb = new Anchor("Kaa Administrative Web Console");
        Widget gotoLink = constructGotoLink(goToKaaAdminWeb);
        detailsTable.setWidget(++row, 0,  gotoLink);
        detailsTable.getFlexCellFormatter().setColSpan(row, 0, 2);
        
        goToAvroUiSandboxWeb = new Anchor("Avro UI Sandbox Web Console");
        gotoLink = constructGotoLink(goToAvroUiSandboxWeb);
        detailsTable.setWidget(++row, 0,  gotoLink);
        detailsTable.getFlexCellFormatter().setColSpan(row, 0, 2);
        
        changeKaaHostLabel = new HTML("To change kaa services host/ip enter new host<br>value in field below and click 'Change' button.");
        detailsTable.setWidget(++row, 0, changeKaaHostLabel);
        detailsTable.getFlexCellFormatter().setColSpan(row, 0, 2);
        kaaHost = new TextBox();
        kaaHost.setWidth("200px");
        detailsTable.setWidget(++row, 0, kaaHost);
        changeKaaHostButton = new Button("Change");
        detailsTable.setWidget(row, 1, changeKaaHostButton);
        
        Label demoProjectsTitle = new Label("Demo projects");
        demoProjectsTitle.getElement().getStyle().setPaddingTop(40, Unit.PX);
        demoProjectsTitle.getElement().getStyle().setPaddingBottom(20, Unit.PX);
        demoProjectsTitle.addStyleName("b-app-content-title");

        detailsTable.setWidget(++row, 0, demoProjectsTitle);
        detailsTable.getFlexCellFormatter().setColSpan(row, 0, 2);
        
        demoProjectsView = new DemoProjectsView();
        detailsTable.setWidget(++row, 0, demoProjectsView);
        detailsTable.getFlexCellFormatter().setColSpan(row, 0, 2);
    }

    private void resetImpl() {
        setChangeKaaHostEnabled(false);
        kaaHost.setText("");
        demoProjectsView.reset();
    }

    @Override
    public HasClickHandlers getChangeKaaHostButton() {
        return changeKaaHostButton;
    }

    @Override
    public HasValue<String> getKaaHost() {
        return kaaHost;
    }

    @Override
    public HasClickHandlers getGoToKaaAdminWeb() {
        return goToKaaAdminWeb;
    }
    
    @Override
    public HasClickHandlers getGoToAvroUiSandboxWeb() {
        return goToAvroUiSandboxWeb;
    }

    @Override
    public void setProjects(List<Project> projects) {
        demoProjectsView.setProjects(projects);
    }

    @Override
    public HasProjectActionEventHandlers getProjectsActionSource() {
        return demoProjectsView;
    }

}
