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

#ifndef KAACLIENTSTATELISTENER_HPP_
#define KAACLIENTSTATELISTENER_HPP_

#include <memory>

#include <kaa/EndpointConnectionInfo.hpp>

namespace kaa {

class KaaException;

/**
 * @brief Notifies about Kaa client state changes and errors.
 *
 * @author Denis Kimcherenko
 *
 */
class KaaClientStateListener {
public:

    /**
     * @brief On successful start of Kaa client. Kaa client is successfully connected to Kaa cluster and is ready for
     * usage.
     */
    virtual void onStarted() {}

    /**
     * @brief On failure during Kaa client startup. Typically failure is related to network issues.
     *
     * @param exception cause of failure
     */
    virtual void onStartFailure(const KaaException& exception) {}

    /**
     * @brief On successful pause of Kaa client. Kaa client is successfully paused and does not consume any resources
     * now.
     */
    virtual void onPaused() {}

    /**
     * @brief On failure during Kaa client pause. Typically related to failure to free some resources.
     *
     * @param exception cause of failure
     */
    virtual void onPauseFailure(const KaaException& exception) {}

    /**
     * @brief On successful resume of Kaa client. Kaa client is successfully connected to Kaa cluster and is ready
     * for usage.
     */
    virtual void onResumed() {}

    /**
     * @brief On failure during Kaa client resume. Typically failure is related to network issues.
     *
     * @param exception cause of failure
     */
    virtual void onResumeFailure(const KaaException& exception) {}

    /**
     * @brief On successful stop of Kaa client. Kaa client is successfully stopped and does not consume any resources
     * now.
     */
    virtual void onStopped() {}

    /**
     * @brief On failure during Kaa client stop. Typically related to failure to free some resources.
     *
     * @param exception cause of failure
     */
    virtual void onStopFailure(const KaaException& exception) {}

    /**
     *
     * On connection established. The method is called as soon as the connection with either bootstrap
     * or operation server is established.
     *
     * @param[in] connection The connection metadata, see @c EndpointConnectionInfo.
     */
    virtual void onConnectionEstablished(const EndpointConnectionInfo& connection) {}

    virtual ~KaaClientStateListener() = default;
};

using KaaClientStateListenerPtr = std::shared_ptr<KaaClientStateListener>;

} /* namespace kaa */

#endif /* KAACLIENTSTATELISTENER_HPP_ */
