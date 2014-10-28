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

#ifndef COMMANDS_H_
#define COMMANDS_H_

typedef enum commands_t {
  COMMAND_NONE = 0,
  COMMAND_PING = 'p',
  COMMAND_MOVE_FORWARD_WO_CB = 'f',
  COMMAND_MOVE_FORWARD = 'F',
  COMMAND_MOVE_BACKWARD = 'b',
  COMMAND_TURN_RIGHT_WO_CB = 'r',
  COMMAND_TURN_RIGHT = 'R',
  COMMAND_TURN_LEFT_WO_CB = 'l',
  COMMAND_TURN_LEFT = 'L',
  COMMAND_KEEP_ALIVE = 'k',
  COMMAND_GET_MPU = 'm',

  COMMAND_BUSY = 'u',
  COMMAND_INVALID = 'i'
} commands_t;

#endif
