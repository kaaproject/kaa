---
layout: page
title: Objective-C
permalink: /:path/
sort_idx: 30
---

**Table of Contents**

* TOC
{:toc}

This guide describes how to use the Kaa Objective-C SDK into your application, including base API overview and
external links to demo applications and programming guides, which cover a pretty big amount of KAA architecture
topics.

# Requirements

First, ensure that the following components are installed on your machine: 

- Xcode 7.x or later 
- CocoaPods 1.x.x or later

# Installation

The main method for installing Kaa SDK into a project as a third-party framework is using [CocoaPods](https://cocoapods.org/) dependency manager.

First of all, before start to develop your application, you need to [generate Endpoint SDK](%s:https://balldir.github.io/kaa/docs/current/:{{root_url}}:g) for the target platform - Objective-C. 
After pressing on _download_ button, you'll get an archived file wrapped with __.tar.gz__ extension. 
After downloading, please, move this archive into your project directory and extract.

After that, open a console, move into your project directory by _cd_ command. 
When you're getting into required directory, write ``pod init`` into console in working directory or create a `Podfile` manually, using your preferable text editor.

If you create __Podfile__, using a command above, add a line to include Kaa SDK as pod:
``` ruby
pod 'Kaa', :path => './libs/kaa-ep-sdk'
```

If you created a __Podfile__ by hand, first of all, you must fill it with as below:
``` ruby 
use_frameworks!

target '<PROJECT_NAME>' do
    pod 'Kaa', :path => './libs/kaa-ep-sdk'
end
```
where __PROJECT_NAME__ - name of your project's target.

You must add ``use_frameworks!`` command as first line into your __Podfile__ if you will use Kaa into swift project. 
This step is mandatory if you don't want to bridge headers apart into your project.

When you complete all steps before, complete next command chain:
``` bash
pod update 
pod install
```

After that, you'll see a log message within extra frameworks success installation used in Kaa.
If you don't get any errors, you can open your project with Xcode, running __xworkspace__ file.
In your project tree you can find Kaa into __Development Pods__ group, located  Pods target.

# Using endpoint SDK 

So, let's begin to use Kaa SDK. 
Development will be carried out in one of auto-created files, e.g. __ViewController__.

Open `ViewController.h` and create a new property, named `kaaClient` as below:
```objc
@property (nonatomic, strong) id<KaaClient> kaaClient;
```
Of course, you might not to declare kaaClient variable and make it accessible only to the controller, but as desirable, it should be visible and available outside of ViewController implementation.

After declaration you, obviously, get an error about undeclared type. To get it correct, make a forward declaration of KaaClient protocol. 

``` objc
@protocol KaaClient;
```

OK, let's move forward to __ViewController.m__ file.

The next step is to add  Kaa header to the top of the file, before controller implementation. 
It should look as  ``#import <Kaa/Kaa.h>``.
After that, conform your controller to the __KaaClientStateDelegate__ and __ProfileContainer__ protocols and implement all required methods.

After all steps, SDK provides you with class factory Kaa which is responsible for creating new instance of the client.
So, here's an example, showing how it should be:
``` objc
self.kaaClient = [KaaClientFactory clientWithContext:[[DefaultKaaPlatformContext alloc] init] stateDelegate:self];
```

Making all these steps, build your project. 
If all made correct, the project will build without errors. 
Congratulations, you have embedded Kaa into your project.

---

Under the hood Objective-C endpoint SDK uses [CocoaLumberjack](https://github.com/CocoaLumberjack/CocoaLumberjack) framework for logging. 
By default SDK logs only warnings and errors. 
In order to change current SDK logging level open `Kaa/KaaLogging.m` file and assign to `ddLogLevel` variable one of the following constants:
* `DDLogLevelVerbose`
* `DDLogLevelDebug`
* `DDLogLevelInfo`
* `DDLogLevelWarning`
* `DDLogLevelError`
* `DDLogLevelAll`
* `DDLogLevelOff`

# Demo application

To better familiarize yourself with Kaa Objective-C SDK, you may look at our demo applications.

Find the demo source code in our [sample-apps](https://github.com/kaaproject/sample-apps) repository on GitHub.
