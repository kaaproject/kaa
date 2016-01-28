pod setup
pod install
xcodebuild -workspace Kaa.xcworkspace/ -scheme libKaa.a -sdk iphonesimulator -destination 'platform=iOS Simulator,name=iPhone 6' test
