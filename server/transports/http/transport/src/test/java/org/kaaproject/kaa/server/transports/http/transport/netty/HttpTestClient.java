/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.transports.http.transport.netty;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * HTTP Test Client Class.
 * 
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class HttpTestClient implements Runnable {

    private HttpActivity activity;
    private URLConnection connection;
    private PostParameters params;

    public HttpTestClient(PostParameters params, HttpActivity activity, String commandName) throws MalformedURLException, IOException {
        this.activity = activity;
        connection = new URL("http://" + NettyHttpServerIT.TEST_HOST + ":" + NettyHttpServerIT.TEST_PORT + "/domain/" + commandName)
                .openConnection();
        this.params = params;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        IOException error = null;
        connection.setDoOutput(true);
        try {
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "ASCII");
            out.write(params.toString());
            out.write("\r\n");
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            error = e;
        }
        StringBuffer b = new StringBuffer();
        try {
            InputStreamReader r = new InputStreamReader(connection.getInputStream(), "UTF-8");
            int c;
            while ((c = r.read()) != -1) {
                b.append((char) c);
            }
        } catch (IOException e) {
            e.printStackTrace();
            error = e;
        }
        if (activity != null) {
            String body = b.toString();
            activity.httpRequestComplete(error, connection.getHeaderFields(), body);
        }
    }

}
