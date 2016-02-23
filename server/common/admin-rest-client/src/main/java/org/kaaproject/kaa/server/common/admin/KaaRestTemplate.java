package org.kaaproject.kaa.server.common.admin;

import org.apache.http.HttpHost;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.*;
import org.springframework.web.util.UriTemplate;

import java.net.URI;

/**
 * Created by Chyzhevskyi Volodymyr on 19.02.16.
 */
public class KaaRestTemplate extends RestTemplate {

    private String[] hosts;

    private int[] ports;

    private String host;

    private int port;

    private String newUrl;

    private ClientHttpRequestFactory requestFactory;

    public String getUrl(){
        return newUrl;
    }

    public KaaRestTemplate(String[] hosts, int[] ports) {
        if(hosts.length != ports.length){

            throw new IllegalArgumentException("Length of arrays of hosts and ports must be the same length");

        }else {

            this.newUrl ="http://" + hosts[0] + ":" + ports[0] + "/kaaAdmin/rest/api/";
            this.hosts = hosts;
            this.ports = ports;

            requestFactory = new HttpComponentsRequestFactoryBasicAuth(new HttpHost(hosts[0], ports[0], "http"));
            setRequestFactory(requestFactory);
        }
    }

    public KaaRestTemplate(String host, int port) {

        this.newUrl = "http://" + host + ":" + port + "/kaaAdmin/rest/api/";

        this.host = host;
        this.port = port;

        requestFactory = new HttpComponentsRequestFactoryBasicAuth(new HttpHost(host, port, "http"));
        setRequestFactory(requestFactory);
    }

    public String getNewHost(){
        return hosts[count];
    }

    public int getNewPort(){
        return ports[count];
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    private int count = 0;

    @Override
    protected <T> T doExecute(URI url, HttpMethod method, RequestCallback requestCallback,
                              ResponseExtractor<T> responseExtractor) {
        try {
            super.doExecute(url, method, requestCallback, responseExtractor);
        }catch (ResourceAccessException ex) {

            count++;
            if(count >= hosts.length) {
                count = 0;
            }

            newUrl = "http://" + hosts[count] + ":" + ports[count] + "/kaaAdmin/rest/api/";

            requestFactory = new HttpComponentsRequestFactoryBasicAuth(new HttpHost(hosts[count], ports[count], "http"));
            setRequestFactory(requestFactory);

            String urlWitthNewHostAndPort = url.toString();
            urlWitthNewHostAndPort = urlWitthNewHostAndPort.replace(url.getHost(), hosts[count]);
            urlWitthNewHostAndPort = urlWitthNewHostAndPort.replace(String.valueOf(url.getPort()), String.valueOf(ports[count]));

            Object[] obj = {};

            URI FullUrl = new UriTemplate(urlWitthNewHostAndPort).expand(obj);

            super.doExecute(FullUrl, method, requestCallback, responseExtractor);

        }
        return null;
    }

}
