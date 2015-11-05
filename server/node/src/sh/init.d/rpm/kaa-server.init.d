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
# Starts a #serverdesc#
#
# chkconfig: 2345 90 10
# description: #serverdesc#
#
### BEGIN INIT INFO
# Provides:          #servername#
# Required-Start:    $remote_fs
# Should-Start:
# Required-Stop:     $remote_fs
# Should-Stop:
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: #serverdesc#
### END INIT INFO

# Custom part

NAME=#servername#
DESC="#serverdesc#"

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
JAVA_OPTIONS="#javaopts#"

STATUS_RUNNING=0
STATUS_DEAD=1
STATUS_DEAD_AND_LOCK=2
STATUS_NOT_RUNNING=3

ERROR_PROGRAM_NOT_INSTALLED=5

if [ `id -u` -ne 0 ]; then
	echo "You need root privileges to run this script"
	exit 1
fi      

. /etc/rc.d/init.d/functions

# Load default options, if any
if [ -f "$DEFAULT" ]; then
    CURRENT_JAVA_OPTIONS=$JAVA_OPTIONS
    . "$DEFAULT"
    # Current options are considered to be more specific,
    # so they are overriding defaults
    JAVA_OPTIONS="$JAVA_OPTIONS $CURRENT_JAVA_OPTIONS"
    export JAVA_OPTIONS
fi

if [ -z $SERVER_LOG_SUFIX ]; then
    SERVER_LOG_SUFIX=
fi

# These directories may be tmpfs and may or may not exist
# depending on the OS (ex: /var/lock/subsys does not exist on debian/ubuntu)
for dir in "$SERVER_RUN_DIR" "$SERVER_LOCK_DIR"; do
  [ -d "${dir}" ] || install -d -m 0755 -o $SERVER_USER -g $SERVER_USER ${dir}
done

export SERVER_HOME
export SERVER_LOG_DIR
export SERVER_LOG_SUFIX
export JAVA_OPTIONS

start() {
  [ -x $exec ] || exit $ERROR_PROGRAM_NOT_INSTALLED

  checkstatus 0
  status=$?
  if [ "$status" -eq "$STATUS_RUNNING" ]; then
    checkstatus 1    
    exit 0
  fi

  if [ ! -d ${SERVER_LOG_DIR} ]; then
      mkdir -p ${SERVER_LOG_DIR}
  fi

  success && echo "Starting $desc ($NAME): "
  /bin/su -s /bin/bash -c "/bin/bash -c 'echo \$\$ >${SERVER_PID_FILE} && exec ${EXEC_PATH} start >>${SERVER_LOG_DIR}/${NAME}-server${SERVER_LOG_SUFIX}.init.log 2>&1' &" $SERVER_USER
  RETVAL=$?
  [ $RETVAL -eq 0 ] && touch $LOCKFILE
  return $RETVAL
}

stop() {
  if [ ! -e $SERVER_PID_FILE ]; then
    failure && echo "$desc is not running"
    exit 0
  fi

  success && echo "Stopping $desc ($NAME): "

  SERVER_PID=`cat $SERVER_PID_FILE`
  if [ -n $SERVER_PID ]; then
    /bin/su -s /bin/bash -c "${EXEC_PATH} stop >>${SERVER_LOG_DIR}/${NAME}-server${SERVER_LOG_SUFIX}.shutdown.log 2>&1" $SERVER_USER
    for i in `seq 1 ${SERVER_SHUTDOWN_TIMEOUT}` ; do
      kill -0 ${SERVER_PID} &>/dev/null || break
      sleep 1
    done
    kill -KILL ${SERVER_PID} &>/dev/null
  fi
  rm -f $LOCKFILE $SERVER_PID_FILE
  return 0
}

console() {
  if [ ! -e $SERVER_PID_FILE ]; then
    failure && echo "$desc is not running"
    exit 0
  fi

  /bin/su -s /bin/bash -c "(${EXEC_PATH} console \"$*\" 2>&1) | tee -a ${SERVER_LOG_DIR}/${NAME}-server${SERVER_LOG_SUFIX}.console.log" $SERVER_USER

}      

restart() {
  stop
  start
}

checkstatus(){
  log_status=$1
  pidofproc -p $SERVER_PID_FILE java > /dev/null
  status=$?

  if [ $log_status -eq 1 ]; then
    case "$status" in
      $STATUS_RUNNING)
        success && echo "$desc is running"
        ;;
      $STATUS_DEAD)
        failure && echo "$desc is dead and pid file exists"
        ;;
      $STATUS_DEAD_AND_LOCK)
        failure && echo "$desc is dead and lock file exists"
        ;;
      $STATUS_NOT_RUNNING)
        failure && echo "$desc is not running"
        ;;
      *)
        failure && echo "$desc status is unknown"
        ;;
    esac
  fi
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
    checkstatus 1
    ;;
  console)
    console ${@:2}
    ;;
  restart)
    restart
    ;;
  condrestart|try-restart)
    condrestart
    ;;
  *)
    echo $"Usage: $0 {start|stop|status|console|restart|try-restart|condrestart}"
    exit 1
esac

exit $RETVAL
