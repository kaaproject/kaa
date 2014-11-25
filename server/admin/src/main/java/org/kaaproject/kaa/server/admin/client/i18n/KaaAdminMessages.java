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

package org.kaaproject.kaa.server.admin.client.i18n;

import com.google.gwt.i18n.client.Messages;

public interface KaaAdminMessages extends Messages {

    @DefaultMessage("This is first time login.<br>Please enter Kaa admin username and password then click ''Login'' to register.")
    String kaaAdminNotExists();

    @DefaultMessage("Username and password shouldn''t be empty!")
    String emptyUsernameOrPassword();

    @DefaultMessage("Unexpected Error occurred!")
    String unexpectedError();

    @DefaultMessage("Current password is temporary. Please change your password.")
    String tempCredentials();

    @DefaultMessage("Entered passwords doesn''t match")
    String newPasswordsNotMatch();

    @DefaultMessage("New password should be different")
    String newPasswordShouldDifferent();

    @DefaultMessage("Are you sure you want to delete selected entry?")
    String deleteSelectedEntryQuestion();

    @DefaultMessage("Delete entry")
    String deleteSelectedEntryTitle();

    @DefaultMessage("Are you sure you want to unassign selected notification topic from endpoint group?")
    String removeTopicFromEndpointGroupQuestion();

    @DefaultMessage("Unassign notification topic")
    String removeTopicFromEndpointGroupTitle();

    @DefaultMessage("Fields marked with <span class=\"required\"></span> needs to be filled before saving.")
    String requiredFieldsNote();

    @DefaultMessage("<h1 title=\"Please login\">Please login</h1>")
    String loginTitle();

    @DefaultMessage("Page {0} of {1}")
    String pagerText(int current, int total);

    @DefaultMessage("Incorrect configuration. Validate your configuration regarding schema version.")
    String incorrectConfiguration();

    @DefaultMessage("Are you sure you want to delete selected log appender?")
    String removeLogAppenderQuestion();

    @DefaultMessage("Remove log appender")
    String removeLogAppenderTitle();

}
