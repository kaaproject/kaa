/*
 * posix_kaa_strategy.h
 *
 *  Created on: Jul 1, 2015
 *      Author: architec
 */

#ifndef POSIX_KAA_STRATEGY_H_
#define POSIX_KAA_STRATEGY_H_

#include "../../platform/ext_kaa_failover_strategy.h"


kaa_error_t kaa_failover_strategy_set(kaa_failover_strategy_t* strategy, kaa_failover_decision_t* decision);

#endif /* POSIX_KAA_STRATEGY_H_ */
