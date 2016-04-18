#
# Copyright 2014-2016 CyberVision, Inc.
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

************************************
PREREQUISITES
************************************

Before building the C++ endpoint SDK, ensure the following components 
are installed:

1. C++ compiler supporting the C++11 standard
2. cmake 2.8.8 or later
3. Boost 1.55
4. Avro 1.7.6
5. Botan 1.10

************************************
INSTALL
************************************

To build the C++ endpoint SDK, do the following:

1. Download and untar the Kaa C++ SDK archive.
2. Run the following commands:
    ./avrogen.sh
    mkdir build
    cd build
    cmake ..
    make
    make install

************************************
GENERAL CONFIGURATION
************************************

To configure the C++ endpoint SDK build, you can optionally specify
the following parameters for the cmake command:
------------------------------------
KAA_INSTALL_PATH - specify the directory for Kaa to be installed.

Accepted:
'/path/to/some/directory'

Default:
'/usr/local'
------------------------------------
KAA_DEBUG_ENABLED=[0|1] - build type.

Accepted:
0 - the release type
1 - the debug type

Default:
0
------------------------------------
KAA_MAX_LOG_LEVEL - maximum log level used by the Kaa SDK.

Accepted:
0 - NONE (no logs)
1 - FATAL
2 - ERROR
3 - WARN
4 - INFO
5 - DEBUG
6 - TRACE

Default:
4 - if KAA_DEBUG_ENABLED=0
6 - if KAA_DEBUG_ENABLED=1
------------------------------------
KAA_WITHOUT_<MODULE> - the Kaa module to be omitted during the build.

Accepted:
EVENTS
LOGGING
CONFIGURATION
NOTIFICATIONS
OPERATION_TCP_CHANNEL
OPERATION_LONG_POLL_CHANNEL
OPERATION_HTTP_CHANNEL
BOOTSTRAP_HTTP_CHANNEL
CONNECTIVITY_CHECKER
THREADSAFE

Default:
All modules are present in the build.

************************************
PLATFORM DEPEDENCIES
************************************
There are no dependencies on platforms.

************************************
BUILD EXAMPLE
************************************
The following example illustrates the build procedure for the debug build
with the INFO log level and disabled EVENTS feature and specified path to the folder Kaa will be installed in:
    ./avrogen.sh
    mkdir build
    cd build
    cmake -DKAA_INSTALL_PATH='/home/username/kaa' -DKAA_DEBUG_ENABLED=1 -DKAA_MAX_LOG_LEVEL=4 -DKAA_WITHOUT_EVENTS=1 ..
    make
    make install
