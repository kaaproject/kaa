FROM nimmis/java:oracle-8-jdk
MAINTAINER Dmitry Sergeev <dsergeev@cybervisiontech.com>
#Install build dependencies
RUN apt-get update && apt-get install -y maven gcc cmake wget # libssl-dev libcunit1-dev g++ libboost1.54-all-dev
RUN mkdir kaa && cd kaa && wget https://github.com/kaaproject/kaa/releases/download/v0.8.1/kaa-deb-0.8.1.tar.gz
RUN cd kaa && tar -xvf kaa-deb-0.8.1.tar.gz && dpkg -i deb/kaa-node.deb

RUN service kaa-node stop

#Don't prompt for password
RUN sudo echo 'kaa     ALL=(ALL) NOPASSWD: ALL' >> /etc/sudoers

ADD ./config_listener.sh /
RUN chmod 777 /config_listener.sh
EXPOSE 8080 25 20 9888 9889 9997 9999
RUN service kaa-node start
RUN /bin/bash
