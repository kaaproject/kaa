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

#ifndef Kaa_TransportCommon_h
#define Kaa_TransportCommon_h

/**
 * TransportType - enum with list of all possible transport types which
 * every Channel can support.
 */
typedef enum {
    TRANSPORT_TYPE_BOOTSTRAP,
    TRANSPORT_TYPE_PROFILE,
    TRANSPORT_TYPE_CONFIGURATION,
    TRANSPORT_TYPE_NOTIFICATION,
    TRANSPORT_TYPE_USER,
    TRANSPORT_TYPE_EVENT,
    TRANSPORT_TYPE_LOGGING
} TransportType;

typedef enum {
    SERVER_BOOTSTRAP,
    SERVER_OPERATIONS
} ServerType;

typedef enum {
    /**
     *  From the endpoint to the server
     */
    CHANNEL_DIRECTION_UP,
    /**
     *  From the server to the enpoint
     */
    CHANNEL_DIRECTION_DOWN,
    /**
     * In both ways
     */
    CHANNEL_DIRECTION_BIDIRECTIONAL
    
} ChannelDirection;

#endif
