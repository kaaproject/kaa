//
// Copyright 2014 CyberVision, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

use kaa;
db.endpoint_profile.find(
    {
        srv_profile: {$exists: true}
    }).forEach(function(obj) {
        if(obj.srv_profile.length != 0) {
            obj.srv_profile = JSON.parse(obj.srv_profile);
        } else {
            obj.srv_profile = new Object();
        }
        db.endpoint_profile.save(obj);
    }
);
