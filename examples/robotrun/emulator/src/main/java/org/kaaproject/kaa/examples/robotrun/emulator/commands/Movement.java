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

/**
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
package org.kaaproject.kaa.examples.robotrun.emulator.commands;

import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.OperationCallback;
import org.kaaproject.kaa.examples.robotrun.controller.robot.callbacks.OperationStatus;

/**
 * Abstract class for complete movement operations in emulator.
 * 
 * @author Andriy Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public abstract class Movement implements Runnable {

    private final long sleep;
    
    private final OperationCallback callback;
    
    /**
     * Default constructor.
     * @param sleep long - timeout necessary to emulate operation.
     * @param callback OperationCallback - callback to notify operation complete.
     */
    public Movement(long sleep, OperationCallback callback) {
        this.sleep = sleep;
        this.callback = callback;
    }
    
    /**
     * Method to implement specific movment operations.
     */
    abstract protected void operationsComplete();
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            Thread.sleep(sleep);
            operationsComplete();
            callback.complete(OperationStatus.SUCESSFULL);
        } catch (InterruptedException e) {
            callback.complete(OperationStatus.FAILED);
        }
    }

}
