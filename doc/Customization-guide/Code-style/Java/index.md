---
layout: page
title: Java
permalink: /:path/
sort_idx: 10
---

* TOC
{:toc}

We follow [Google Java Style](https://google.github.io/styleguide/javaguide.html) in Kaa Java SDK.  
This guide describes tools which we use to check and format a code.

# IntelliJ IDEA plugin
[Checkstyle](https://github.com/jshiell/checkstyle-idea) plugin installation:
1. Go to *File>Settings>IDE Settings>plugins*.
2. Click on the *Browse repositories* button.
3. Type *Checkstyle-idea* and install the plugin.
4. Download the *[configuration for the Google coding conventions](https://github.com/checkstyle/checkstyle/blob/master/src/main/resources/google_checks.xml)*.
5. Go to *File>Settings>IDE Settings>CheckStyle*.
6. Add the configuration file (`google_checks.xml`).

Plugin configuration:
1. Go to *Analyze>Inspect code*.
2. Create a new profile with the checkstyle inspector.

Plugin usage:
1. Go to *Analyze>Inspect code*.
2. Select an inspection scope and an inspection profile.

# Eclipse plugin
Use the next [tutorial](http://eclipse-cs.sourceforge.net/#!/install) to install the checkstyle plugin.

# Maven plugin
We use [check style plugin](https://maven.apache.org/plugins/maven-checkstyle-plugin/usage.html) with [configuration for the Google coding conventions](https://github.com/checkstyle/checkstyle/blob/master/src/main/resources/google_checks.xml).

# Automatic code formatter
We use IntelliJ IDEA to reformat Java source code to comply with Google Java Style.   
Installing the coding style settings:
1. Download [intellij java google style](https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml).
2. Go to *File>Settings>Editor>Code Style*.
3. Press *Manage* button and import `intellij-java-google-style.xml`.