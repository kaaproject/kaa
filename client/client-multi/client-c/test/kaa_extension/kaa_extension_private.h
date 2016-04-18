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
 * This file is a fake definition of kaa extensions. It's used in the
 * test_kaa_extension.c.
 *
 * The kaa_extension.c must use info from the kaa_extension_private.h
 * file for proper work. (That's a part of its requirements.)
 *
 * For more info see original kaa_extension_private.h file.
 */
#include <kaa_extension.h>

extern const struct kaa_extension fake_extension1;
extern const struct kaa_extension fake_extension2;
extern const struct kaa_extension fake_extension3;

static const struct kaa_extension *kaa_extensions[] = {
    &fake_extension1,
    &fake_extension2,
    &fake_extension3,
};
