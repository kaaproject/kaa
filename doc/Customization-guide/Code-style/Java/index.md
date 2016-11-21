---
layout: page
title: Java
permalink: /:path/
sort_idx: 10
---

{% include variables.md %}

* TOC
{:toc}

The entire Java source code of the Kaa project must conform to the [Google Java Style](https://google.github.io/styleguide/javaguide.html).  
This guide describes some tools which can be used to ensure proper style and format of the Java source code.


## IntelliJ IDEA plugin
[Checkstyle](https://github.com/jshiell/checkstyle-idea) plugin installation:

1. Go to **File>Settings>IDE Settings>plugins**.
2. Click **Browse repositories**.
3. Type *Checkstyle-idea* and install the plugin.
4. Go to **File>Settings>IDE Settings>CheckStyle**.
5. Add the configuration file (`checkstyle.xml`) from the root of the Kaa project.

Plugin configuration:

1. Go to **Analyze>Inspect code**.
2. Create a new profile with the checkstyle inspector.

Plugin usage:

1. Go to **Analyze>Inspect code**.
2. Select the inspection scope and the inspection profile.

## Eclipse plugin
Follow [tutorial](http://eclipse-cs.sourceforge.net/#!/install) to install the checkstyle plugin.

## Maven plugin
The [check style maven plugin](https://maven.apache.org/plugins/maven-checkstyle-plugin/usage.html) is used to continuously verify the style of the Java code. By default, it is already configured to use the Google coding conventions. The configuration is located in the root of the Kaa project (`checkstyle.xml`).

## Automatic code formatter
To automate formatting of the Java code, it is recommended that you use IntelliJ IDEA with appropriate code style settings.
To install the code style settings:

1. Click **File** > **Settings** > **Editor** > **Code Style**.

2. Click **Manage** and import the `checkstyle.xml` configuration file from the Kaa project root directory.