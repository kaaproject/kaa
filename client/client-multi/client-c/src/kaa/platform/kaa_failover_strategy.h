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

#ifndef POSIX_KAA_STRATEGY_H_
#define POSIX_KAA_STRATEGY_H_

#include "platform/ext_kaa_failover_strategy.h"

/**
* @brief Sets decision that will be made in case of failover.
*
* @param[in]   strategy     The pointer to the failover strategy instance.
* @param[in]  decision      The pointer to the the decision structure, filled by user.
*
* @return The error code.
*/
kaa_error_t kaa_failover_strategy_set(kaa_failover_strategy_t *strategy,
        kaa_failover_decision_t *decision);


#endif /* POSIX_KAA_STRATEGY_H_ */
