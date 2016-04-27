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

    /**
     * @brief Set client ID.
     *
     * @param [in] clientId Client Id
     *
     * A client id is used for logging purposes.
     * Kaa supports a several client instances hence there is a need
     * to distinguish them. Client id is introduced for this purpose.
     * Each log line begins with client id:
     *
     * [cliend id] [date] ....
     *
     * By default client id is provided.
     */
    void setClientId(const std::string& clientId);

    /**
     * @brief Returns client id.
     *
     * @return Client Id.
     */
    std::string getClientId() const
    {
        return getProperty(PROP_CLIENT_ID, DEFAULT_CLIENT_ID);
    }

    /**
     * @brief Set file name logs will be written to.
     *
     * @param[in] logFileName  The name of the file. If empty -
     * - logs will be written to stdout. Otherwise - they will be
     * duplicated to the specified file.
     */
    void setLogFileName(const std::string& logFileName);

    /**
     * @brief Returns current path to the file which contains logs.
     *
     * @return The path to the file that stores logs.
     * if the returned string is emty, logs are not written to a file.
     */
    std::string getLogFileName() const
    {
        auto fileName = getProperty(PROP_LOG_FILE_NAME, DEFAULT_LOG_FILE_NAME);
        return fileName.empty() ? fileName : getWorkingDirectoryPath() + fileName;
    }

    /**
     * @brief Sets working directory path.
     *
     * @param[in] path The path to the working directory.
     *
     * The path to the working directory is used to
     * specify the directory where client's state and keys files
     * will be stored.
     */
    void setWorkingDirectoryPath(const std::string& path);

    /**
     * @brief Returns a path to the client's working directory
     *
     * @return The path to the client's working directory.
     */
    std::string getWorkingDirectoryPath() const
    {
        return getProperty(PROP_WORKING_DIR, DEFAULT_WORKING_DIR);
    }

    /**
     * @brief Sets state file name.
     *
     * @param[in] fileName The name of the client's state file.
     *
     * State file name is used to store client state info.
     */
    void setStateFileName(const std::string& fileName);

    /**
     * @brief Returns the path to the state file name.
     *
     * @return The path to the client's status file.
     */
    std::string getStateFileName() const
    {
        return getWorkingDirectoryPath() + getProperty(PROP_STATE_FILE, DEFAULT_STATE_FILE);
    }

    /**
     * @brief Sets public key file name.
     *
     * @param[in] fileName The name of the client's public key file.
     *
     */
    void setPublicKeyFileName(const std::string& fileName);

    /**
     * @brief Returns the path to the client's public key file.
     *
     * @return The path to the public key file.
     */
    std::string getPublicKeyFileName() const
    {
        return getWorkingDirectoryPath() + getProperty(PROP_PUB_KEY_FILE, DEFAULT_PUB_KEY_FILE);
    }

    /**
     * @brief Sets private key file name.
     *
     * @param[in] fileName The name of the client's private key file.
     *
     */
    void setPrivateKeyFileName(const std::string& fileName);

    /**
     * @brief Returns the path to the client's private key file.
     *
     * @return The path to the private key file.
     */
    std::string getPrivateKeyFileName() const
    {
        return getWorkingDirectoryPath() + getProperty(PROP_PRIV_KEY_FILE, DEFAULT_PRIV_KEY_FILE);
    }

    /**
     * @brief Sets database file name.
     *
     * @param[in] fileName The name of the database where logs will be stored.
     *
     */
    void setLogsDatabaseFileName(const std::string& fileName);

    /**
     * @brief Returns the path to the database's name where the logs are stored.
     *
     * @return The path to the database.
     */
    std::string getLogsDatabaseFileName() const
    {
        return getWorkingDirectoryPath() + getProperty(PROP_LOGS_DB, DEFAULT_LOGS_DB);
    }

    /**
     * @brief Sets configuration file name.
     *
     * @param[in] fileName Configuration file name.
     */
    void setConfigurationFileName(const std::string& fileName);

    /**
     * @brief Returns the path to the configuration file.
     *
     * @return The path to the configuration file.
     */
    std::string getConfigurationFileName() const
    {
        return getWorkingDirectoryPath() + getProperty(PROP_CONF_FILE, DEFAULT_CONF_FILE);
    }

    /**
     * @brief Sets property.
     *
     * @param[in] name Property name.
     * @param[in] value Property value.
     */
    void setProperty(const std::string& name, const std::string& value);

    /**
     * @brief Returns propety value.
     *
     * @param[in] name Property name.
     * @param[in] defaultValue The value which will be returned in
     * case if the property has no value set.
     */
    std::string getProperty(const std::string& name, const std::string& defaultValue = std::string()) const;

public:
    static const std::string PROP_WORKING_DIR;
    static const std::string PROP_STATE_FILE;
    static const std::string PROP_PUB_KEY_FILE;
    static const std::string PROP_PRIV_KEY_FILE;
    static const std::string PROP_LOGS_DB;
    static const std::string PROP_CONF_FILE;
    static const std::string PROP_CLIENT_ID;
    static const std::string PROP_LOG_FILE_NAME;

    static const std::string DEFAULT_WORKING_DIR;
    static const std::string DEFAULT_STATE_FILE;
    static const std::string DEFAULT_PUB_KEY_FILE;
    static const std::string DEFAULT_PRIV_KEY_FILE;
    static const std::string DEFAULT_LOGS_DB;
    static const std::string DEFAULT_CONF_FILE;
    static const std::string DEFAULT_CLIENT_ID;
    static const std::string DEFAULT_LOG_FILE_NAME;

private:
    void initByDefaults();

private:
    std::unordered_map<std::string, std::string> properties_;
};

} /* namespace kaa */

#endif /* KAACLIENTPROPERTIES_HPP_ */
