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

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_EVENTS

#include <list>
#include <string>

namespace kaa {

typedef std::map<std::string/*epToken*/, std::string/*epHash*/> AttachedEndpoints;

class IAttachedEndpointListListener;
class IEndpointAttachStatusListener;

/**
 * Manager which is responsible for Endpoints attach/detach.
 */
class IEndpointRegistrationManager
{
public:
    /**
     * Generate new access token for a current endpoint
     */
    virtual void regenerateEndpointAccessToken() = 0;

    /**
     * Retrieve an access token for a current endpoint
     */
    virtual const std::string& getEndpointAccessToken() = 0;

    /**
     * Adds new endpoint attach request
     *
     * @param endpointAccessToken Access token of the attaching endpoint
     * @param listener Optional listener to notify about result of endpoint attaching.
     *        Set to null if is no need in it.
     */
    virtual void attachEndpoint(const std::string&  endpointAccessToken
                              , IEndpointAttachStatusListener* listener = nullptr) = 0;

    /**
     * Adds new endpoint detach request
     *
     * @param endpointKeyHash Key hash of the attached endpoint
     * @param listener Optional listener to notify about result of endpoint detaching.
     *        Set to null if is no need in it.
     */
    virtual void detachEndpoint(const std::string&  endpointKeyHash
                              , IEndpointAttachStatusListener* listener = nullptr) = 0;

    /**
     * Adds current endpoint detach request
     * @param listener Optional listener to notify about result of endpoint detaching.
     *        Set to null if is no need in it.
     */
    virtual void detachEndpoint(IEndpointAttachStatusListener* listener = nullptr) = 0;

    /**
     * Attaches the endpoint to a user entity. The user verification is carried out by the default verifier.
     *
     * @b NOTE: If the default user verifier (@link DEFAULT_USER_VERIFIER_TOKEN @endlink) is not specified,
     * the attach attempt fails with the @c KaaException exception.
     *
     * Use this function to request attachment of the endpoint to a user entity using the specified external authentication
     * credentials. Only endpoints associated with the same user entity can exchange events.
     *
     * @param userExternalId    External user ID.
     * @param userAccessToken   External access token.
     */
    virtual void attachUser(const std::string& userExternalId
                          , const std::string& userAccessToken
                          , IEndpointAttachStatusListener* listener = nullptr) = 0;

    /**
     * Attaches the endpoint to a user entity. The user verification will be carried out by the specified verifier.
     *
     * Use this function to request attachment of the endpoint to a user entity using the specified external authentication
     * credentials. Only endpoints associated with the same user entity can exchange events.
     *
     * @param userExternalId      External user ID.
     * @param userAccessToken     External access token.
     * @param userVerifierToken   User verifier token.
     */
    virtual void attachUser(const std::string& userExternalId
                          , const std::string& userAccessToken
                          , const std::string& userVerifierToken
                          , IEndpointAttachStatusListener* listener = nullptr) = 0;

    /**
     * Retrieves list of attached endpoints
     *
     * @return set of attached endpoint's "access token/key hash" pair
     */
    virtual const AttachedEndpoints& getAttachedEndpoints() = 0;

    /**
     * Adds listener of attached endpoint list changes
     *
     * @param listener Attached endpoints list change listener
     */
    virtual void addAttachedEndpointListListener(IAttachedEndpointListListener *listener) = 0;

    /**
     * Removes listener of attached endpoint list changes
     *
     * @param listener Attached endpoints list change listener
     */
    virtual void removeAttachedEndpointListListener(IAttachedEndpointListListener *listener) = 0;

    /**
     * Checks if current endpoint is already attached to some user
     */
    virtual bool isCurrentEndpointAttached() = 0;

    /**
     * Set lister to notify about attaching/detaching the current endpoint
     * either by itself or another endpoint
     *
     * @param listener Attach status listener
     */
    virtual void setAttachStatusListener(IEndpointAttachStatusListener* listener) = 0;

    virtual ~IEndpointRegistrationManager() {}
};

}

#endif

#endif /* IENDPOINTREGISTRATIONMANAGER_HPP_ */
