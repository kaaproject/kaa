package org.kaaproject.kaa.demo.iotworld.irrigation.web;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.kaaproject.kaa.demo.iotworld.irrigation.qrcode.QRCodeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class MiniWebServer {

    private static final Logger LOG = LoggerFactory.getLogger(MiniWebServer.class);

    private static final String QRCODE_URL = "/qrcode";
    private static final String RESPONSE_TYPE = "image/png";

    private Route qrcodeRoute;
    private final QRCodeManager qrCodeManager;

    public MiniWebServer(int port) {
        LOG.info("Init webserver with port {}", port);
        Spark.setPort(port);
        this.qrCodeManager = new QRCodeManager();
    }

    public void start(final String acesstoken) {
        qrcodeRoute = new Route(QRCODE_URL) {
            @Override
            public Object handle(Request request, Response response) {
                response.type(RESPONSE_TYPE);
                HttpServletResponse rawResponse = response.raw();
                try (OutputStream out = rawResponse.getOutputStream();) {
                    qrCodeManager.writeQRCodeImageBuffer(acesstoken, out);
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    LOG.error("Got error during get request", e);
                }
                response.status(HttpServletResponse.SC_OK);
                return response;
            }
        };
        Spark.get(qrcodeRoute);
    }
}
