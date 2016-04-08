package org.kaaproject.kaa.server.common.paf.shared.system;

import org.kaaproject.kaa.server.common.paf.shared.common.AbstractPafBean;
import org.kaaproject.kaa.server.common.paf.shared.common.exception.PafMessagingException;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.ErrorMessage;

public abstract class AbstractPafErrorAdapter extends AbstractPafBean implements PafErrorAdapter {
    
    @Override
    public Message<byte[]> transform(ErrorMessage source) {
        Throwable rootCause = findRootCause(source.getPayload());
        if (rootCause instanceof PafMessagingException) {
            PafMessagingException pafException = (PafMessagingException) rootCause;
            return fromPafException(source, pafException);
        } else {
            return fromGenericException(source, rootCause);
        }
    }
    
    private Throwable findRootCause(Throwable t) {
         if (t.getCause() != null){
            return findRootCause(t.getCause());
        } else {
            return t;
        }
    }
    
    protected abstract Message<byte[]> fromPafException(ErrorMessage source, PafMessagingException e);
    
    protected abstract Message<byte[]> fromGenericException(ErrorMessage source, Throwable e);
    
}
