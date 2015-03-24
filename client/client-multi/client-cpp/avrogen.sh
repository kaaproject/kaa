#
# Copyright 2014 CyberVision, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#!/bin/bash

mkdir -p avro/event

avrogencpp -i avro/endpoint.avsc -o kaa/gen/EndpointGen.hpp -n kaa
avrogencpp -i avro/profile.avsc -o kaa/gen/ProfileGen.hpp -n kaa
avrogencpp -i avro/notification.avsc -o kaa/notification/gen/NotificationGen.hpp -n kaa
avrogencpp -i avro/log.avsc -o kaa/log/gen/LogGen.hpp -n kaa
avrogencpp -i avro/configuration.avsc -o kaa/configuration/gen/ConfigurationGen.hpp -n kaa

# List items must be sepated by space. Empty string is applicable as well.
EVENT_CLASS_FAMILY_LIST=""

for FAMILY in $EVENT_CLASS_FAMILY_LIST
do
    avrogencpp -i avro/event/"$FAMILY".avsc -o kaa/event/gen/"$FAMILY"Gen.hpp -n kaa
done
