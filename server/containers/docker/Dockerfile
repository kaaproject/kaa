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
FROM openjdk:8u212-jre-stretch
MAINTAINER KAA IoT PLATFORM <www.kaaproject.org>
ENV DEBIAN_FRONTEND noninteractive

# Copy compiled/downloaded Kaa .DEB file and install
ARG setupfile
COPY ["$setupfile", "/kaa-node.deb"]
RUN dpkg -i /kaa-node.deb \
	&& rm -R /kaa-node.deb \
	&& apt-get autoremove -y && apt-get clean

RUN touch /var/log/kaa/kaa-node.log \
	&& chown kaa:kaa /var/log/kaa/kaa-node.log

# Kaa service & convenience shell scripts
COPY kaa/ /kaa

EXPOSE 9090

ENTRYPOINT ["/kaa/docker-entry.sh"]
