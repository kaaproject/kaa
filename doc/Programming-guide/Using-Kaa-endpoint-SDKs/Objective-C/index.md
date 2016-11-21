---
layout: page
title: Objective-C
permalink: /:path/
sort_idx: 30
---

{% include variables.md %}

* TOC
{:toc}

This guide describes how to use the [Kaa Objective-C SDK]({{root_url}}Glossary/#endpoint-sdk) within your application.

## Requirements

Make sure that the following components are installed on your machine:

- Xcode 7.x or later.
- CocoaPods 1.x.x or later.

## Installation

To install a Kaa SDK in your project as a third-party framework, use the [CocoaPods](https://cocoapods.org/) dependency manager.

To generate your Objective-C SDK:

<ol>
<li markdown="1">
Click the **Source** button.
A project source download window will open.
Click **Ok** and download the `.tar.gz` file.
</li>
<li markdown="1">
Unpack the downloaded archive into your project directory and run the following command from that directory.

```	
pod init
```	

This will create a file named `Podfile`.
You can also create it manually using a text editor.

If you create the `Podfile` file using the `pod init` command, add the following line to include your Kaa SDK as a pod.

```ruby
pod 'Kaa', :path => './libs/kaa-ep-sdk'
```

If you create the `Podfile` file using a text editor, make sure to include the following content.

```ruby
use_frameworks!

target '<PROJECT_NAME>' do
    pod 'Kaa', :path => './libs/kaa-ep-sdk'
end
```

In the example above, **PROJECT_NAME** is the name of your project target.

>**TIP:** If you want to deploy your Kaa instance in a Swift project, add the `use_frameworks!` command as the first line in your `Podfile` file.
>This step is required if you don't want to import the bridging headers into your project.
{:.tip}
</li>
<li markdown="1">
Run the following commands.

``` bash
pod update
pod install
```

A log message will be created upon successful installation.
</li>
<li markdown="1">
Open the `.xworkspace` file in Xcode.
Your Kaa SDK is listed in the **Development Pods** group of the project tree.
</li>
</ol>

## Using endpoint SDK

To build your project using the Kaa Objective-C SDK:

<ol>
<li markdown="1">
Open the auto-generated `ViewController.h` file and create a property named `kaaClient`.

```objc
@property (nonatomic, strong) id<KaaClient> kaaClient;
```
You can skip the declaration of a `kaaClient` variable and make it accessible to the controller only, but it should be visible and available outside the `ViewController` implementation.
</li>
<li markdown="1">
After the declaration, you will receive the 'undeclared type' error.
To avoid this, make a forward declaration of the `KaaClient` protocol.

``` objc
@protocol KaaClient;
```
</li>
<li markdown="1">
Open the `ViewController.m` file and add the `#import <Kaa/Kaa.h>` header before the controller implementation.
Make sure that your controller conforms to the `KaaClientStateDelegate` and `ProfileContainer` protocols, and that all required methods are implemented.
</li>
</ol>

After this, Kaa SDK provides you with the **Kaa** class factory used to create a new instance of the [Kaa client]({{root_url}}Glossary/#kaa-client).

``` objc
self.kaaClient = [KaaClientFactory clientWithContext:[[DefaultKaaPlatformContext alloc] init] stateDelegate:self];
```

Kaa Objective-C SDK is now successfully embedded into your project.

You can find auto-generated docs for Kaa Objective C SDK [here]({{site.baseurl}}/autogen-docs/client-objective-c/{{version}}/).

## Logging

Kaa Objective-C endpoint SDK uses [CocoaLumberjack](https://github.com/CocoaLumberjack/CocoaLumberjack) framework for logging.
By default, the SDK logs warnings and errors only.

To change current SDK logging level, open the `Kaa/KaaLogging.m` file and assign one of the following constants to the `ddLogLevel` variable:

* `DDLogLevelVerbose`
* `DDLogLevelDebug`
* `DDLogLevelInfo`
* `DDLogLevelWarning`
* `DDLogLevelError`
* `DDLogLevelAll`
* `DDLogLevelOff`
