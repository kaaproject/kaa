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

package org.kaaproject.kaa.server.common.verifier;

/**
 * User verification callback. Helps to identify verification status and
 * possible reason failure
 * 
 * @author Andrew Shvayka
 *
 */
public interface UserVerifierCallback {

    /**
     * On successful verification.
     */
    void onSuccess();

    /**
     * Failed verification due to invalid token
     */
    void onTokenInvalid();

    /**
     * Failed verification due to outdated token
     */
    void onTokenExpired();

    /**
     * Failed verification due to specified reason
     * 
     * @param reason
     *            - reason of failure
     */
    void onVerificationFailure(String reason);

    /**
     * Failed verification due to internal error
     */
    void onInternalError();

    /**
     * Failed verification due to internal error
     */
    void onInternalError(String reason);

    /**
     * Failed verification due to connection error
     */
    void onConnectionError();

    /**
     * Failed verification due to connection error
     */
    void onConnectionError(String reason);

    /**
     * Failed verification due to remote authentication service error
     */
    void onRemoteError();

    /**
     * Failed verification due to remote authentication service error
     */
    void onRemoteError(String reason);

}
