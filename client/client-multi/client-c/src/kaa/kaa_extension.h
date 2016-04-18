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
#include <stdbool.h>

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
     * Serialize request.
     *
     * @note @p buffer may be @c NULL. In that case the function must
     * return the required size of the buffer in @p size.
     *
     * @param[in]     context     The context of the extension, as returned
     *                            by init().
     *
     * @param[in]     request_id  The id of the currently serializing request.
     *
     * @param[out]    buffer      Serialized request.
     *
     * @param[in,out] size        Size of the buffer. The function must
     *                            set to the actual size used (or required).
     *
     * @param[out]    sync_needed Extension must set it to @c true if
     *                            it requires syncing. If set to @c false,
     *                            the extension's buffer is ignored and not
     *                            synced.
     *
     * @return Error code.
     *
     * @retval KAA_ERR_BUFFER_IS_NOT_ENOUGH Should be returned if @p
     * size is smaller than needed.
     *
     * @note Error codes other than @c KAA_ERR_NONE will result in
     * abort of transaction; extension must set @p sync_needed to
     * @c false if extension has nothing to say.
     */
    kaa_error_t (*request_serialize)(void *context, uint32_t request_id,
            uint8_t *buffer, size_t *size, bool *sync_needed);

    /**
     * Extension's action in response to the server's sync message.
     *
     * @param[in] context    The extension context, as returned by init().
     *
     * @param[in] request_id The id of the request server is responding.
     *
     * @param[in] extension_options
     * @parblock
     * Protocol-dependent options.
     *
     * @note Don't rely on it, as it may be removed in the future.
     * @endparblock
     *
     * @param[in] buffer     The message.
     *
     * @param[in] size       Size of the @p buffer.
     *
     * @return Error code.
     */
    kaa_error_t (*server_sync)(void *context, uint32_t request_id,
            uint16_t extension_options, const uint8_t *buffer, size_t size);
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
 * A proxy for kaa_extension::request_serialize().
 *
 * @retval KAA_ERR_NOT_FOUND Extension was not found.
 */
kaa_error_t kaa_extension_request_serialize(kaa_extension_id id, uint32_t request_id,
        uint8_t *buffer, size_t *size, bool *sync_needed);

/**
 * A proxy for kaa_extension::server_sync().
 *
 * @retval KAA_ERR_NOT_FOUND Extension was not found.
 */
kaa_error_t kaa_extension_server_sync(kaa_extension_id id, uint32_t request_id,
        uint16_t extension_options, const uint8_t *buffer, size_t size);

#ifdef __cplusplus
}
#endif

#endif /* KAA_EXTENSION_H */
