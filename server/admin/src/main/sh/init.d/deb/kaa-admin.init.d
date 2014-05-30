#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Starts a Kaa Admin Web Server
#
# chkconfig: 2345 90 10
# description: Kaa Admin Web Server
#
### BEGIN INIT INFO
# Provides:          kaa-admin
# Required-Start:    $remote_fs
# Should-Start:
# Required-Stop:     $remote_fs
# Should-Stop:
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Kaa Admin Web Server
### END INIT INFO

# Custom part

NAME=kaa-admin
DESC="Kaa Administration Web Server"

# Common part

DEFAULT=/etc/default/$NAME
SERVER_LOG_DIR=/var/log/kaa
SERVER_CONF_DIR=/etc/$NAME/conf
SERVER_RUN_DIR=/var/run/kaa
SERVER_HOME=/usr/lib/$NAME
SERVER_USER=kaa
SERVER_LOCK_DIR="/var/lock/subsys/"
SERVER_PID_FILE=${SERVER_RUN_DIR}/${NAME}.pid
SERVER_SHUTDOWN_TIMEOUT=${SERVER_SHUTDOWN_TIMEOUT:-60}

LOCKFILE="${SERVER_LOCK_DIR}/${NAME}"
desc="$DESC daemon"
EXEC_PATH=/usr/bin/$NAME
JAVA_OPTIONS="-Xmx256m"

STATUS_RUNNING=0
STATUS_DEAD=1
STATUS_DEAD_AND_LOCK=2
STATUS_NOT_RUNNING=3

ERROR_PROGRAM_NOT_INSTALLED=5

if [ `id -u` -ne 0 ]; then
	echo "You need root privileges to run this script"
	exit 1
fi      

. /lib/lsb/init-functions


if [ -f "$DEFAULT" ]; then
    . "$DEFAULT"
fi


# These directories may be tmpfs and may or may not exist
# depending on the OS (ex: /var/lock/subsys does not exist on debian/ubuntu)
for dir in "$SERVER_RUN_DIR" "$SERVER_LOCK_DIR"; do
  [ -d "${dir}" ] || install -d -m 0755 -o $SERVER_USER -g $SERVER_USER ${dir}
done

export SERVER_HOME
export SERVER_LOG_DIR
export JAVA_OPTIONS

start() {
  [ -x $exec ] || exit $ERROR_PROGRAM_NOT_INSTALLED

  checkstatus
  status=$?
  if [ "$status" -eq "$STATUS_RUNNING" ]; then
    exit 0
  fi

  if [ ! -d ${SERVER_LOG_DIR} ]; then
      mkdir -p ${SERVER_LOG_DIR}
  fi

  log_success_msg "Starting $desc ($NAME): "
  /bin/su -s /bin/bash -c "/bin/bash -c 'echo \$\$ >${SERVER_PID_FILE} && exec ${EXEC_PATH} start >>${SERVER_LOG_DIR}/${NAME}-server.init.log 2>&1' &" $SERVER_USER
  RETVAL=$?
  [ $RETVAL -eq 0 ] && touch $LOCKFILE
  return $RETVAL
}

stop() {
  if [ ! -e $SERVER_PID_FILE ]; then
    log_failure_msg "$desc is not running"
    exit 0
  fi

  log_success_msg "Stopping $desc ($NAME): "

  SERVER_PID=`cat $SERVER_PID_FILE`
  if [ -n $SERVER_PID ]; then
    ${EXEC_PATH} stop
    for i in `seq 1 ${SERVER_SHUTDOWN_TIMEOUT}` ; do
      kill -0 ${SERVER_PID} &>/dev/null || break
      sleep 1
    done
    kill -KILL ${SERVER_PID} &>/dev/null
  fi
  rm -f $LOCKFILE $SERVER_PID_FILE
  return 0
}

restart() {
  stop
  start
}

checkstatus(){
  pidofproc -p $SERVER_PID_FILE java > /dev/null
  status=$?

  case "$status" in
    $STATUS_RUNNING)
      log_success_msg "$desc is running"
      ;;
    $STATUS_DEAD)
      log_failure_msg "$desc is dead and pid file exists"
      ;;
    $STATUS_DEAD_AND_LOCK)
      log_failure_msg "$desc is dead and lock file exists"
      ;;
    $STATUS_NOT_RUNNING)
      log_failure_msg "$desc is not running"
      ;;
    *)
      log_failure_msg "$desc status is unknown"
      ;;
  esac
  return $status
}

condrestart(){
  [ -e ${LOCKFILE} ] && restart || :
}

case "$1" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  status)
    checkstatus
    ;;
  restart)
    restart
    ;;
  condrestart|try-restart)
    condrestart
    ;;
  *)
    echo $"Usage: $0 {start|stop|status|restart|try-restart|condrestart}"
    exit 1
esac

exit $RETVAL
