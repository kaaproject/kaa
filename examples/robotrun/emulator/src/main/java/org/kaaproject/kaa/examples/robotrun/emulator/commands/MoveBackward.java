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

/**
 * @author Andriy Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class MoveBackward extends Movement {

    /**
     * @param sleep
     * @param callback
     */
    public MoveBackward(long sleep, OperationCallback callback) {
        super(sleep, callback);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.examples.robotrun.emulator.commands.Movement#operationsComplete()
     */
    @Override
    protected void operationsComplete() {
        // TODO Auto-generated method stub

    }

}
