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

#ifndef KAA_CHANNEL_IMPERMANENTDATACHANNEL_HPP_
#define KAA_CHANNEL_IMPERMANENTDATACHANNEL_HPP_

#include "kaa/channel/IDataChannel.hpp"

namespace kaa {

class ImpermanentDataChannel : public IDataChannel {
public:
    virtual ~ImpermanentDataChannel() { }

    virtual void resume() { }
    virtual void pause() { }
    virtual void shutdown() { }
};

}



#endif /* KAA_CHANNEL_IMPERMANENTDATACHANNEL_HPP_ */
