package org.kaaproject.kaa.server.paf.adapters.sample.system;

import org.kaaproject.kaa.server.common.paf.shared.common.exception.PafErrorCode;
import org.kaaproject.kaa.server.common.paf.shared.common.exception.PafMessagingException;
import org.kaaproject.kaa.server.common.paf.shared.system.AbstractPafErrorAdapter;
import org.springframework.http.HttpStatus;
import org.springframework.integration.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.messaging.support.MessageBuilder;

public class SamplePafErrorAdapter extends AbstractPafErrorAdapter {

    @Override
    protected Message<byte[]> fromPafException(ErrorMessage source, PafMessagingException e) {
        PafErrorCode errorCode = e.getErrorCode();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        switch (errorCode) {
        case ENDPOINT_REGISTRATION_FAILED:
        case MISSING_ATTRIBUTE:
            status = HttpStatus.BAD_REQUEST;
            break;
        case NOT_IMPLEMENTED:
            status = HttpStatus.NOT_IMPLEMENTED;
            break;
        case OK:
            status = HttpStatus.OK;
            break;
        default:
            break;
        }
        return MessageBuilder.withPayload(e.getMessage().getBytes())
                .copyHeaders(source.getHeaders())
                .setHeader(HttpHeaders.STATUS_CODE, status).build();
    }

    @Override
    protected Message<byte[]> fromGenericException(ErrorMessage source, Throwable e) {
        return MessageBuilder.withPayload(e.getMessage().getBytes())
                .copyHeaders(source.getHeaders())
                .setHeader(HttpHeaders.STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

}
