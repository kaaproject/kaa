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

#ifndef IENDPOINTREGISTRATIONMANAGER_HPP_
#define IENDPOINTREGISTRATIONMANAGER_HPP_


#include <list>
#include <string>

#include "kaa/event/registration/IUserAttachCallback.hpp"
#include "kaa/event/registration/IAttachStatusListener.hpp"
#include "kaa/event/registration/IAttachEndpointCallback.hpp"
#include "kaa/event/registration/IDetachEndpointCallback.hpp"

namespace kaa {

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
     * @param[in] endpointAccessToken    The access token of the endpoint to be attached to the user.
     * @param[in] listener               The optional listener to notify of the result.
     *
     * @throw BadCredentials                The endpoint access token is empty.
     * @throw TransportNotFoundException    The Kaa SDK isn't fully initialized.
     * @throw KaaException                  Some other failure has happened.
     */
    virtual void attachEndpoint(const std::string&  endpointAccessToken
                              , IAttachEndpointCallbackPtr listener = IAttachEndpointCallbackPtr()) = 0;

    /**
     * @brief Detaches the specified endpoint from the user to which the current endpoint is attached.
     *
     * @param[in] endpointKeyHash    The key hash of the endpoint to be detached from the user.
     * @param[in] listener           The optional listener to notify of the result.
     *
     * @throw BadCredentials                The endpoint access token is empty.
     * @throw TransportNotFoundException    The Kaa SDK isn't fully initialized.
     * @throw KaaException                  Some other failure has happened.
     */
    virtual void detachEndpoint(const std::string&  endpointKeyHash
                              , IDetachEndpointCallbackPtr listener = IDetachEndpointCallbackPtr()) = 0;

    /**
     * @brief Attaches the current endpoint to the specifier user. The user verification is carried out by the default verifier.
     *
     * @b NOTE: If the default user verifier (@link DEFAULT_USER_VERIFIER_TOKEN @endlink) is not specified,
     * the attach attempt fails with the @c KaaException exception.
     *
     * <b>Only endpoints associated with the same user can exchange events.</b>
     *
     * @param[in] userExternalId     The external user ID.
     * @param[in] userAccessToken    The user access token.
     *
     * @throw BadCredentials                The endpoint access token is empty.
     * @throw TransportNotFoundException    The Kaa SDK isn't fully initialized.
     * @throw KaaException                  Some other failure has happened.
     */
    virtual void attachUser(const std::string& userExternalId
                          , const std::string& userAccessToken
                          , IUserAttachCallbackPtr listener = IUserAttachCallbackPtr()) = 0;

    /**
     * @brief Attaches the endpoint to a user entity. The user verification will be carried out by the specified verifier.
     *
     * <b>Only endpoints associated with the same user can exchange events.</b>
     *
     * @param[in] userExternalId       The external user ID.
     * @param[in] userAccessToken      The user access token.
     * @param[in] userVerifierToken    The user verifier token.
     * @param[in] listener             The optional listener to notify of the result.
     *
     * @throw BadCredentials                The endpoint access token is empty.
     * @throw TransportNotFoundException    The Kaa SDK isn't fully initialized.
     * @throw KaaException                  Some other failure has happened.
     */
    virtual void attachUser(const std::string& userExternalId
                          , const std::string& userAccessToken
                          , const std::string& userVerifierToken
                          , IUserAttachCallbackPtr listener = IUserAttachCallbackPtr()) = 0;

    /**
     * @brief Sets listener to notify of the current endpoint is attached/detached by another one.
     *
     * @param[in] listener    Listener to notify of the attach status is changed.
     */
    virtual void setAttachStatusListener(IAttachStatusListenerPtr listener) = 0;

    /**
     * @brief Checks if the current endpoint is already attached to some user.
     *
     * @return TRUE if the current endpoint is attached, FALSE otherwise.
     */
    virtual bool isAttachedToUser() = 0;

    virtual ~IEndpointRegistrationManager() {}
};

}

#endif /* IENDPOINTREGISTRATIONMANAGER_HPP_ */
