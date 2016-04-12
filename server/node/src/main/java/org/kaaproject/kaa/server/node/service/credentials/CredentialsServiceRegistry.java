package org.kaaproject.kaa.server.node.service.credentials;

import java.util.List;

public interface CredentialsServiceRegistry {
    
    /**
     * Returns the names of credentials services configured. This method is used
     * to set acceptable values of the listbox used to specify a credentials
     * service for an application via the Admin UI.
     *
     * The default implementation loads all credentials services configured as
     * Spring beans and returns their names.
     *
     * @return The names of credentials services configured
     */
    List<String> getCredentialsServiceNames();
}
