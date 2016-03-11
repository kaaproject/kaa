package org.kaaproject.kaa.server.common.admin;

import org.apache.http.HttpHost;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.*;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Random;

/**
 * Created by Chyzhevskyi Volodymyr on 19.02.16.
 */
public class KaaRestTemplate extends RestTemplate {

    private String[] hosts;

    private int[] ports;

    private String currentUrl;

    private String username;

    private String password;

    private static final String restApiSuffix = "/kaaAdmin/rest/api/";

    private int index;

    public KaaRestTemplate(String[] hosts, int[] ports) {
        if((hosts.length != ports.length) && (hosts!=null)){

            throw new IllegalArgumentException("Length of arrays of hosts and ports must be the same length and not null");

        }else {

            this.currentUrl ="http://" + hosts[index] + ":" + ports[index] + restApiSuffix;
            this.hosts = hosts;
            this.ports = ports;

            initIndexAndRequestFactory();
        }
    }

    public KaaRestTemplate(String host, int port) {
        this(new String[]{host}, new int[]{port});
    }

    //host:port, host:port ...
    public KaaRestTemplate(String addresses) {
        if(addresses == null){
            throw new IllegalArgumentException("String of addresses must be not null");
        }

        String[] splitedAddresses = addresses.split(",");

        int lengthOfHostsAndPOrtsArrays = splitedAddresses.length;

        this.hosts = new String[lengthOfHostsAndPOrtsArrays];
        this.ports = new int[lengthOfHostsAndPOrtsArrays];

        for(int i=0; i < hosts.length; i++){
            String[] separatedAddresses = splitedAddresses[i].split(":");
            hosts[i] = separatedAddresses[0];
            ports[i] = Integer.parseInt(separatedAddresses[1]);
        }

        initIndexAndRequestFactory();
    }

    private void initIndexAndRequestFactory(){
        index = new Random().nextInt(hosts.length);
        setNewRequestFactory(new HttpHost(hosts[index], ports[index], "http"));
    }

    public String getUrl(){
        return currentUrl;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    protected <T> T doExecute(URI url, HttpMethod method, RequestCallback requestCallback,
                              ResponseExtractor<T> responseExtractor) throws ResourceAccessException {

        int maxRetry = hosts.length;

        while(true){

            try {

                super.doExecute(url, method, requestCallback, responseExtractor);

            }catch (Exception ex) {

                index++;

                if(maxRetry <= 0) {
                    throw new ResourceAccessException("I/O error on " + method.name() +
                            " request for \"" + url + "\":" + ex.getMessage(), (IOException) ex);
                }

                if(username != null && password != null) {
                    HttpComponentsRequestFactoryBasicAuth requestFactory = (HttpComponentsRequestFactoryBasicAuth) getRequestFactory();
                    requestFactory.setCredentials(username, password);
                }

                currentUrl = "http://" + hosts[index] + ":" + ports[index] + restApiSuffix;

                setNewRequestFactory(new HttpHost(hosts[index], ports[index], "http"));

                String currentErrorURI = url.toString();

                int indexOfDefaultPartOfURI = currentErrorURI.indexOf(restApiSuffix);

                String defaultURIPartWithVariableHostPort = currentErrorURI.substring(0, indexOfDefaultPartOfURI);
                String otherPart = currentErrorURI.substring(indexOfDefaultPartOfURI);

                defaultURIPartWithVariableHostPort = currentErrorURI.replaceFirst(url.getHost(), hosts[index]);
                defaultURIPartWithVariableHostPort = currentErrorURI.replaceFirst(String.valueOf(url.getPort()), String.valueOf(ports[index]));

                url = URI.create(defaultURIPartWithVariableHostPort + otherPart);

                maxRetry--;

            }
        }
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
