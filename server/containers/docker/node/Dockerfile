FROM nimmis/java:oracle-8-jdk
MAINTAINER Dmitry Sergeev <dsergeev@cybervisiontech.com>
#Install build dependencies
RUN apt-get update && apt-get install -y maven gcc cmake wget
RUN mkdir kaa && cd kaa && wget https://github.com/kaaproject/kaa/releases/download/v0.9.0/kaa-deb-0.9.0.tar.gz
RUN cd kaa && tar -xvf kaa-deb-0.9.0.tar.gz && dpkg -i deb/kaa-node.deb

RUN service kaa-node stop

#Don't prompt for password
RUN sudo echo 'kaa     ALL=(ALL) NOPASSWD: ALL' >> /etc/sudoers

ADD ./config_listener.sh /
RUN chmod 777 /config_listener.sh
EXPOSE 8080 25 20 9888 9889 9997 9999
RUN service kaa-node start
RUN /bin/bash
