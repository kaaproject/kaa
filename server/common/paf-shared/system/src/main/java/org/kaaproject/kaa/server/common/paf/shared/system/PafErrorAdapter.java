package org.kaaproject.kaa.server.common.paf.shared.system;

import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.ErrorMessage;

public interface PafErrorAdapter extends GenericTransformer<ErrorMessage, Message<byte[]>> {

}
