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

include ../../../../../applications/$(SDK_APP)/src/kaa/platform-impl/Econais/EC19D/filelist.mak
CFILES-SDK_APP = application.c $(CFILES-KAA)

OBJS-$(SDK_APP) := $(CFILES-SDK_APP:.c=.o) $(SFILES-SDK_APP:.s=.o)
CFLAGS-SDK_APP = 
#CFLAGS += "-std=gnu99"
CFLAGS += "-D ECONAIS_PLATFORM"
CFLAGS += "-D TRACE_DELAY=1"
# CFLAGS += "-D KAA_TRACE_MEMORY_ALLOCATIONS"

lint.sdk_app : $(CFILES-SDK_APP:.c=.lnt)
$(SDK_APP).a : $(OBJS-$(SDK_APP))

