package org.kaaproject.kaa.server.admin.client.i18n;

/**
 * Interface to represent the messages contained in resource bundle:
 */
public interface KaaAdminMessages extends com.google.gwt.i18n.client.Messages {
  
  /**
   * Translated "Are you sure you want to delete the selected entry?".
   * 
   * @return translated "Are you sure you want to delete the selected entry?"
   */
  @DefaultMessage("Are you sure you want to delete the selected entry?")
  @Key("deleteSelectedEntryQuestion")
  String deleteSelectedEntryQuestion();

  /**
   * Translated "Delete entry".
   * 
   * @return translated "Delete entry"
   */
  @DefaultMessage("Delete entry")
  @Key("deleteSelectedEntryTitle")
  String deleteSelectedEntryTitle();

  /**
   * Translated "You have unsaved changes on this form. If you navigate away from this page without first saving, all changes will be lost.".
   * 
   * @return translated "You have unsaved changes on this form. If you navigate away from this page without first saving, all changes will be lost."
   */
  @DefaultMessage("You have unsaved changes on this form. If you navigate away from this page without first saving, all changes will be lost.")
  @Key("detailsMayStopMessage")
  String detailsMayStopMessage();

  /**
   * Translated "Username and password can not be empty!".
   * 
   * @return translated "Username and password can not be empty!"
   */
  @DefaultMessage("Username and password can not be empty!")
  @Key("emptyUsernameOrPassword")
  String emptyUsernameOrPassword();

  /**
   * Translated "This client is not compatible with the server. Cleanup and refresh the browser.".
   * 
   * @return translated "This client is not compatible with the server. Cleanup and refresh the browser."
   */
  @DefaultMessage("This client is not compatible with the server. Cleanup and refresh the browser.")
  @Key("incompatibleRemoteService")
  String incompatibleRemoteService();

  /**
   * Translated "Incorrect configuration. Validate your configuration regarding schema version.".
   * 
   * @return translated "Incorrect configuration. Validate your configuration regarding schema version."
   */
  @DefaultMessage("Incorrect configuration. Validate your configuration regarding schema version.")
  @Key("incorrectConfiguration")
  String incorrectConfiguration();

  /**
   * Translated "This is the first time login.<br>Please enter Kaa administrator username and password then click ''Login'' to register.".
   * 
   * @return translated "This is the first time login.<br>Please enter Kaa administrator username and password then click ''Login'' to register."
   */
  @DefaultMessage("This is the first time login.<br>Please enter Kaa administrator username and password then click ''Login'' to register.")
  @Key("kaaAdminNotExists")
  String kaaAdminNotExists();

  /**
   * Translated "<h1 title=\"Please login\">Please login</h1>".
   * 
   * @return translated "<h1 title=\"Please login\">Please login</h1>"
   */
  @DefaultMessage("<h1 title=\"Please login\">Please login</h1>")
  @Key("loginTitle")
  String loginTitle();

  /**
   * Translated "New password should be different".
   * 
   * @return translated "New password should be different"
   */
  @DefaultMessage("New password should be different")
  @Key("newPasswordShouldDifferent")
  String newPasswordShouldDifferent();

  /**
   * Translated "Entered passwords do not match".
   * 
   * @return translated "Entered passwords do not match"
   */
  @DefaultMessage("Entered passwords do not match")
  @Key("newPasswordsNotMatch")
  String newPasswordsNotMatch();

  /**
   * Translated "Page {0} of {1}".
   * 
   * @return translated "Page {0} of {1}"
   */
  @DefaultMessage("Page {0} of {1}")
  @Key("pagerText")
  String pagerText(String arg0,  String arg1);

  /**
   * Translated "Your password has been reset. You should receive mail with new temporary password.".
   * 
   * @return translated "Your password has been reset. You should receive mail with new temporary password."
   */
  @DefaultMessage("Your password has been reset. You should receive mail with new temporary password.")
  @Key("passwordWasReset")
  String passwordWasReset();

  /**
   * Translated "Are you sure you want to delete selected log appender?".
   * 
   * @return translated "Are you sure you want to delete selected log appender?"
   */
  @DefaultMessage("Are you sure you want to delete selected log appender?")
  @Key("removeLogAppenderQuestion")
  String removeLogAppenderQuestion();

  /**
   * Translated "Remove log appender".
   * 
   * @return translated "Remove log appender"
   */
  @DefaultMessage("Remove log appender")
  @Key("removeLogAppenderTitle")
  String removeLogAppenderTitle();

  /**
   * Translated "Are you sure you want to unassign selected notification topic from endpoint group?".
   * 
   * @return translated "Are you sure you want to unassign selected notification topic from endpoint group?"
   */
  @DefaultMessage("Are you sure you want to unassign selected notification topic from endpoint group?")
  @Key("removeTopicFromEndpointGroupQuestion")
  String removeTopicFromEndpointGroupQuestion();

  /**
   * Translated "Unassign notification topic".
   * 
   * @return translated "Unassign notification topic"
   */
  @DefaultMessage("Unassign notification topic")
  @Key("removeTopicFromEndpointGroupTitle")
  String removeTopicFromEndpointGroupTitle();

  /**
   * Translated "Are you sure you want to delete selected user verifier?".
   * 
   * @return translated "Are you sure you want to delete selected user verifier?"
   */
  @DefaultMessage("Are you sure you want to delete selected user verifier?")
  @Key("removeUserVerifierQuestion")
  String removeUserVerifierQuestion();

  /**
   * Translated "Remove user verifier".
   * 
   * @return translated "Remove user verifier"
   */
  @DefaultMessage("Remove user verifier")
  @Key("removeUserVerifierTitle")
  String removeUserVerifierTitle();

  /**
   * Translated "Fields marked with <span class=\"{0}\"></span> are mandatory.".
   * 
   * @return translated "Fields marked with <span class=\"{0}\"></span> are mandatory."
   */
  @DefaultMessage("Fields marked with <span class=\"{0}\"></span> are mandatory.")
  @Key("requiredFieldsNote")
  String requiredFieldsNote(String arg0);

  /**
   * Translated "Email has been sent with further instruction to reset your password.".
   * 
   * @return translated "Email has been sent with further instruction to reset your password."
   */
  @DefaultMessage("Email has been sent with further instruction to reset your password.")
  @Key("resetPasswordLinkWasSent")
  String resetPasswordLinkWasSent();

  /**
   * Translated "Please enter existing username or email in order to reset password.".
   * 
   * @return translated "Please enter existing username or email in order to reset password."
   */
  @DefaultMessage("Please enter existing username or email in order to reset password.")
  @Key("resetPasswordMessage")
  String resetPasswordMessage();

  /**
   * Translated "An error occurred while communicating with the server. Possible causes are:<br>a) Server is not running, or <br>b) Network problem.<br>Check your network connection or try again later.".
   * 
   * @return translated "An error occurred while communicating with the server. Possible causes are:<br>a) Server is not running, or <br>b) Network problem.<br>Check your network connection or try again later."
   */
  @DefaultMessage("An error occurred while communicating with the server. Possible causes are:<br>a) Server is not running, or <br>b) Network problem.<br>Check your network connection or try again later.")
  @Key("serverIsUnreacheableMessage")
  String serverIsUnreacheableMessage();

  /**
   * Translated "It looks like your session has timed out, or you have been logged out of site. You will need to log back in to continue.".
   * 
   * @return translated "It looks like your session has timed out, or you have been logged out of site. You will need to log back in to continue."
   */
  @DefaultMessage("It looks like your session has timed out, or you have been logged out of site. You will need to log back in to continue.")
  @Key("sessionExpiredMessage")
  String sessionExpiredMessage();

  /**
   * Translated "Current password is temporary. Please change your password.".
   * 
   * @return translated "Current password is temporary. Please change your password."
   */
  @DefaultMessage("Current password is temporary. Please change your password.")
  @Key("tempCredentials")
  String tempCredentials();

  /**
   * Translated "Unexpected error occurred".
   * 
   * @return translated "Unexpected error occurred"
   */
  @DefaultMessage("Unexpected error occurred")
  @Key("unexpectedError")
  String unexpectedError();
}
