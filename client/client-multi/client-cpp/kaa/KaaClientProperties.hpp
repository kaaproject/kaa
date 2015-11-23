/*
 * Copyright 2014-2015 CyberVision, Inc.
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

#ifndef KAACLIENTPROPERTIES_HPP_
#define KAACLIENTPROPERTIES_HPP_

#include <string>
#include <unordered_map>

namespace kaa {

class KaaClientProperties {
public:
    KaaClientProperties()
    {
        initByDefaults();
    }

    void setWorkingDirectoryPath(const std::string& path);
    std::string getWorkingDirectoryPath() const
    {
        return getProperty(PROP_WORKING_DIR, DEFAULT_WORKING_DIR);
    }

    void setStateFileName(const std::string& fileName);
    std::string getStateFileName() const
    {
        return getWorkingDirectoryPath() + getProperty(PROP_STATE_FILE, DEFAULT_STATE_FILE);
    }

    void setPublicKeyFileName(const std::string& fileName);
    std::string getPublicKeyFileName() const
    {
        return getWorkingDirectoryPath() + getProperty(PROP_PUB_KEY_FILE, DEFAULT_PUB_KEY_FILE);
    }

    void setPrivateKeyFileName(const std::string& fileName);
    std::string getPrivateKeyFileName() const
    {
        return getWorkingDirectoryPath() + getProperty(PROP_PRIV_KEY_FILE, DEFAULT_PRIV_KEY_FILE);
    }

    void setLogsDatabaseFileName(const std::string& fileName);
    std::string getLogsDatabaseFileName() const
    {
        return getWorkingDirectoryPath() + getProperty(PROP_LOGS_DB, DEFAULT_LOGS_DB);
    }

    void setConfigurationFileName(const std::string& fileName);
    std::string getConfigurationFileName() const
    {
        return getWorkingDirectoryPath() + getProperty(PROP_CONF_FILE, DEFAULT_CONF_FILE);
    }

    void setProperty(const std::string& name, const std::string& value);
    std::string getProperty(const std::string& name, const std::string& defaultValue = std::string()) const;

public:
    static const std::string PROP_WORKING_DIR;
    static const std::string PROP_STATE_FILE;
    static const std::string PROP_PUB_KEY_FILE;
    static const std::string PROP_PRIV_KEY_FILE;
    static const std::string PROP_LOGS_DB;
    static const std::string PROP_CONF_FILE;

    static const std::string DEFAULT_WORKING_DIR;
    static const std::string DEFAULT_STATE_FILE;
    static const std::string DEFAULT_PUB_KEY_FILE;
    static const std::string DEFAULT_PRIV_KEY_FILE;
    static const std::string DEFAULT_LOGS_DB;
    static const std::string DEFAULT_CONF_FILE;

private:
    void initByDefaults();

private:
    std::unordered_map<std::string, std::string> properties_;
};

} /* namespace kaa */

#endif /* KAACLIENTPROPERTIES_HPP_ */
