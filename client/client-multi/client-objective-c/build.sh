#!/bin/sh
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


generate_docs() {
	appledoc --project-name "Kaa client Objective-C SDK" --project-company CyberVision --output ./ --no-create-docset --ignore ./*.m --explicit-crossref --verbose 2 --index-desc mainpage.html Kaa
}

compile() {
	pod setup
	pod install
	xcodebuild -workspace Kaa.xcworkspace -scheme Kaa clean build
}

test() {
	pod setup
	pod install
	xcodebuild -workspace Kaa.xcworkspace -scheme Kaa -sdk iphonesimulator -destination 'platform=iOS Simulator,name=iPhone 6' test
}

help() {
	echo "Usage: ./build.sh [OPTION]
    compile			Compile Kaa client Objective-C SDK
    test			Run tests
    doc 			Generate documentation"
}

if [ "$#" -eq 0 ]; then
	help
fi

if [ "$#" -eq 1 ]; then
	case $1 in
		"doc") generate_docs
		;;
		"compile") compile
		;;
		"test") test
		;;
		"help") help
		;;
		*) help
		;;
	esac
fi
