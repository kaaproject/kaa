/*
 * Copyright 2014-2015 CyberVision, Inc.
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

#ifndef KAACLIENTCONTEXT_H
#define KAACLIENTCONTEXT_H

#include "IKaaClientContext.hpp"

namespace kaa {

class KaaClientContext : public IKaaClientContext
{
public:
    KaaClientContext(const KaaClientProperties &properties,
                     const ILogger &logger,
                     const IKaaClientStateStorage &state,
                     const IExecutorContext &executorContext);

    KaaClientProperties &getProperties();
    ILogger &getLogger();
    IKaaClientStateStorage &getStatus();
    IExecutorContext &getExecutorContext();

protected:
    KaaClientProperties    &properties_;
    ILogger                &logger_;
    IKaaClientStateStorage &state_;
    IExecutorContext       &executorContext_;
};

}


#endif // KAACLIENTCONTEXT_H
