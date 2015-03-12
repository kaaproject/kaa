package org.kaaproject.kaa.sandbox.web.client.i18n;

/**
 * Interface to represent the messages contained in resource bundle:
 * 	/kaa/sandbox/web/src/main/java/org/kaaproject/kaa/sandbox/web/client/i18n/SandboxMessages.properties'.
 */
public interface SandboxMessages extends com.google.gwt.i18n.client.Messages {
  
  /**
   * Translated "{0} of {1} max characters".
   * 
   * @return translated "{0} of {1} max characters"
   */
  @DefaultMessage("{0} of {1} max characters")
  @Key("charactersLength")
  String charactersLength(String arg0,  String arg1);

  /**
   * Translated "Page {0} of {1}".
   * 
   * @return translated "Page {0} of {1}"
   */
  @DefaultMessage("Page {0} of {1}")
  @Key("pagerText")
  String pagerText(String arg0,  String arg1);
}
