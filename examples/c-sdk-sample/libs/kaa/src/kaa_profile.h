/*
 * Copyright 2014 CyberVision, Inc.
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

/**
 * @file kaa_profile.h
 * @brief Kaa profile reporting API
 *
 * Supplies API to report endpoint profile to Operations server.
 */

# ifndef KAA_PROFILE_H_
# define KAA_PROFILE_H_

# ifdef __cplusplus
extern "C" {
# endif

# include <stdbool.h>
# include "kaa_error.h"
# include "gen/kaa_profile_gen.h"



/**
 * User defined endpoint profile structure
 */
typedef kaa_profile_profile_t kaa_profile_t;



/**
 * Private profile manager data structure
 */
typedef struct kaa_profile_manager_t kaa_profile_manager_t;



/**
 * @brief Updates user profile.
 *
 * After a new profile is set a sync request to Operations server will be sent.
 * The profile must be set prior to the endpoint registration.
 *
 * @param[in] self      Profile manager instance.
 * @param[in] profile   Filled in user-defined profile data structure.
 *
 * @return      Error code.
 */
kaa_error_t kaa_profile_update_profile(kaa_profile_manager_t *self, kaa_profile_t *profile);



# ifdef __cplusplus
}      /* extern "C" */
# endif

# endif /* KAA_PROFILE_H_ */
