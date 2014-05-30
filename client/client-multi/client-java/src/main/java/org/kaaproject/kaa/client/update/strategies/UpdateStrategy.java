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

package org.kaaproject.kaa.client.update.strategies;

import org.kaaproject.kaa.client.update.commands.Command;

/**
 * Strategy for obtaining updates (i.e. polling, long poll, etc.).
 *
 * @author Yaroslav Zeygerman
 *
 */
public interface UpdateStrategy {

    /**
     * Starts polling.
     *
     */
    void startPoll();

    /**
     * Stops polling. All running tasks will be interrupted.
     *
     */
    void stopPoll();

    /**
     * Stops current tasks and retries the given command.
     *
     * @param milliseconds time to wait before executing the command.
     * @param cmd command to execute.
     *
     */
    public void retryCommand(Long milliseconds, Command cmd);

    /**
     * Executes command.
     *
     * @param cmd command to execute.
     *
     */
    void executeCommand(Command cmd);
}
