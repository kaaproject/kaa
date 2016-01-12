package org.kaaproject.kaa.server.common.dao.lock;

import org.springframework.dao.OptimisticLockingFailureException;

public class KaaOptimisticLockingFailureException extends OptimisticLockingFailureException {

    public KaaOptimisticLockingFailureException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public KaaOptimisticLockingFailureException(String msg) {
        super(msg);
    }
}
