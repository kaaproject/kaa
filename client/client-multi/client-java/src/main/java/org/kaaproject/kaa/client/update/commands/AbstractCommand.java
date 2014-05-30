/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.client.update.commands;

import org.kaaproject.kaa.client.TransportExceptionHandler;
import org.kaaproject.kaa.client.transport.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract {@link Command} implementation which supports {@link TransportException} handling.
 *
 * @author Yaroslav Zeygerman
 *
 */
public abstract class AbstractCommand implements Command {
    
    /** The Constant logger. */
    public static final Logger LOG = LoggerFactory //NOSONAR
            .getLogger(AbstractCommand.class);

    final TransportExceptionHandler handler;

    public AbstractCommand(TransportExceptionHandler handler) {
        this.handler = handler;
    }

    /**
     * Abstract execution method.
     *
     */
    protected abstract void doExecute() throws TransportException;

    @Override
    public void execute() {
        try {
            doExecute();
        } catch (TransportException e) {
            LOG.error("Transport Exception!", e);
            handler.onTransportException();
        }
    }

}
