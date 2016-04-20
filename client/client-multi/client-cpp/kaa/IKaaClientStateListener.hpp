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

#ifndef IKAACLIENTSTATELISTENER_HPP_
#define IKAACLIENTSTATELISTENER_HPP_

#include <memory>

namespace kaa {

class KaaException;

/**
 * @brief Notifies about Kaa client state changes and errors.
 *
 * @author Denis Kimcherenko
 *
 */
class IKaaClientStateListener {
public:

    /**
     * @brief On successful start of Kaa client. Kaa client is successfully connected to Kaa cluster and is ready for
     * usage.
     */
    virtual void onStarted() = 0;

    /**
     * @brief On failure during Kaa client startup. Typically failure is related to network issues.
     *
     * @param exception - cause of failure
     */
    virtual void onStartFailure(const KaaException& exception) = 0;

    /**
     * @brief On successful pause of Kaa client. Kaa client is successfully paused and does not consume any resources
     * now.
     */
    virtual void onPaused() = 0;

    /**
     * @brief On failure during Kaa client pause. Typically related to failure to free some resources.
     *
     * @param exception - cause of failure
     */
    virtual void onPauseFailure(const KaaException& exception) = 0;

    /**
     * @brief On successful resume of Kaa client. Kaa client is successfully connected to Kaa cluster and is ready
     * for usage.
     */
    virtual void onResumed() = 0;

    /**
     * @brief On failure during Kaa client resume. Typically failure is related to network issues.
     *
     * @param exception - cause of failure
     */
    virtual void onResumeFailure(const KaaException& exception) = 0;

    /**
     * @brief On successful stop of Kaa client. Kaa client is successfully stopped and does not consume any resources
     * now.
     */
    virtual void onStopped() = 0;

    /**
     * @brief On failure during Kaa client stop. Typically related to failure to free some resources.
     *
     * @param exception - cause of failure
     */
    virtual void onStopFailure(const KaaException& exception) = 0;

    virtual ~IKaaClientStateListener() = default;
};

typedef std::shared_ptr<IKaaClientStateListener> IKaaClientStateListenerPtr;

} /* namespace kaa */

#endif /* IKAACLIENTSTATELISTENER_HPP_ */
