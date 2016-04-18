@REM
@REM Copyright 2014-2016 CyberVision, Inc.
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM      http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

@echo off

setlocal EnableDelayedExpansion

md avro\event

avrogencpp.exe -i avro\endpoint.avsc -o kaa\gen\EndpointGen.hpp -n kaa
avrogencpp.exe -i avro\profile.avsc -o kaa\profile\gen\ProfileGen.hpp -n kaa_profile
avrogencpp.exe -i avro\notification.avsc -o kaa\notification\gen\NotificationGen.hpp -n kaa_notification
avrogencpp.exe -i avro\log.avsc -o kaa\log\gen\LogGen.hpp -n kaa_log
avrogencpp.exe -i avro\configuration.avsc -o kaa\configuration\gen\ConfigurationGen.hpp -n kaa_configuration

@rem List items must be sepated by space. Empty string is applicable as well.
set EVENT_CLASS_FAMILY_LIST=

:nextVar
   for /F "tokens=1* delims= " %%a in ("%EVENT_CLASS_FAMILY_LIST%") do (
      avrogencpp.exe -i avro\event\%%a.avsc -o kaa\event\gen\%%aGen.hpp -n ns%%a
      set EVENT_CLASS_FAMILY_LIST=%%b
   )
if defined EVENT_CLASS_FAMILY_LIST goto nextVar

endlocal