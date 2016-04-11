/*
 *  Copyright 2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/** @file
 * Extension manager. The extensions should be defined in the
 * kaa_extension_private.h. Currently that file is static but will be
 * auto-generated in the future.
 */
#ifndef KAA_EXTENSION_H
#define KAA_EXTENSION_H

#include <kaa_common.h>
#include <kaa_error.h>
#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

// Forward-declaring kaa_context_t
struct kaa_context_s;

/**
 * An extension interface facing Kaa core.
 */
struct kaa_extension {
    kaa_extension_id id;

    /**
     * Init function must initialize resource or return error code. In
     * case of error all resources should be freed.
     *
     * @param[in]  kaa_context Kaa client context.
     * @param[out] context     Opaque extension context.
     */
    kaa_error_t (*init)(struct kaa_context_s *kaa_context, void **context);

    /**
     * A reverse of init(). Must deinit all allocated resources.
     *
     * It's guaranteed to be called only once per init() call and with
     * output of init().
     *
     * @param[in] context Context returned by call to init.
     */
    kaa_error_t (*deinit)(void *context);

    /**
     * The size of request.
     *
     * @param[in]  context       The context of extension. It was returned in
     *                           init().
     * @param[out] extected_size The size of request.
     * @return Error code.
     *
     * @note Error codes other than @c KAA_ERR_NONE will result in
     * abort of transaction; it's better to return @c KAA_ERR_NONE and
     * @c 0 @p expected_size if you have nothing to say.
     */
    kaa_error_t (*request_get_size)(void *context, size_t *expected_size);
};

/**
 * Return extension for given extension @p id.
 *
 * @retval NULL Extension not found.
 */
const struct kaa_extension *kaa_extension_get(kaa_extension_id id);

/**
 * Return context for the given extension @p id.
 *
 * @retval NULL Extension not found.
 */
void *kaa_extension_get_context(kaa_extension_id id);

/**
 * Sets extension context to later be retrieved with
 * kaa_extension_get_context().
 *
 * @retval KAA_ERR_NONE      Success.
 * @retval KAA_ERR_NOT_FOUND No extension with such id.
 */
kaa_error_t kaa_extension_set_context(kaa_extension_id id, void *context);

/**
 * Initializes all extensions. If error occurs, it deinitializes all
 * initialized extensions in reverse order and returns error.
 *
 * @return Error code.
 */
kaa_error_t kaa_extension_init_all(struct kaa_context_s *kaa_context);

/**
 * Deinitializes all extensions in reverse order.
 *
 * If any extension errored during deinitialization, error code is
 * returned.
 *
 * @warning The function doesn't check if extension was
 * initialized. Must only be called if kaa_extension_init_all()
 * succeeded before.
 *
 * @retval KAA_ERR_NONE All extensions deinitialized successfully.
 *
 * @note In case several extensions failed deinitialization it's
 * unspecified which error code is returned.
 */
kaa_error_t kaa_extension_deinit_all(void);

/**
 * A proxy for kaa_extension::request_get_size().
 *
 * @retval KAA_ERR_NOT_FOUND Extension was not found.
 */
kaa_error_t kaa_extension_request_get_size(kaa_extension_id id, size_t *expected_size);

#ifdef __cplusplus
}
#endif

#endif /* KAA_EXTENSION_H */
