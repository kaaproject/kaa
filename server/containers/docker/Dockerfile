FROM ubuntu:16.04
MAINTAINER Christopher Burroughs <chris@xmight.com>
ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && apt-get -yqq install \
		software-properties-common \
		python-software-properties \
		ca-certificates \
		netcat \
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
COPY install/deb/* /install/deb/
RUN dpkg -i /install/deb/kaa-node.deb \
	&& rm -R /install/deb/ \
	&& apt-get autoremove -y && apt-get clean

# PostgreSQL driver
ADD http://central.maven.org/maven2/org/postgresql/postgresql/9.4.1208/postgresql-9.4.1208.jar /usr/lib/kaa-node/lib/postgresql.jar
RUN chmod 755 /usr/lib/kaa-node/lib/postgresql.jar

RUN touch /var/log/kaa/kaa-node.log \
	&& chown kaa:kaa /var/log/kaa/kaa-node.log

# Kaa service & convenience shell scripts
COPY kaa/ /kaa

# Kaa config template files
COPY install/conf/ /usr/lib/kaa-node/conf/

EXPOSE 8080 9888 9889 9997 9999
VOLUME ["/var/log/kaa", "/usr/lib/kaa-node/conf"]

# Make kaa sudoer
RUN echo 'kaa     ALL=(ALL) NOPASSWD: ALL' >> /etc/sudoers

ENTRYPOINT ["/kaa/docker-entry.sh"]