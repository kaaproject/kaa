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
from subprocess import check_output, CalledProcessError

variationsOfDatabases = ['mariadb-mongodb', 'mariadb-cassandra', 'postgresql-mongodb', 'postgresql-cassandra'];

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
NGINX_DEPENDS_SERVICES = '      - {{KAA_SERVICE_NAME}}'

cassandraInitScriptLocation = '      - ./../../../../server/common/nosql/cassandra-dao/src/main/resources/cassandra.cql:/cassandra.cql';
cassandraWaitScriptLoc = '      - ./cassandra-image/initKeySpaceCassandra.sh:/initKeySpaceCassandra.sh';
commandEntrypointCassandra = '    command: bash -c \"/initKeySpaceCassandra.sh & /docker-entrypoint.sh cassandra -f\"';

def getExternalHostLinuxMacOs() :
    try:
        return getstatusoutput("ip route get 8.8.8.8 | awk '{print $NF; exit}'")[1];
    except:
        return "N/A"


def getExternalHostWindows() :
    try:
        return getstatusoutput("netsh interface ip show address \"Ethernet\" | findstr \"IP Address\"")[1].rsplit(' ', 1)[1];
    except:
        return "N/A";

def getstatusoutput(cmd):
    try:
        data = check_output(cmd, shell=True, universal_newlines=True)
        status = 0
    except CalledProcessError as ex:
        data = ex.output
        status = ex.returncode
    if data[-1:] == '\n':
        data = data[:-1]
    return status, data

def getInputedVariationsDataBases() :
    isValid = False;
    try:
        for i in range(0, len(variationsOfDatabases)):
            if sys.argv[1] == variationsOfDatabases[i]:
                isValid = True;
                return variationsOfDatabases[i];
        if isValid == False:
            sys.exit();
    except:
        print ("Please choose correct variation of SQL and NoSQL databases \n"+str(variationsOfDatabases));
        sys.exit();


def configureThirdPartyComponents(newFile, template) :
    sql_nosql = getInputedVariationsDataBases().split("-");
    with open(template, 'r') as file:
        data = file.readlines();
    for i in range(0, len(data)):
        if " sql:" in data[i]:
            data[i]=("  "+getInputedVariationsDataBases()+"_sql:"+"\n");
            data[i+1]="    image: "+dockerImages[sql_nosql[0]]+"\n";
        if " nosql:" in data[i]:
            data[i]=("  "+getInputedVariationsDataBases()+"_nosql:"+"\n");
            data[i+1]="    image: "+dockerImages[sql_nosql[1]]+"\n";
        if "env_file" in data[i]:
            data[i]="    env_file: "+sql_nosql[0]+"-sql-example.env\n"
        if (sql_nosql[1]=="cassandra") & ("- nosql-data" in data[i]):
            data.insert(i+1, (cassandraInitScriptLocation+"\n"));
            data.insert(i+2, (cassandraWaitScriptLoc+"\n"));
            data.insert(i+3, (commandEntrypointCassandra+"\n"));
    with open(newFile, 'w+') as fout:
        fout.write(''.join(data));
    return;


def configurKaaNode(templateFileName, newFile) :
    kaaAdminUiPorts.append(KaaConfigPorts['ADMIN_PORT']);
    kaaNodeNames.append(DEFAULT_KAA_SERVICE_NAME+'0');
    sql_nosqlList = getInputedVariationsDataBases().split("-");
    insertLine=0;
    nginxDependsLine=0;
    with open(templateFileName, 'r') as file:
        data = file.readlines();
    for i in range(0, len(data)):
        if 'kaa_lb:' in data[i]:
            insertLine=i;
        if 'depends_on:' in data[i]:
            nginxDependsLine=i-insertLine+1;
        data[i] = configurePorts( data[i], sql_nosqlList, 0 )
    with open(newFile, 'w+') as fout:
        fout.write(''.join(data));
    return [insertLine, nginxDependsLine];


def configurePorts( strConf, sql_nosqlList, nodeNumber ) :
    strConf=str.replace(strConf, '{{KAA_SERVICE_NAME}}', DEFAULT_KAA_SERVICE_NAME+str(nodeNumber));
    strConf=str.replace(strConf, '{{SQL_PROVIDER_NAME}}', sql_nosqlList[0]);
    strConf=str.replace(strConf, '{{NOSQL_PROVIDER_NAME}}', sql_nosqlList[1]);
    strConf=str.replace(strConf, '{{ADMIN_PORT}}', str(KaaConfigPorts['ADMIN_PORT']+DEFAULT_INCREASE_PORT_VALUES*nodeNumber));
    strConf=str.replace(strConf, '{{BOOTSTRAP_TCP}}', str(KaaConfigPorts['BOOTSTRAP_TCP']+DEFAULT_INCREASE_PORT_VALUES*nodeNumber));
    strConf=str.replace(strConf, '{{BOOTSTRAP_HTTP}}', str(KaaConfigPorts['BOOTSTRAP_HTTP']+DEFAULT_INCREASE_PORT_VALUES*nodeNumber));
    strConf=str.replace(strConf, '{{OPERATIONS_TCP}}', str(KaaConfigPorts['OPERATIONS_TCP']+DEFAULT_INCREASE_PORT_VALUES*nodeNumber));
    strConf=str.replace(strConf, '{{OPERATIONS_HTTP}}', str(KaaConfigPorts['OPERATIONS_HTTP']+DEFAULT_INCREASE_PORT_VALUES*nodeNumber));
    return strConf;


def configureClusterModeKaa(templateFileName, newFile) :
    insertLineArray=configurKaaNode(templateFileName, newFile);
    sql_nosqlList = getInputedVariationsDataBases().split("-");
    nodeCount = int(sys.argv[2]);
    for nodeNumber in range(1, nodeCount):
        kaaService=''
        widthOfKaaService = 0;
        with open(templateFileName) as input_data:

            for line in input_data:
                if line.strip() == 'services:':
                    break;

            for line in input_data:
                if line.strip() == 'kaa_lb:':
                    break;
                widthOfKaaService+=1;
                kaaService+=line;
        kaaService = configurePorts( kaaService, sql_nosqlList, nodeNumber );
        kaaAdminUiPorts.append(KaaConfigPorts['ADMIN_PORT']+DEFAULT_INCREASE_PORT_VALUES*nodeNumber);
        kaaNodeNames.append(DEFAULT_KAA_SERVICE_NAME+str(nodeNumber));
        insertInFile(newFile, insertLineArray[0]+(nodeNumber-1)*widthOfKaaService, kaaService);
        strConf=str.replace(str(NGINX_DEPENDS_SERVICES), '{{KAA_SERVICE_NAME}}', DEFAULT_KAA_SERVICE_NAME+str(nodeNumber));
        insertInFile(newFile, (insertLineArray[0]+(nodeNumber-1)*widthOfKaaService)+widthOfKaaService+nodeNumber+insertLineArray[1], (strConf+"\n")); # insert depends for nginx


def insertInFile(file, index, value) :
    f = open(file, "r");
    contents = f.readlines();
    f.close();

    contents.insert(index, value);

    f = open(file, "w");
    contents = "".join(contents);
    f.write(contents);
    f.close();


def configureKaaEnvFile(newFile, templateFileName) :
    sql_nosql = getInputedVariationsDataBases().split("-");
    with open(templateFileName, 'r') as file:
        data = file.readlines();
    for i in range(0, len(data)):
        if 'JDBC_HOST' in data[i]:
            data[i]=str.replace(data[i], '{{sql}}', getInputedVariationsDataBases()+"_sql");
        if sql_nosql[1] == 'cassandra':
            data[i]=str.replace(data[i], '{{cassandra_nosql}}', getInputedVariationsDataBases()+"_nosql");
        elif sql_nosql[1] == 'mongodb':
            data[i]=str.replace(data[i], '{{mongo_nosql}}', getInputedVariationsDataBases()+"_nosql");
        if 'TRANSPORT_PUBLIC_INTERFACE' in data[i]:
            if platform.system() == 'Windows':
                data[i] = 'TRANSPORT_PUBLIC_INTERFACE='+str(getExternalHostWindows())+'\n'
            else:
                data[i] = 'TRANSPORT_PUBLIC_INTERFACE='+str(getExternalHostLinuxMacOs())+'\n'
    with open(newFile, 'w+') as fout:
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
    strConf=str.replace(strConf, '{{NGINX_PORT}}', str(DEFAULT_INTERNAL_ADMIN_PORT));
    if platform.system() == 'Windows':
        nginxHost = str(getExternalHostWindows())
    else:
        nginxHost = str(getExternalHostLinuxMacOs())
    strConf=str.replace(strConf, '{{NGINX_HOST}}', nginxHost);
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
    strConf=str.replace(strConf, '{{PROXY_HOST_KAA}}', str(proxyHost));
    strConf=str.replace(strConf, '{{PROXY_PORT}}', str(proxyPort));
    return strConf;


def stopRunningContainers() :
    print ('Stopping all docker containers');
    runningContainers = getstatusoutput('docker ps -q')[1];
    if runningContainers != '':
        subprocess.call('docker stop $(docker ps -q)', shell=True);
    return;


def removeAvailableContainers() :
    print ('Removing all docker containers');
    availableContainers = getstatusoutput('docker ps -a -q')[1];
    if availableContainers != '':
        subprocess.call('docker rm $(docker ps -a -q)', shell=True);
    return;


stopRunningContainers();
configureThirdPartyComponents('third-party-docker-compose.yml', 'third-party-docker-compose.yml.template');
configureKaaEnvFile('kaa-example.env', 'kaa-example.env.template');
print ('TRANSPORT_PUBLIC_INTERFACE=' + str(getExternalHostLinuxMacOs()));

subprocess.call("docker-compose -f third-party-docker-compose.yml up -d", shell=True);

createDefaultConfFileNginx('kaa-nginx-config/default.conf.template', 'kaa-nginx-config/kaa-default.conf')

if len(sys.argv) == 2:
    configurKaaNode('kaa-docker-compose.yml.template', 'kaa-docker-compose.yml');
    proxyHost=kaaNodeNames[0];
    proxyPort=kaaAdminUiPorts[0];
    createConfFileNginx('kaa-nginx-config/nginx.conf.template', 'kaa-nginx-config/kaa-nginx.conf', proxyHost, proxyPort);
    subprocess.call("docker-compose -f kaa-docker-compose.yml up -d", shell=True);
elif len(sys.argv) == 3:
    try:
        nodeCount = int(sys.argv[2]);
    except:
        print ('This parameter must be Integer');
        sys.exit();
    configureClusterModeKaa('kaa-docker-compose.yml.template', 'kaa-docker-compose.yml');
    createConfFileNginx('kaa-nginx-config/nginx.conf.template', 'kaa-nginx-config/kaa-nginx.conf', kaaNodeNames[0],  kaaAdminUiPorts[0]);
    for i in range(1, nodeCount):
        proxyHost=kaaNodeNames[i];
        proxyPort=kaaAdminUiPorts[i];
        config = configureConfFileNginx(NGINX_TEMPLATE, proxyHost, proxyPort) + '\n';
        insertInFile('kaa-nginx-config/kaa-nginx.conf', 46, config);
    subprocess.call("docker-compose -f kaa-docker-compose.yml up -d", shell=True);

subprocess.call('docker-compose -f kaa-docker-compose.yml ps', shell=True);
subprocess.call('docker-compose -f third-party-docker-compose.yml ps', shell=True);
