
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


set (KAA_SOURCE_FILES
               ${KAA_SOURCE_FILES}
               src/gen/kaa_profile_gen.c
               src/gen/kaa_logging_gen.c
               src/event/kaa_device_event_class_family.c
               src/kaa_event.c
               src/event/kaa_device_event_class_family_definitions.c
)
