/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
#include <boost/asio.hpp>
#include <kaa/Kaa.hpp>
#include <kaa/IKaaClient.hpp>
#include <kaa/configuration/manager/IConfigurationReceiver.hpp>
#include <kaa/configuration/storage/FileConfigurationStorage.hpp>
#include <kaa/log/strategies/RecordCountLogUploadStrategy.hpp>
#include <memory>
#include <string>
#include <cstdint>

class ConfigurationCollection : public kaa::IConfigurationReceiver {
public:
    ConfigurationCollection()
        : kaaClient_(kaa::Kaa::newClient())
        , samplePeriod_(0)
        , interval_(samplePeriod_)
        , timer_(service_, interval_) 
    {
        // Set a custom strategy for uploading logs.
        kaaClient_->setLogUploadStrategy(
            std::make_shared<kaa::RecordCountLogUploadStrategy>(1, kaaClient_->getKaaClientContext()));
        // Set up a configuration subsystem.
        kaa::IConfigurationStoragePtr storage(
            std::make_shared<kaa::FileConfigurationStorage>(std::string(savedConfig_)));
        kaaClient_->setConfigurationStorage(storage);
        kaaClient_->addConfigurationListener(*this);
        auto handlerUpdate = [this](const boost::system::error_code& err) 
        {
            this->update();
        };
        timer_.async_wait(handlerUpdate);
    }
    
    ~ConfigurationCollection() 
    {
        // Stop the Kaa endpoint.
        kaaClient_->stop();
        std::cout << "Simple client demo stopped" << std::endl;
    }
    
    void run() 
    {
        // Run the Kaa endpoint.
        kaaClient_->start();
        // Read default sample period
        samplePeriod_ = kaaClient_->getConfiguration().samplePeriod;
        std::cout << "Default sample period: " << samplePeriod_<< std::endl;
        // Default sample period
        timer_.expires_from_now(boost::posix_time::seconds(samplePeriod_));
        service_.run();
    }
    
private:
    static constexpr auto savedConfig_ = "saved_config.cfg";
    std::shared_ptr<kaa::IKaaClient> kaaClient_;
    int32_t samplePeriod_;
    boost::asio::io_service service_;
    boost::posix_time::seconds interval_;
    boost::asio::deadline_timer timer_;
    
    int32_t getTemperature() 
    {
        // For sake of example random data is used
        return rand() % 10 + 25;
    }
    
    void update() 
    {
        kaa::KaaUserLogRecord logRecord;
        logRecord.temperature = getTemperature();
        // Send value of temperature
        kaaClient_->addLogRecord(logRecord);
        // Show log
        std::cout << "Sampled temperature: " << logRecord.temperature << std::endl;
        // Set a new  period of the send data
        timer_.expires_at(timer_.expires_at() + boost::posix_time::seconds(samplePeriod_));
        // Posts the timer event
        auto handlerUpdate = [this](const boost::system::error_code& err) 
        {
            this->update();
        };
        timer_.async_wait(handlerUpdate);
    }
    
    void updateConfiguration(const kaa::KaaRootConfiguration &configuration) 
    {
        std::cout << "Received configuration data. New sample period: "
            << configuration.samplePeriod << " seconds" << std::endl;
        samplePeriod_ = configuration.samplePeriod;
    }
    
    void onConfigurationUpdated(const kaa::KaaRootConfiguration &configuration) 
    {
        updateConfiguration(configuration);
    }
};

int main() 
{
    ConfigurationCollection configurationCollection;
    
    try {
        // It does control of the transmit and receive data
        configurationCollection.run();
    } catch (std::exception& e) {
        std::cout << "Exception: " << e.what();
    }
    return 0;
}
