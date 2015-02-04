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

import com.google.gwt.i18n.client.ConstantsWithLookup;

public interface KaaAdminConstants extends ConstantsWithLookup {

    @DefaultStringValue("There is no data to display")
    String dataGridEmpty();

    @DefaultStringValue("Ok")
    String ok();

    @DefaultStringValue("Yes")
    String yes();

    @DefaultStringValue("No")
    String no();

    @DefaultStringValue("Entered user name already exists, please choose another")
    String username_exists();

    @DefaultStringValue("Entered email already registered for another account, please choose another")
    String email_exists();

    @DefaultStringValue("User with given username not found")
    String user_not_found();

    @DefaultStringValue("There is no registered user with specified email")
    String user_email_not_found();

    @DefaultStringValue("There is no registered user with specified username or email")
    String user_or_email_not_found();
    
    @DefaultStringValue("Requested user doesn't have email")
    String user_email_not_defined();
    
    @DefaultStringValue("Current password is invalid")
    String old_password_mismatch();

    @DefaultStringValue("New entered password have bad strength")
    String bad_password_strength();

    @DefaultStringValue("Current password")
    String oldPassword();

    @DefaultStringValue("New password")
    String newPassword();

    @DefaultStringValue("New password again")
    String newPasswordAgain();

    @DefaultStringValue("Change password")
    String change_password();
    
    @DefaultStringValue("Reset password")
    String reset_password();
    
    @DefaultStringValue("Username or email")
    String usernameOrEmail();

    @DefaultStringValue("Send reset password link")
    String sendResetPasswordLink();
    
    @DefaultStringValue("Forgot password?")
    String forgotPassword();
    
    @DefaultStringValue("Reset link has been sent")
    String resetLinkWasSent();
    
    @DefaultStringValue("Password has been reset")
    String passwordWasReset();

    @DefaultStringValue("Cancel")
    String cancel();

    @DefaultStringValue("Close")
    String close();

    @DefaultStringValue("Unexpected service error occurred: ")
    String general_error();

    @DefaultStringValue("You are not authorized to perform this operation!")
    String not_authorized();

    @DefaultStringValue("You do not have permission to perform this operation!")
    String permission_denied();

    @DefaultStringValue("Provided arguments are invalid for requested operation!")
    String invalid_arguments();

    @DefaultStringValue("Requested item not found!")
    String item_not_found();

    @DefaultStringValue("Schema validation error: ")
    String invalid_schema();

    @DefaultStringValue("Kaa admin")
    String kaa_admin();

    @DefaultStringValue("Tenant admin")
    String tenant_admin();

    @DefaultStringValue("Tenant developer")
    String tenant_developer();

    @DefaultStringValue("Tenant user")
    String tenant_user();

    @DefaultStringValue("Generate SDK")
    String generate_sdk();

    @DefaultStringValue("Send notification")
    String send_notification();

    @DefaultStringValue("Configuration schema version")
    String configurationSchemaVersion();

    @DefaultStringValue("Profile schema version")
    String profileSchemaVersion();

    @DefaultStringValue("Notification schema version")
    String notificationSchemaVersion();

    @DefaultStringValue("Log schema version")
    String logSchemaVersion();

    @DefaultStringValue("Target platform")
    String targetPlatform();

    @DefaultStringValue("Java")
    String java();

    @DefaultStringValue("Android")
    String android();

    @DefaultStringValue("C++")
    String cpp();

    @DefaultStringValue("C")
    String c();

    @DefaultStringValue("Notification schema")
    String notificationSchema();

    @DefaultStringValue("Expires at")
    String expiresAt();

    @DefaultStringValue("Select notification file")
    String selectNotificationFile();

    @DefaultStringValue("Send")
    String send();

    @DefaultStringValue("Add topic to endpoint group")
    String add_topic_to_ep();

    @DefaultStringValue("Notification topic")
    String notificationTopic();

    @DefaultStringValue("Select notification topics")
    String selectNotificationTopics();

    @DefaultStringValue("Add")
    String add();

    @DefaultStringValue("Save")
    String save();

    @DefaultStringValue("Saved")
    String saved();

    @DefaultStringValue("Application")
    String application();

    @DefaultStringValue("App name")
    String appName();

    @DefaultStringValue("Applications")
    String applications();

    @DefaultStringValue("Title")
    String title();

    @DefaultStringValue("Public Key")
    String publicKey();

    @DefaultStringValue("Application Token")
    String appToken();

    @DefaultStringValue("Add new application")
    String addNewApplication();

    @DefaultStringValue("Application details")
    String applicationDetails();

    @DefaultStringValue("Username:")
    String username();

    @DefaultStringValue("Password:")
    String password();

    @DefaultStringValue("Login")
    String login();

    @DefaultStringValue("Configuration schema")
    String configurationSchema();

    @DefaultStringValue("Configuration")
    String configuration();

    @DefaultStringValue("Endpoint group")
    String endpointGroup();

    @DefaultStringValue("Endpoint groups")
    String endpointGroups();

    @DefaultStringValue("Notification")
    String notification();

    @DefaultStringValue("Profile schema")
    String profileSchema();

    @DefaultStringValue("Profile")
    String profile();

    @DefaultStringValue("Profile filter")
    String profileFilter();

    @DefaultStringValue("Schemas")
    String schemas();

    @DefaultStringValue("Tenants")
    String tenants();

    @DefaultStringValue("Notification topics")
    String notificationTopics();

    @DefaultStringValue("Users")
    String users();

    @DefaultStringValue("Event class families")
    String ecfs();

    @DefaultStringValue("Delete")
    String delete();

    @DefaultStringValue("Actions")
    String actions();

    @DefaultStringValue("Action")
    String action();

    @DefaultStringValue("Sign out")
    String signOut();

    @DefaultStringValue("Settings")
    String settings();

    @DefaultStringValue("Configuration body")
    String configurationBody();

    @DefaultStringValue("Configuration schemas")
    String configurationSchemas();

    @DefaultStringValue("Add new schema")
    String addNewSchema();

    @DefaultStringValue("Add configuration schema")
    String addConfigurationSchema();

    @DefaultStringValue("Configuration schema details")
    String configurationSchemaDetails();

    @DefaultStringValue("Configuration details")
    String configurationDetails();

    @DefaultStringValue("Name")
    String name();

    @DefaultStringValue("Weight")
    String weight();

    @DefaultStringValue("Author")
    String author();

    @DefaultStringValue("Date created")
    String dateCreated();

    @DefaultStringValue("Number of EPs")
    String numberOfEps();

    @DefaultStringValue("Add new endpoint group")
    String addNewEndpointGroup();

    @DefaultStringValue("Endpoint group details")
    String endpointGroupDetails();

    @DefaultStringValue("Date/Time created")
    String dateTimeCreated();

    @DefaultStringValue("Number of endpoints")
    String numberOfEndpoints();

    @DefaultStringValue("Description")
    String description();

    @DefaultStringValue("Profile filters")
    String profileFilters();

    @DefaultStringValue("Configurations")
    String configurations();

    @DefaultStringValue("Include deprecated")
    String includeDeprecated();

    @DefaultStringValue("Add profile filter")
    String addProfileFilter();

    @DefaultStringValue("Add configuration")
    String addConfiguration();

    @DefaultStringValue("Add notification topic")
    String addNotificationTopic();

    @DefaultStringValue("Notification schemas")
    String notificationSchemas();

    @DefaultStringValue("Add notification schema")
    String addNotificationSchema();

    @DefaultStringValue("Notification schema details")
    String notificationSchemaDetails();

    @DefaultStringValue("Filter body")
    String filterBody();

    @DefaultStringValue("Profile filter details")
    String profileFilterDetails();

    @DefaultStringValue("Profile schemas")
    String profileSchemas();

    @DefaultStringValue("Add profile schema")
    String addProfileSchema();

    @DefaultStringValue("Profile schema details")
    String profileSchemaDetails();

    @DefaultStringValue("Log schemas")
    String logSchemas();

    @DefaultStringValue("Log schema")
    String logSchema();

    @DefaultStringValue("Add log schema")
    String addLogSchema();

    @DefaultStringValue("Log schema details")
    String logSchemaDetails();

    @DefaultStringValue("Log")
    String log();

    @DefaultStringValue("Version")
    String version();

    @DefaultStringValue("Min schema version")
    String minVersion();

    @DefaultStringValue("Max schema version")
    String maxVersion();

    @DefaultStringValue("Schema")
    String schema();

    @DefaultStringValue("Select schema file")
    String selectSchemaFile();

    @DefaultStringValue("Active")
    String active();

    @DefaultStringValue("Draft")
    String draft();

    @DefaultStringValue("Schema version")
    String schemaVersion();

    @DefaultStringValue("Date/Time modified")
    String dateTimeModified();

    @DefaultStringValue("Date/Time activated")
    String dateTimeActivated();

    @DefaultStringValue("Date/Time deactivated")
    String dateTimeDectivated();

    @DefaultStringValue("Last modified by")
    String lastModifiedBy();

    @DefaultStringValue("Activated by")
    String activatedBy();

    @DefaultStringValue("Deactivated by")
    String deactivatedBy();

    @DefaultStringValue("Body")
    String body();

    @DefaultStringValue("Activate")
    String activate();

    @DefaultStringValue("Deactivate")
    String deactivate();

    @DefaultStringValue("Tenant name")
    String tenantName();

    @DefaultStringValue("Tenant user")
    String tenantUser();

    @DefaultStringValue("Add new tenant")
    String addNewTenant();

    @DefaultStringValue("Tenant")
    String tenant();

    @DefaultStringValue("Tenant details")
    String tenantDetails();

    @DefaultStringValue("Tenant admin username")
    String tenantAdminUsername();

    @DefaultStringValue("Tenant admin email")
    String tenantAdminEmail();

    @DefaultStringValue("Mandatory")
    String mandatory();
    
    @DefaultStringValue("Confirm delivery")
    String confirmDelivery();    

    @DefaultStringValue("Remove")
    String remove();

    @DefaultStringValue("Add new notification topic")
    String addNewNotificationTopic();

    @DefaultStringValue("Notification topic details")
    String notificationTopicDetails();

    @DefaultStringValue("Username")
    String userName();

    @DefaultStringValue("Email")
    String email();

    @DefaultStringValue("Role")
    String role();

    @DefaultStringValue("Add new user")
    String addNewUser();

    @DefaultStringValue("User")
    String user();

    @DefaultStringValue("User details")
    String userDetails();

    @DefaultStringValue("Account role")
    String accountRole();

    @DefaultStringValue("Account profile")
    String accountProfile();

    @DefaultStringValue("First name")
    String firstName();

    @DefaultStringValue("Last name")
    String lastName();

    @DefaultStringValue("Add new ECF")
    String addNewEcf();

    @DefaultStringValue("Event class family")
    String ecf();

    @DefaultStringValue("Event class family details")
    String ecfDetails();

    @DefaultStringValue("Add schema")
    String addSchema();

    @DefaultStringValue("Add event class family schema")
    String addEcfSchema();

    @DefaultStringValue("FQN")
    String fqn();

    @DefaultStringValue("Namespace")
    String namespace();

    @DefaultStringValue("Class name")
    String className();

    @DefaultStringValue("Class type")
    String classType();

    @DefaultStringValue("Event")
    String event();

    @DefaultStringValue("Object")
    String object();

    @DefaultStringValue("Event class schema")
    String eventClassSchema();

    @DefaultStringValue("Event class family schema")
    String ecfSchema();

    @DefaultStringValue("Event class family schema details")
    String ecfSchemaDetails();

    @DefaultStringValue("Event classes")
    String eventClasses();

    @DefaultStringValue("Event class family name")
    String ecfName();

    @DefaultStringValue("Event class family version")
    String ecfVersion();

    @DefaultStringValue("Source")
    String source();

    @DefaultStringValue("Sink")
    String sink();

    @DefaultStringValue("Both")
    String both();

    @DefaultStringValue("Event family mappings")
    String aefMaps();

    @DefaultStringValue("Event family mapping")
    String aefMap();

    @DefaultStringValue("Event family mapping details")
    String aefMapDetails();

    @DefaultStringValue("Add new event family mapping")
    String addNewAefMap();

    @DefaultStringValue("Event map")
    String eventMap();

    @DefaultStringValue("Available")
    String available();

    @DefaultStringValue("Selected")
    String selected();

    @DefaultStringValue("Log Appenders")
    String logAppenders();

    @DefaultStringValue("Log Appender")
    String logAppender();

    @DefaultStringValue("Log Appender Type")
    String logAppenderType();

    @DefaultStringValue("Add new log appender")
    String addNewLogAppender();

    @DefaultStringValue("Add log appender")
    String addLogAppender();

    @DefaultStringValue("Log Appender details")
    String logAppenderDetails();

    @DefaultStringValue("Log Appender Status")
    String logAppenderStatus();

    @DefaultStringValue("Log Metadata")
    String logMetada();

    @DefaultStringValue("Hosts balancing")
    String logAppenderHostsBalancing();

    @DefaultStringValue("Add host")
    String addHost();

    @DefaultStringValue("Remove host")
    String remHost();

    @DefaultStringValue("Host")
    String host();

    @DefaultStringValue("Port")
    String port();

    @DefaultStringValue("Priority")
    String priority();

    @DefaultStringValue("Library")
    String downloadRecordLibrary();

    @DefaultStringValue("Schema")
    String downloadRecordSchema();
    
    @DefaultStringValue("Infinite")
    String infinite();
    
    @DefaultStringValue("Session Expired")
    String sessionExpired();
    
    @DefaultStringValue("Log in again")
    String logInAgain();

    @DefaultStringValue("Ignore")
    String ignore();

    @DefaultStringValue("General Settings")
    String generalSettings();
    
    @DefaultStringValue("General Settings Details")
    String generalSettingsDetails();

    @DefaultStringValue("Outgoing Mail")
    String outgoingMail();
    
    @DefaultStringValue("Outgoing Mail Settings")
    String outgoingMailSettings();

    @DefaultStringValue("SMTP Mail Server Details")
    String smtpMailServerDetails();
    
    @DefaultStringValue("Upload from file")
    String uploadFromFile();
    
    @DefaultStringValue("Upload")
    String upload();
    
    @DefaultStringValue("Notification body")
    String notificationBody();
    
    @DefaultStringValue("Notification details")
    String notificationDetails();

    @DefaultStringValue("Incompatible Server Version")
    String incompatibleRemoteService();
    
    @DefaultStringValue("Server is unreachable")
    String serverIsUnreachable();
    
    @DefaultStringValue("Type")
    String type();
    
    @DefaultStringValue("Verifier token")
    String verifierToken();
    
    @DefaultStringValue("Add user verifier")
    String addUserVerifier();
    
    @DefaultStringValue("User Verifier")
    String userVerifier();
    
    @DefaultStringValue("User Verifiers")
    String userVerifiers();
    
    @DefaultStringValue("Add new user verifier")
    String addNewUserVerifier();

    @DefaultStringValue("User Verifier details")
    String userVerifierDetails();
    
    @DefaultStringValue("Default User Verifier")
    String defaultUserVerifier();
    
    @DefaultStringValue("SDK details")
    String sdkDetails();
    
}