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

import org.kaaproject.kaa.common.dto.user.UserVerifierDto;


/**
 * Provides a context for verifier initialization parameters and {@link MessageHandler
 * }.
 * 
 * @author Andrew Shvayka
 *
 */
public class UserVerifierContext {

    private final UserVerifierDto verifierDto;

    public UserVerifierContext(UserVerifierDto verifierDto) {
        super();
        this.verifierDto = verifierDto;
    }

    public UserVerifierDto getVerifierDto() {
        return verifierDto;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserVerifierContext [verifierDto=");
        builder.append(verifierDto);
        builder.append("]");
        return builder.toString();
    }
}
