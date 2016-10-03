
Pod::Spec.new do |s|


  s.name         = "Kaa"
  s.version      = "0.9.0"
  s.summary      = "Kaa iOS SDk"


  s.description  = <<-DESC

Kaa is a production-ready, multi-purpose middleware platform for building complete end-to-end IoT solutions, connected applications, and smart products.
                   DESC

  s.homepage     = "http://www.kaaproject.org"


  # ―――  Spec License  ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #

s.license      = {
                    :type => 'Apache License, Version 2.0',
                    :text => '
                      Licensed under the Apache License, Version 2.0 (the "License");
                      you may not use this file except in compliance with the License.
                      You may obtain a copy of the License at
                         http://www.apache.org/licenses/LICENSE-2.0
                      Unless required by applicable law or agreed to in writing, software
                      distributed under the License is distributed on an "AS IS" BASIS,
                      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                      See the License for the specific language governing permissions and
                      limitations under the License.
                    '
                  }

  s.author             = { "Anton Vilimets" => "avilimets@cybervisiontech.com" }
 

  
  s.platform     = :ios
  s.ios.deployment_target = "8.0"

  s.source       = { :path => "./Kaa"}


  # ――― Source Code 

  s.source_files  = "Kaa/**/*", "Kaa/**/**/*"

  s.private_header_files = "Kaa/security/NSData+CommonCrypto.h"


  # ――― Resources ―――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #


  # ――― Project Linking ―――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #

  s.frameworks = "Security", "SystemConfiguration"


  # ――― Project Settings ――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #

  s.dependency "CocoaLumberjack", "2.3"
  s.dependency "AFNetworking", "~> 3.x"
  s.dependency "sqlite3"

  s.prefix_header_contents = %(
#import <Availability.h>

#ifndef Kaa_prefix_pch
#define Kaa_prefix_pch

#ifdef __OBJC__
    #import "CocoaLumberjack.h"
#endif

#endif)



end
