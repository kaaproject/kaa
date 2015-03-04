/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License") = 0;
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

#ifndef IENDPOINTREGISTRATIONMANAGER_HPP_
#define IENDPOINTREGISTRATIONMANAGER_HPP_

#ifdef KAA_USE_EVENTS

#include <string>

namespace kaa {

class IUserAttachCallback;
class IAttachStatusListener;
class IAttachEndpointCallback;
class IDetachEndpointCallback;

/**
 * @brief The interface to a module which associates endpoints with users.
 *
 * @b NOTE: According to the Kaa architecture, endpoints must be associated with the same user in order to be able
 * to communicate with each other.
 */
class IEndpointRegistrationManager
{
public:

    /**
     * @brief Attaches the specified endpoint to the user to which the current endpoint is attached.
     *
     * @param[in]   endpointAccessToken    The access token of the endpoint to be attached to the user.
     * @param[in]   listener               The optional listener to notify of the result.
     *
     * @throw KaaException
     * @throw TransportNotFoundException
     */
    virtual void attachEndpoint(const std::string&  endpointAccessToken
                              , IAttachEndpointCallback* listener = nullptr) = 0;

    /**
     * @brief Detaches the specified endpoint from the user to which the current endpoint is attached.
     *
     * @param[in]   endpointKeyHash    The key hash of the endpoint to be detached from the user.
     * @param[in]   listener           The optional listener to notify of the result.
     *
     * @throw KaaException
     * @throw TransportNotFoundException
     */
    virtual void detachEndpoint(const std::string&  endpointKeyHash
                              , IDetachEndpointCallback* listener = nullptr) = 0;

    /**
     * @brief Attaches the current endpoint to the specifier user. The user verification is carried out by the default verifier.
     *
     * @b NOTE: If the default user verifier (@link DEFAULT_USER_VERIFIER_TOKEN @endlink) is not specified,
     * the attach attempt fails with the @c KaaException exception.
     *
     * <b>Only endpoints associated with the same user can exchange events.</b>
     *
     * @param userExternalId    The external user ID.
     * @param userAccessToken   The user access token.
     *
     * @throw KaaException
     * @throw TransportNotFoundException
     */
    virtual void attachUser(const std::string& userExternalId
                          , const std::string& userAccessToken
                          , IUserAttachCallback* listener = nullptr) = 0;

    /**
     * @brief Attaches the endpoint to a user entity. The user verification will be carried out by the specified verifier.
     *
     * <b>Only endpoints associated with the same user can exchange events.</b>
     *
     * @param userExternalId             The external user ID.
     * @param userAccessToken            The user access token.
     * @param[in]   userVerifierToken    The user verifier token.
     * @param[in]   listener             The optional listener to notify of the result.
     *
     * @throw KaaException
     * @throw TransportNotFoundException
     */
    virtual void attachUser(const std::string& userExternalId
                          , const std::string& userAccessToken
                          , const std::string& userVerifierToken
                          , IUserAttachCallback* listener = nullptr) = 0;

    /**
     * @brief Sets listener to notify of the current endpoint is attached/detached by another one.
     *
     * @param[in]   listener    Listener to notify of the attach status is changed.
     */
    virtual void setAttachStatusListener(IAttachStatusListener* listener) = 0;

    /**
     * @brief Checks if the current endpoint is already attached to some user.
     *
     * @return TRUE if the current endpoint is attached, FALSE otherwise.
     */
    virtual bool isAttachedToUser() = 0;

    virtual ~IEndpointRegistrationManager() {}
};

}

#endif

#endif /* IENDPOINTREGISTRATIONMANAGER_HPP_ */
