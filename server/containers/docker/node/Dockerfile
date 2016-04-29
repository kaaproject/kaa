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

FROM nimmis/java:oracle-8-jdk
MAINTAINER Dmitry Sergeev <dsergeev@cybervisiontech.com>
#Install build dependencies
RUN apt-get update && apt-get install -y maven gcc cmake wget

ARG setupfile
ADD ["$setupfile", "/kaa-node.deb"]
RUN dpkg -i /kaa-node.deb

RUN service kaa-node stop

#Don't prompt for password
RUN sudo echo 'kaa     ALL=(ALL) NOPASSWD: ALL' >> /etc/sudoers

ADD ./config_listener.sh /
RUN chmod 777 /config_listener.sh
EXPOSE 8080 25 20 9888 9889 9997 9999
RUN service kaa-node start
RUN /bin/bash
