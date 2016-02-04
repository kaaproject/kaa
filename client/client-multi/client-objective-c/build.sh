#!/bin/sh

generate_docs() {
	appledoc --project-name "Kaa client Objective-C SDK" --project-company CyberVision --output ./ --no-create-docset --explicit-crossref --verbose 2 --index-desc mainpage.html Kaa
}

compile() {
	pod setup
	pod install
	xcodebuild -workspace Kaa.xcworkspace/ -scheme libKaa.a clean build
}

test() {
	pod setup
	pod install
	xcodebuild -workspace Kaa.xcworkspace/ -scheme libKaa.a -sdk iphonesimulator -destination 'platform=iOS Simulator,name=iPhone 6' test
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
