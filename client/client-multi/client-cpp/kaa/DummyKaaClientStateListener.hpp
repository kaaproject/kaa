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

#ifndef DUMMYKAACLIENTSTATELISTENER_HPP_
#define DUMMYKAACLIENTSTATELISTENER_HPP_

#include <kaa/IKaaClientStateListener.hpp>

namespace kaa {

class DummyKaaClientStateListener: public IKaaClientStateListener {
public:
    virtual void onStarted() {}
    virtual void onStartFailure(const KaaException& exception) {}

    virtual void onPaused() {}
    virtual void onPauseFailure(const KaaException& exception) {}

    virtual void onResumed() {}
    virtual void onResumeFailure(const KaaException& exception) {}

    virtual void onStopped() {}
    virtual void onStopFailure(const KaaException& exception) {}
};

} /* namespace kaa */

#endif /* DUMMYKAACLIENTSTATELISTENER_HPP_ */
