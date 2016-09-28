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
FROM ubuntu:16.04
MAINTAINER KAA IoT PLATFORM <www.kaaproject.org>
ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && apt-get -yqq install \
		software-properties-common \
		python-software-properties \
		ca-certificates \
		netcat \
		nano \
		net-tools \
	&& apt-get clean \
	&& rm -rf /var/lib/apt/lists/*

## ORACLE JAVA 8 (auto-accept license)
RUN \
	echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections \
	&& echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections \
	&& add-apt-repository ppa:webupd8team/java -y \
	&& apt-get update \
	&& apt-get -yqq install \
		oracle-java8-installer \
	&& apt-get purge -y \
		software-properties-common \
		python-software-properties \
	&& apt-get clean \
	&& rm -rf /var/lib/apt/lists/* \
	&& rm -rf /var/cache/oracle-jdk8-installer

ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

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
