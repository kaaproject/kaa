package org.kaaproject.kaa.sandbox.web.client.i18n;

/**
 * Interface to represent the messages contained in resource bundle:
 * 	/kaa/sandbox/web/src/main/java/org/kaaproject/kaa/sandbox/web/client/i18n/SandboxMessages.properties'.
 */
public interface SandboxMessages extends com.google.gwt.i18n.client.Messages {
  
  /**
   * Translated "In order to make Kaa services accessible via network<br>it is recommended to change Kaa services host/ip according to machine network configuration.<br>You can ignore this message and change host/ip later by navigating to Settings->Change Kaa host/ip. ".
   * 
   * @return translated "In order to make Kaa services accessible via network<br>it is recommended to change Kaa services host/ip according to machine network configuration.<br>You can ignore this message and change host/ip later by navigating to Settings->Change Kaa host/ip. "
   */
  @DefaultMessage("In order to make Kaa services accessible via network<br>it is recommended to change Kaa services host/ip according to machine network configuration.<br>You can ignore this message and change host/ip later by navigating to Settings->Change Kaa host/ip. ")
  @Key("changeKaaHostDialogMessage")
  String changeKaaHostDialogMessage();

  /**
   * Translated "To change Kaa services host/ip enter new host<br>value in field below and click 'Change' button.".
   * 
   * @return translated "To change Kaa services host/ip enter new host<br>value in field below and click 'Change' button."
   */
  @DefaultMessage("To change Kaa services host/ip enter new host<br>value in field below and click 'Change' button.")
  @Key("changeKaaHostMessage")
  String changeKaaHostMessage();

  /**
   * Translated "{0} of {1} max characters".
   * 
   * @return translated "{0} of {1} max characters"
   */
  @DefaultMessage("{0} of {1} max characters")
  @Key("charactersLength")
  String charactersLength(String arg0,  String arg1);

  /**
   * Translated "Kaa host field can not be empty!".
   * 
   * @return translated "Kaa host field can not be empty!"
   */
  @DefaultMessage("Kaa host field can not be empty!")
  @Key("emptyKaaHostError")
  String emptyKaaHostError();

  /**
   * Translated "Powered by <b><a href=\"http://www.kaaproject.org\">Kaa IoT Application Plaform</a></b> {0} · <a href=\"http://jira.kaaproject.org/browse/KAA\">Report a bug</a> · <a href=\"https://docs.kaaproject.org/display/KAA\">Documentation</a>".
   * 
   * @return translated "Powered by <b><a href=\"http://www.kaaproject.org\">Kaa IoT Application Plaform</a></b> {0} · <a href=\"http://jira.kaaproject.org/browse/KAA\">Report a bug</a> · <a href=\"https://docs.kaaproject.org/display/KAA\">Documentation</a>"
   */
  @DefaultMessage("Powered by <b><a href=\"http://www.kaaproject.org\">Kaa IoT Application Plaform</a></b> {0} · <a href=\"http://jira.kaaproject.org/browse/KAA\">Report a bug</a> · <a href=\"https://docs.kaaproject.org/display/KAA\">Documentation</a>")
  @Key("footerMessage")
  String footerMessage(String arg0);

  /**
   * Translated "Page {0} of {1}".
   * 
   * @return translated "Page {0} of {1}"
   */
  @DefaultMessage("Page {0} of {1}")
  @Key("pagerText")
  String pagerText(String arg0,  String arg1);
}
