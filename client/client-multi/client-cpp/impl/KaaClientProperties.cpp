/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "kaa/KaaClientProperties.hpp"

#include <sstream>

#include "kaa/KaaDefaults.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/log/LogStorageConstants.hpp"

namespace kaa {

#if __GNUC__
    static const char FILE_SEPARATOR = '/';
#else
    static const char FILE_SEPARATOR = '\\';
#endif

const std::string KaaClientProperties::PROP_WORKING_DIR = "kaa.work_dir";
const std::string KaaClientProperties::PROP_STATE_FILE = "kaa.state.file";
const std::string KaaClientProperties::PROP_PUB_KEY_FILE = "kaa.keys.public";
const std::string KaaClientProperties::PROP_PRIV_KEY_FILE = "kaa.keys.private";
const std::string KaaClientProperties::PROP_LOGS_DB = "kaa.logs.db_file";
const std::string KaaClientProperties::PROP_CONF_FILE = "kaa.conf.file";
const std::string KaaClientProperties::PROP_CLIENT_ID = "kaa.conf.client_id";
const std::string KaaClientProperties::PROP_LOG_FILE_NAME = "kaa.log.file.name";

const std::string KaaClientProperties::DEFAULT_WORKING_DIR = std::string(".") + &FILE_SEPARATOR;
const std::string KaaClientProperties::DEFAULT_STATE_FILE = CLIENT_STATUS_FILE_LOCATION;
const std::string KaaClientProperties::DEFAULT_PUB_KEY_FILE = CLIENT_PUB_KEY_LOCATION;
const std::string KaaClientProperties::DEFAULT_PRIV_KEY_FILE = CLIENT_PRIV_KEY_LOCATION;
const std::string KaaClientProperties::DEFAULT_LOGS_DB = "logs.db";
const std::string KaaClientProperties::DEFAULT_CONF_FILE = "configuration.bin";
const std::string KaaClientProperties::DEFAULT_CLIENT_ID = "client_";
const std::string KaaClientProperties::DEFAULT_LOG_FILE_NAME = "";

static std::string getDefaultClientId()
{
   static size_t counter = 0;
   std::stringstream ss;
   ss << KaaClientProperties::DEFAULT_CLIENT_ID << ++counter;
   return ss.str();
}

static void checkEmptyness(const std::string& value, const std::string& errorMessage)
{
    if (value.empty()) {
        throw KaaException(errorMessage);
    }
}

std::string KaaClientProperties::getProperty(const std::string& name, const std::string& defaultValue) const
{
    auto it = properties_.find(name);
    if (it != properties_.end()) {
        return it->second;
    }
    return defaultValue;
}

void KaaClientProperties::setProperty(const std::string& name, const std::string& value)
{
    checkEmptyness(name, "Empty property name");
    properties_[name] = value;
}

void KaaClientProperties::initByDefaults()
{
    properties_.clear();
    properties_.insert(std::make_pair(PROP_WORKING_DIR, DEFAULT_WORKING_DIR));
    properties_.insert(std::make_pair(PROP_STATE_FILE, DEFAULT_STATE_FILE));
    properties_.insert(std::make_pair(PROP_PUB_KEY_FILE, DEFAULT_PUB_KEY_FILE));
    properties_.insert(std::make_pair(PROP_PRIV_KEY_FILE, DEFAULT_PRIV_KEY_FILE));
    properties_.insert(std::make_pair(PROP_LOGS_DB, DEFAULT_LOGS_DB));
    properties_.insert(std::make_pair(PROP_CONF_FILE, DEFAULT_CONF_FILE));
    properties_.insert(std::make_pair(PROP_CLIENT_ID, getDefaultClientId()));
    properties_.insert(std::make_pair(PROP_LOG_FILE_NAME, DEFAULT_LOG_FILE_NAME));
}

void KaaClientProperties::setWorkingDirectoryPath(const std::string& path)
{
    checkEmptyness(path, "Empty value of working directory path");

    if (path[path.size() - 1] != FILE_SEPARATOR) {
        std::string editedPath = path + &FILE_SEPARATOR;
        setProperty(PROP_WORKING_DIR, editedPath);
    } else {
        setProperty(PROP_WORKING_DIR, path);
    }
}

void KaaClientProperties::setClientId(const std::string& clientId)
{
    checkEmptyness(clientId, "Empty client id");
    setProperty(PROP_CLIENT_ID, clientId);
}

void KaaClientProperties::setLogFileName(const std::string& logFileName)
{
    checkEmptyness(logFileName, "Empty log file name");
    setProperty(PROP_LOG_FILE_NAME, logFileName);
}

void KaaClientProperties::setStateFileName(const std::string& fileName)
{
    checkEmptyness(fileName, "Empty value of state file name");
    setProperty(PROP_STATE_FILE, fileName);
}

void KaaClientProperties::setPublicKeyFileName(const std::string& fileName)
{
    checkEmptyness(fileName, "Empty value of public key file name");
    setProperty(PROP_PUB_KEY_FILE, fileName);
}

void KaaClientProperties::setPrivateKeyFileName(const std::string& fileName)
{
    checkEmptyness(fileName, "Empty value of private key file name");
    setProperty(PROP_PRIV_KEY_FILE, fileName);
}

void KaaClientProperties::setLogsDatabaseFileName(const std::string& fileName)
{
    checkEmptyness(fileName, "Empty value of logs database name");
    setProperty(PROP_LOGS_DB, fileName);
}

void KaaClientProperties::setConfigurationFileName(const std::string& fileName)
{
    checkEmptyness(fileName, "Empty value of configuration file name");
    setProperty(PROP_CONF_FILE, fileName);
}

} /* namespace kaa */
