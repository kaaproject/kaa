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
 * Represents process of access token verification of endpoint user.
 * 
 * @author Andrew Shvayka
 * 
 */
public interface UserVerifier {

    /**
     * Initialize a user verifier instance with a particular configuration and
     * common transport properties. The configuration is a serialized Avro
     * object. The serialization is done using the schema specified in
     * {@link KaaUserVerifierConfig}.
     *
     * @param context
     *            the user verifier initialization context
     * @throws UserVerifierLifecycleException
     */
    void init(UserVerifierContext context) throws UserVerifierLifecycleException;
    
    /**
     * Verifies the access token.
     *
     * @param userExternalId the user external id
     * @param accessToken the access token
     * @return true, if verified
     */
    void checkAccessToken(String userExternalId, String accessToken, UserVerifierCallback callback);

    /**
     * Starts a user verifier instance. This method should block its caller thread
     * until the user verifier is started. This method should not block its caller
     * thread after startup sequence is successfully completed.
     */
    void start();

    /**
     * Stops the user verifier instance. This method should block its current thread
     * until user verifier is stopped. User verifier may be started again after it is
     * stopped.
     */
    void stop();
}
