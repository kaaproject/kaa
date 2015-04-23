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


#include <memory>
#include <thread>
#include <cstdint>
#include <cstdlib>
#include <ctime>
#include <string>

#include <kaa/Kaa.hpp>
#include <kaa/log/ILogStorageStatus.hpp>
#include <kaa/log/DefaultLogUploadStrategy.hpp>
#include <kaa/profile/DefaultProfileContainer.hpp>

using namespace kaa;

#define VEHICLE_TELEMETRY_VEHICLE_ID    "id"
#define VEHICLE_TELEMETRY_MAKER         "maker"
#define VEHICLE_TELEMETRY_MODEL         "model"
#define VEHICLE_TELEMETRY_NICKNAME      "nickname"
#define VEHICLE_TELEMETRY_BRAND         "brand"
#define VEHICLE_TELEMETRY_COLOR         "color"
#define VEHICLE_TELEMETRY_OWNER_ID      12345
#define VEHICLE_TELEMETRY_REG_YEAR      1985
#define VEHICLE_TELEMETRY_TAX_CLASS     "tax_class"
#define VEHICLE_TELEMETRY_VIN_NUMBER    "vin_number"

#define VEHICLE_TELEMETRY_LOG_COUNT        5
#define VEHICLE_TELEMETRY_LOG_HASH_CODE    "hashcode"



class LogUploadStrategy : public DefaultLogUploadStrategy {
public:
    LogUploadStrategy(IKaaChannelManagerPtr manager)
        : DefaultLogUploadStrategy(manager) {}

    virtual LogUploadStrategyDecision isUploadNeeded(ILogStorageStatus& status)
    {
        if (status.getRecordsCount() >= 1) {
            return LogUploadStrategyDecision::UPLOAD;
        }
        return LogUploadStrategyDecision::NOOP;
    }
};

static double getRandDouble() {
    return rand() / rand();
}



int main()
{
    std::srand(std::time(nullptr));

    std::cout << "Vehicle telemetry demo started" << std::endl;
    std::cout << "--= Press Enter to exit =--" << std::endl;

    //Create a Kaa client with the Kaa desktop context.
    Kaa::init();
    IKaaClient& kaaClient =  Kaa::getKaaClient();

    auto profileContainer = std::make_shared<DefaultProfileContainer>();

    KaaProfile profile;
    profile.vehicle_id = VEHICLE_TELEMETRY_VEHICLE_ID;
    profile.vehicle_maker = VEHICLE_TELEMETRY_MAKER;
    profile.vehicle_model = VEHICLE_TELEMETRY_MODEL;
    profile.vehicle_nickname = VEHICLE_TELEMETRY_MODEL;
    profile.vehicle_brand = VEHICLE_TELEMETRY_BRAND;
    profile.vehicle_color = VEHICLE_TELEMETRY_COLOR;
    profile.owner_id = VEHICLE_TELEMETRY_OWNER_ID;
    profile.reg_Year = VEHICLE_TELEMETRY_REG_YEAR;
    profile.tax_Class = VEHICLE_TELEMETRY_TAX_CLASS;
    profile.vin_Number = VEHICLE_TELEMETRY_VIN_NUMBER;

    // Add profile container.
    kaaClient.setProfileContainer(profileContainer);

    // Update profile.
    profileContainer->setProfile(profile);
    kaaClient.updateProfile();

    // Set a custom strategy for uploading logs.
    kaaClient.setLogUploadStrategy(std::make_shared<LogUploadStrategy>(&kaaClient.getChannelManager()));

    // Start the Kaa client and connect it to the Kaa server.
    Kaa::start();

    // Send LOGS_TO_SEND_COUNT logs in a loop.
    size_t logNumber = 0;
    while (logNumber++ < VEHICLE_TELEMETRY_LOG_COUNT) {
        KaaUserLogRecord logRecord;

        logRecord.gyroX = getRandDouble();
        logRecord.gyroY = getRandDouble();
        logRecord.gyroZ = getRandDouble();
        logRecord.accX = getRandDouble();
        logRecord.accY = getRandDouble();
        logRecord.accZ = getRandDouble();
        logRecord.comX = getRandDouble();
        logRecord.comY = getRandDouble();
        logRecord.comZ = getRandDouble();
        logRecord.satellite_fix = getRandDouble();
        logRecord.satellite = getRandDouble();
        logRecord.vehicle_speed = getRandDouble();
        logRecord.engine_speed = getRandDouble();
        logRecord.vehicleHeading = getRandDouble();
        logRecord.compassHeading = getRandDouble();
        logRecord.consumption = getRandDouble();
        logRecord.latitude = getRandDouble();
        logRecord.longitude = getRandDouble();
        logRecord.vehicle_id = VEHICLE_TELEMETRY_VEHICLE_ID;
        logRecord.hash_code = VEHICLE_TELEMETRY_LOG_HASH_CODE;
        logRecord.time = std::chrono::system_clock::now().time_since_epoch().count();

        std::cout << "Going to send " << logNumber << "th record" << std::endl;

        kaaClient.addLogRecord(logRecord);
    }

    // Wait for the Enter key before exiting.
    std::cin.get();

    // Stop the Kaa client and release all the resources which were in use.
    Kaa::stop();

    std::cout << "Vehicle telemetry demo stopped" << std::endl;

    return 0;
}
