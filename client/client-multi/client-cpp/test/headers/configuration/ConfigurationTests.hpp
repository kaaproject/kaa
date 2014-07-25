/*
 * Copyright 2014 CyberVision, Inc.
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

#ifndef CONFIGURATIONTESTS_HPP_
#define CONFIGURATIONTESTS_HPP_

#include "kaa/common/types/CommonRecord.hpp"

#include "headers/configuration/AbstractConfigurationDeltaCreator.hpp"

namespace kaa {

class ICheckConfiguration
{
public:
    virtual ~ICheckConfiguration() {}
    virtual void checkLoadedConfiguration(const ICommonRecord &configuration) = 0;
};

class AbstractProcessConfigurationTest
        : public ICheckConfiguration, public AbstractConfigurationDeltaCreator
{ };

class FullResyncTest : public AbstractProcessConfigurationTest
{
public:
    std::vector<deltaT> fillDelta();
    void checkLoadedConfiguration(const ICommonRecord &configuration);
};

class OverrideItemByUuidTest : public AbstractProcessConfigurationTest
{
public:
    std::vector<deltaT> fillDelta();
    void checkLoadedConfiguration(const ICommonRecord &configuration);
};

class RemoveTwoArrayItemsAndAddNewItemsToArrayTest : public AbstractProcessConfigurationTest
{
public:
    std::vector<deltaT> fillDelta();
    void checkLoadedConfiguration(const ICommonRecord &configuration);
};

class ResetArrayAddMoreItemsOfDiffTypeToArrayTest : public AbstractProcessConfigurationTest
{
public:
    std::vector<deltaT> fillDelta();
    void checkLoadedConfiguration(const ICommonRecord &configuration);
};

class ResetArrayOfDiffTest : public AbstractProcessConfigurationTest
{
public:
    std::vector<deltaT> fillDelta();
    void checkLoadedConfiguration(const ICommonRecord &configuration);
};

} /* namespace kaa */

#endif /* CONFIGURATIONTESTS_HPP_ */
