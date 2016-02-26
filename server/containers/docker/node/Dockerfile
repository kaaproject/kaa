FROM nimmis/java:oracle-8-jdk
MAINTAINER Dmitry Sergeev <dsergeev@cybervisiontech.com>
#Install build dependencies
RUN apt-get update && apt-get install -y maven gcc cmake wget # libssl-dev libcunit1-dev g++ libboost1.54-all-dev

RUN git clone https://github.com/kaaproject/kaa.git
RUN cd kaa/ && git checkout release/v0.8.0 && mvn -P cassandra,compile-gwt,mongo-dao,cassandra-dao clean install verify -DskipTests && dpkg -i server/node/target/kaa-node.deb

RUN service kaa-node stop

#Don't prompt for password
RUN sudo echo 'kaa     ALL=(ALL) NOPASSWD: ALL' >> /etc/sudoers

ADD ./config_listener.sh /
RUN chmod 777 /config_listener.sh
EXPOSE 8080 25 20 9888 9889 9997 9999
RUN service kaa-node start
RUN /bin/bash
