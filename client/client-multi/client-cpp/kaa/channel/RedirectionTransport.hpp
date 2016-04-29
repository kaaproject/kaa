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

#ifndef REDIRECTIONTRANSPORT_HPP_
#define REDIRECTIONTRANSPORT_HPP_

#include "kaa/channel/transport/IRedirectionTransport.hpp"
#include "kaa/bootstrap/IBootstrapManager.hpp"

namespace kaa {

class RedirectionTransport : public IRedirectionTransport {
public:
    RedirectionTransport(IBootstrapManager &manager);
    void onRedirectionResponse(const RedirectSyncResponse& response);

private:
    IBootstrapManager &manager_;
};

}  // namespace kaa


#endif /* REDIRECTIONTRANSPORT_HPP_ */
