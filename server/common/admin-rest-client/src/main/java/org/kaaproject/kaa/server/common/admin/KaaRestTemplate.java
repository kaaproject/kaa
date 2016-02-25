package org.kaaproject.kaa.server.common.admin;

import org.apache.http.HttpHost;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.*;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.Random;

/**
 * Created by Chyzhevskyi Volodymyr on 19.02.16.
 */
public class KaaRestTemplate extends RestTemplate {

    private String[] hosts;

    private int[] ports;

    private String newUrl;

    private String password;

    private String username;

    private int index;

    public KaaRestTemplate(String[] hosts, int[] ports) {
        if((hosts.length != ports.length) && (hosts!=null)){

            throw new IllegalArgumentException("Length of arrays of hosts and ports must be the same length and not null");

        }else {

            this.newUrl ="http://" + hosts[0] + ":" + ports[0];
            this.hosts = hosts;
            this.ports = ports;

            setNewRequestFactory(new HttpHost(hosts[0], ports[0], "http"));

            index = new Random().nextInt(hosts.length);
        }
    }

    public KaaRestTemplate(String host, int port) {
        this(new String[]{host}, new int[]{port});
    }

    public String getUrl(){
        return newUrl;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    protected <T> T doExecute(URI url, HttpMethod method, RequestCallback requestCallback,
                              ResponseExtractor<T> responseExtractor) {
        try {
            super.doExecute(url, method, requestCallback, responseExtractor);
        }catch (ResourceAccessException ex) {

            if(username != null && password != null) {
                HttpComponentsRequestFactoryBasicAuth requestFactory = (HttpComponentsRequestFactoryBasicAuth) getRequestFactory();
                requestFactory.setCredentials(username, password);
            }

            index++;
            if(index >= hosts.length) {
                index = 0;
            }

            newUrl = "http://" + hosts[index] + ":" + ports[index];

            setNewRequestFactory(new HttpHost(hosts[index], ports[index], "http"));

            String urlWitthNewHostAndPort = url.toString();
            urlWitthNewHostAndPort = urlWitthNewHostAndPort.replaceFirst(url.getHost(), hosts[index]);
            urlWitthNewHostAndPort = urlWitthNewHostAndPort.replaceFirst(String.valueOf(url.getPort()), String.valueOf(ports[index]));

            URI FullUrl = URI.create(urlWitthNewHostAndPort);

            super.doExecute(FullUrl, method, requestCallback, responseExtractor);

        }
        return null;
    }

    private void setNewRequestFactory(HttpHost http) {
        ClientHttpRequestFactory requestFactory = new HttpComponentsRequestFactoryBasicAuth(http);
        setRequestFactory(requestFactory);
    }

    public void setUsernamePassword(String username, String password){
        this.username = username;
        this.password = password;
    }

}
