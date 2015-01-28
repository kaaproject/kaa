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

package org.kaaproject.kaa.server.admin.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;

public interface KaaAdminResources extends ClientBundle {

    public interface KaaAdminStyle extends CssResource {

        String DEFAULT_CSS = "KaaAdmin.css";
        
        String actionPopup();
        
        @ClassName("b-app-back-button")
        String bAppBackButton();
        
        @ClassName("b-app-back-button-header")
        String bAppBackButtonHeader();
        
        @ClassName("b-app-back-button-panel")
        String bAppBackButtonPanel();
        
        @ClassName("b-app-button-small")
        String bAppButtonSmall();
        
        @ClassName("b-app-cell-button")
        String bAppCellButton();
        
        @ClassName("b-app-cell-button-small")
        String bAppCellButtonSmall();
        
        @ClassName("b-app-content-close")
        String bAppContentClose();
        
        @ClassName("b-app-content-details-table")
        String bAppContentDetailsTable();
        
        @ClassName("b-app-content-notes")
        String bAppContentNotes();
        
        @ClassName("b-app-content-sub-title")
        String bAppContentSubTitle();
        
        @ClassName("b-app-content-title")
        String bAppContentTitle();
        
        @ClassName("b-app-content-title-label")
        String bAppContentTitleLabel();
        
        @ClassName("b-app-header")
        String bAppHeader();
        
        @ClassName("b-app-header-menu")
        String bAppHeaderMenu();
        
        @ClassName("b-app-header-title")
        String bAppHeaderTitle();
        
        @ClassName("b-app-navigator")
        String bAppNavigator();
        
        @ClassName("b-app-padded-panel")
        String bAppPaddedPanel();
        
        @ClassName("b-app-sub-content")
        String bAppSubContent();
        
        @ClassName("b-app-sub-header")
        String bAppSubHeader();
        
        @ClassName("b-app-sub-header-text")
        String bAppSubHeaderText();
        
        @ClassName("b-app-sub-header-title")
        String bAppSubHeaderTitle();
        
        @ClassName("b-app-sub-navigator")
        String bAppSubNavigator();
        
        @ClassName("b-current")
        String bCurrent();
        
        @ClassName("b-nav-content")
        String bNavContent();
        
        @ClassName("b-nav-label")
        String bNavLabel();
        
        @ClassName("b-nav-panel")
        String bNavPanel();
        
        @ClassName("button-margin-left")
        String buttonMarginLeft();
        
        String caret();
        
        String linkLabel();
        
        String loginButton();
        
        String loginPanel();
        
        String secondary();
        
    }

    @NotStrict
    @Source(KaaAdminStyle.DEFAULT_CSS)
    KaaAdminStyle kaaAdminStyle();

    @ImageOptions(width = 14, height = 14)
    @Source("images/remove.png")
    ImageResource remove();

    @ImageOptions(width = 14, height = 14)
    @Source("images/send.png")
    ImageResource send();

    @ImageOptions(width = 14, height = 14)
    @Source("images/details.png")
    ImageResource details();

    @ImageOptions(width = 14, height = 14)
    @Source("images/plus.png")
    ImageResource plus();

    @ImageOptions(width = 14, height = 14)
    @Source("images/drop-down.png")
    ImageResource drop_down();

    @ImageOptions(width = 0, height = 0)
    @Source("images/circles.png")
    ImageResource circles();

    @ImageOptions(width = 0, height = 0)
    @Source("images/circles_ie6.png")
    ImageResource circles_ie6();

    @ImageOptions(width = 14, height = 14)
    @Source("images/download_icon_grey.png")
    ImageResource download();

}
