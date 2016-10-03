#!/usr/bin/python
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

import subprocess
import sys
import platform
import commands
import string

variationsOfdatabases = ['mariadb-mongodb', 'mariadb-cassandra', 'postgresql-mongodb', 'postgresql-cassandra'];

DEFAULT_IMAGE_MARIADB="mariadb:5.5";
DEFAULT_IMAGE_POSTGRESQL="postgres:9.4";
DEFAULT_IMAGE_MONGODB="mongo:3.2";
DEFAULT_IMAGE_CASSANDRA="cassandra:3.7";

dockerImages = {'mariadb': DEFAULT_IMAGE_MARIADB, \
                'postgresql': DEFAULT_IMAGE_POSTGRESQL, \
                'mongodb': DEFAULT_IMAGE_MONGODB, \
                'cassandra': DEFAULT_IMAGE_CASSANDRA};

DEFAULT_KAA_SERVICE_NAME = "kaa_";

DEFAULT_INTERNAL_ADMIN_PORT=8080;

KaaConfigPorts = {'ADMIN_PORT': 10080, \
'BOOTSTRAP_TCP': 10088, \
'BOOTSTRAP_HTTP': 10089, \
'OPERATIONS_TCP': 10097, \
'OPERATIONS_HTTP': 10099};

DEFAULT_INCREASE_PORT_VALUES = 100;

kaaAdminUiPorts = [];
kaaNodeNames = [];

NGINX_TEMPLATE = '        server {{PROXY_HOST_KAA}}:{{PROXY_PORT}};'


def getExternalHostLinuxMacOs() :
    try:
        return commands.getstatusoutput("ip route get 8.8.8.8 | awk '{print $NF; exit}'")[1];
    except:
        return "N/A"


def getExternalHostWindows() :
    try:
        return commands.getstatusoutput("for /f \"skip=4 usebackq tokens=2\" %a in (`nslookup myip.opendns.com resolver1.opendns.com`) do echo %a")[1];
    except:

        return "N/A";

def getExternalHostKaa(kaaServiceName) :
    externalHostKaaCommand = "docker inspect --format='{{(index (index .NetworkSettings.Networks ) \"usingcompose_front-tier\").IPAddress}}' usingcompose_"+kaaServiceName+"_1";
    try:
        return commands.getstatusoutput(externalHostKaaCommand)[1];
    except:
        return "N/A";


def getInputedVariationsDataBases() :
    isValid = False;
    try:
        for i in range(0, len(variationsOfdatabases)):
            if sys.argv[1] == variationsOfdatabases[i]:
                isValid = True;
                return variationsOfdatabases[i];
        if isValid == False:
            sys.exit();
    except:
        print "Please choose correct variation of SQL and NoSQL databases \n" + str(variationsOfdatabases);
        sys.exit();


def configureThirdPartyComponents(fileName) :
    sql_nosql = getInputedVariationsDataBases().split("-");
    with open(fileName, 'r') as file:
        data = file.readlines();
    for i in range(0, len(data)):
        if " sql:" in data[i]:
            data[i+1]="    image: "+dockerImages[sql_nosql[0]]+"\n";
        if " nosql:" in data[i]:
            data[i+1]="    image: "+dockerImages[sql_nosql[1]]+"\n";
        if "env_file" in data[i]:
            data[i]="    env_file: "+sql_nosql[0]+"-sql-example.env\n"
    with open(fileName, 'w') as fout:
        fout.write(''.join(data));
    return;


def configurKaaNode(templateFileName, newFile) :
    kaaAdminUiPorts.append(KaaConfigPorts['ADMIN_PORT']);
    kaaNodeNames.append(DEFAULT_KAA_SERVICE_NAME+'0');
    sql_nosqlList = getInputedVariationsDataBases().split("-");
    insertLine=0;
    with open(templateFileName, 'r') as file:
        data = file.readlines();
    for i in range(0, len(data)):
        if 'kaa_lb:' in data[i]:
            insertLine=i;
        data[i] = configurePorts( data[i], sql_nosqlList, 0 )
    with open(newFile, 'w+') as fout:
        fout.write(''.join(data));
    return insertLine;


def configurePorts( strConf, sql_nosqlList, nodeNumber ) :
    strConf=string.replace(strConf, '{{KAA_SERVICE_NAME}}', DEFAULT_KAA_SERVICE_NAME+str(nodeNumber));
    strConf=string.replace(strConf, '{{SQL_PROVIDER_NAME}}', sql_nosqlList[0]);
    strConf=string.replace(strConf, '{{NOSQL_PROVIDER_NAME}}', sql_nosqlList[1]);
    strConf=string.replace(strConf, '{{ADMIN_PORT}}', str(KaaConfigPorts['ADMIN_PORT']+DEFAULT_INCREASE_PORT_VALUES*nodeNumber));
    strConf=string.replace(strConf, '{{BOOTSTRAP_TCP}}', str(KaaConfigPorts['BOOTSTRAP_TCP']+DEFAULT_INCREASE_PORT_VALUES*nodeNumber));
    strConf=string.replace(strConf, '{{BOOTSTRAP_HTTP}}', str(KaaConfigPorts['BOOTSTRAP_HTTP']+DEFAULT_INCREASE_PORT_VALUES*nodeNumber));
    strConf=string.replace(strConf, '{{OPERATIONS_TCP}}', str(KaaConfigPorts['OPERATIONS_TCP']+DEFAULT_INCREASE_PORT_VALUES*nodeNumber));
    strConf=string.replace(strConf, '{{OPERATIONS_HTTP}}', str(KaaConfigPorts['OPERATIONS_HTTP']+DEFAULT_INCREASE_PORT_VALUES*nodeNumber));
    return strConf;


def configureClusterModeKaa(templateFileName, newFile) :
    insertLine=configurKaaNode(templateFileName, newFile);
    sql_nosqlList = getInputedVariationsDataBases().split("-");
    nodeCount = int(sys.argv[2]);
    for i in range(1, nodeCount):
        kaaService=''
        j = 0;
        with open(templateFileName) as input_data:

            for line in input_data:
                if line.strip() == 'services:':
                    break;

            for line in input_data:
                if line.strip() == 'kaa_lb:':
                    break;
                j+=1;
                kaaService+=line;
        kaaService = configurePorts( kaaService, sql_nosqlList, i );
        kaaAdminUiPorts.append(KaaConfigPorts['ADMIN_PORT']+DEFAULT_INCREASE_PORT_VALUES*i);
        kaaNodeNames.append(DEFAULT_KAA_SERVICE_NAME+str(i));
        insertInFile(newFile, insertLine+(i-1)*j, kaaService);


def insertInFile(file, index, value) :
    f = open(file, "r");
    contents = f.readlines();
    f.close();

    contents.insert(index, value);

    f = open(file, "w");
    contents = "".join(contents);
    f.write(contents);
    f.close();


def setTransportPublicInterface() :
    with open('kaa-example.env', 'r') as file:
        data = file.readlines();
    for i in range(0, len(data)):
        if 'TRANSPORT_PUBLIC_INTERFACE' in data[i]:
            if platform.system() == 'Windows':
                data[i] = 'TRANSPORT_PUBLIC_INTERFACE='+str(getExternalHostWindows())+'\n'
            else:
                data[i] = 'TRANSPORT_PUBLIC_INTERFACE='+str(getExternalHostLinuxMacOs())+'\n'
    with open('kaa-example.env', 'w') as fout:
        fout.write(''.join(data));


def createDefaultConfFileNginx(templateFileName, newFile):
    with open(templateFileName, 'r') as file:
        data = file.readlines();
    for i in range(0, len(data)):
        data[i] = configureDefaultConfFileNginx(data[i])
    with open(newFile, 'w+') as fout:
        fout.write(''.join(data));
    return;


def configureDefaultConfFileNginx(strConf) :
    strConf=string.replace(strConf, '{{NGINX_PORT}}', str(DEFAULT_INTERNAL_ADMIN_PORT));
    if platform.system() == 'Windows':
        nginxHost = str(getExternalHostWindows())
    else:
        nginxHost = str(getExternalHostLinuxMacOs())
    strConf=string.replace(strConf, '{{NGINX_HOST}}', nginxHost);
    return strConf;


def createConfFileNginx(templateFileName, newFile, proxyHost, proxyPort):
    with open(templateFileName, 'r') as file:
        data = file.readlines();
    for i in range(0, len(data)):
        data[i] = configureConfFileNginx(data[i], proxyHost, proxyPort);
    with open(newFile, 'w+') as fout:
        fout.write(''.join(data));
    return;


def configureConfFileNginx(strConf, proxyHost, proxyPort) :
    strConf=string.replace(strConf, '{{PROXY_HOST_KAA}}', str(proxyHost));
    strConf=string.replace(strConf, '{{PROXY_PORT}}', str(proxyPort));
    return strConf;


def stopRunningContainers() :
    runningContainers = commands.getstatusoutput('docker ps -q')[1];
    if runningContainers != '':
        subprocess.call('docker stop $(docker ps -q)', shell=True);
    return;


def removeAvailableContainers() :
    availableContainers = commands.getstatusoutput('docker ps -a -q')[1];
    if availableContainers != '':
        subprocess.call('docker rm $(docker ps -a -q)', shell=True);
    return;


stopRunningContainers();
configureThirdPartyComponents('third-party-docker-compose.yml');
setTransportPublicInterface();
print 'TRANSPORT_PUBLIC_INTERFACE=' + str(getExternalHostLinuxMacOs());

subprocess.call("docker-compose -f third-party-docker-compose.yml up -d", shell=True);

kaaNodesStartCommand = 'docker-compose -f kaa-docker-compose.yml up -d ';

createDefaultConfFileNginx('kaa-nginx-config/default.conf.template', 'kaa-nginx-config/kaa-default.conf')

if len(sys.argv) == 2:
    configurKaaNode('kaa-docker-compose.yml.template', 'kaa-docker-compose.yml');
    subprocess.call((kaaNodesStartCommand+' '.join(kaaNodeNames)), shell=True);
    proxyHost=getExternalHostKaa(kaaNodeNames[0]);
    proxyPort=kaaAdminUiPorts[0];
    createConfFileNginx('kaa-nginx-config/nginx.conf.template', 'kaa-nginx-config/kaa-nginx.conf', proxyHost, proxyPort);
    subprocess.call("docker-compose -f kaa-docker-compose.yml up -d kaa_lb", shell=True);
elif len(sys.argv) == 3:
    try:
        nodeCount = int(sys.argv[2]);
    except:
        print 'This parameter must be Integer';
        sys.exit();
    configureClusterModeKaa('kaa-docker-compose.yml.template', 'kaa-docker-compose.yml');
    subprocess.call((kaaNodesStartCommand+' '.join(kaaNodeNames)), shell=True);
    createConfFileNginx('kaa-nginx-config/nginx.conf.template', 'kaa-nginx-config/kaa-nginx.conf', getExternalHostKaa(kaaNodeNames[0]),  kaaAdminUiPorts[0]);
    for i in range(1, nodeCount):
        proxyHost=getExternalHostKaa(kaaNodeNames[i]);
        proxyPort=kaaAdminUiPorts[i];
        config = configureConfFileNginx(NGINX_TEMPLATE, proxyHost, proxyPort) + '\n';
        insertInFile('kaa-nginx-config/kaa-nginx.conf', 46, config);
    subprocess.call("docker-compose -f kaa-docker-compose.yml up -d kaa_lb", shell=True);

subprocess.call('docker-compose -f kaa-docker-compose.yml ps', shell=True);
subprocess.call('docker-compose -f third-party-docker-compose.yml ps', shell=True);
