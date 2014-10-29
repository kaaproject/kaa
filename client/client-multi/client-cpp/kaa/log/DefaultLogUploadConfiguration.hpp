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

#ifndef DEFAULTLOGUPLOADCONFIGURATION_HPP_
#define DEFAULTLOGUPLOADCONFIGURATION_HPP_

#include <cstdint>
#include "kaa/log/ILogUploadConfiguration.hpp"

namespace kaa {

class DefaultLogUploadConfiguration : public ILogUploadConfiguration {
public:
    DefaultLogUploadConfiguration()
            : blockSize_(DEFAULT_BLOCK_SIZE)
            , maxStorageVolume_(DEFAULT_MAX_STORAGE_VOLUME)
            , volumeThreshold_(DEFAULT_VOLUME_THRESHOLD) {}

    DefaultLogUploadConfiguration(size_t blockSize, size_t maxStorageVolume, size_t volumeThreshold)
        : blockSize_(blockSize)
        , maxStorageVolume_(maxStorageVolume)
        , volumeThreshold_(volumeThreshold) {}

    std::size_t  getBlockSize()          const { return blockSize_; }
    std::size_t  getMaxStorageVolume()   const { return maxStorageVolume_; }
    std::size_t  getVolumeThreshold()    const { return volumeThreshold_; }

private:
    std::size_t blockSize_;
    std::size_t maxStorageVolume_;
    std::size_t volumeThreshold_;

    static const std::size_t DEFAULT_BLOCK_SIZE          = 8192;                     //8 Kb
    static const std::size_t DEFAULT_MAX_STORAGE_VOLUME  = 1024*1024;                //1 Mb
    static const std::size_t DEFAULT_VOLUME_THRESHOLD    = DEFAULT_BLOCK_SIZE * 4;   // 32 Kb
};

}  // namespace kaa

#endif /* DEFAULTLOGUPLOADCONFIGURATION_HPP_ */
