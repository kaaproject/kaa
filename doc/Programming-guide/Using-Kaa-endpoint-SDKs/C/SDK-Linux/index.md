---
layout: page
title: Linux
permalink: /:path/
sort_idx: 10
---

{% include variables.md %}

* TOC
{:toc}

This section describes how to build [Kaa C SDK]({{root_url}}Glossary/#kaa-sdk-type) on a Linux-based machine and install a [Kaa application]({{root_url}}Glossary/#kaa-application).

The application code based on Kaa C SDK is similar for most of the [supported platforms]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/).
However, the build system is not.

This guide is focused on the details of the [CMake](https://cmake.org/)-based build system that is used to tie application code together with the C SDK.

>**NOTE:** This guide is verified against Ubuntu 14.04 LTS Desktop 64-bit.
{:.note}

## Prerequisites

To build Kaa C SDK, make sure to install the following components:

 - CMake.
 - C99 compatible compiler (e.g. GCC).

## Build C SDK

To build Kaa C SDK:

1. [Generate your C SDK]({{root_url}}Programming-guide/Your-first-Kaa-application/#generate-sdk).

2. Unpack the C SDK archive.

   ```bash
   tar -xvf c-sdk-archive-name.tar.gz
   ```
   
3. Create a directory where the SDK will be built.

   ```bash
   mkdir build
   cd build
   ```

4. Configure the build via CMake.

   ```bash
   cmake ..
   ```

5. Perform build.

   ```bash
   make
   ```

## Build Kaa application

After you installed the required dependencies and built the C SDK, you can build and run your Kaa application.
To do this:

1. Create directories for your build structure.
For example, you can create the following directories:

    - `my-kaa-application` -- root directory containing all files;
    - `my-kaa-application/kaa` -- contains the C SDK source files;
    - `my-kaa-application/src` -- contains the application code.

   ```bash
   mkdir my-kaa-application
   mkdir my-kaa-application/kaa
   mkdir my-kaa-application/src
   cd my-kaa-application
   ```

2. Generate C SDK and unpack it to the `my-kaa-application/kaa` directory.

   ```bash
   tar -xvf c-sdk-archive.tar.gz -C my-kaa-application/kaa
   ```

3. In `my-kaa-application/src` directory, create a `kaa-application.c` file that will contain the application code.

   ```bash
   touch src/kaa-application.c
   ```

    Open the `kaa-application.c` file and write the application code in it.
    For the sake of example, a very simple application will be built that displays a string: *Hello, I am a Kaa Application!*.

   ```c
	#include <stdio.h>
	#include <stdlib.h>
	#include <kaa/kaa.h>
	#include <kaa/platform/kaa_client.h>
	#include <kaa/kaa_error.h>


	static void dummy_function(void *context)
	{
		printf("Hello, I am a Kaa Application!\n");
		kaa_client_stop(context);
	}

	int main(void)
	{
		kaa_client_t *kaa_client = NULL;
		kaa_error_t error = kaa_client_create(&kaa_client, NULL);
		if (error) {
			return EXIT_FAILURE;
		}

		error = kaa_client_start(kaa_client, dummy_function, (void *)kaa_client, 0);

		kaa_client_destroy(kaa_client);

		if (error) {
			return EXIT_FAILURE;
		}

		return EXIT_SUCCESS;
	}
   ```

4. Create a `CMakeLists.txt` file in the `my-kaa-application` directory.
It is a top-level CMake file handling the application build.

   ```bash
   touch CMakeLists.txt
   ```

    Add the following code to the `CMakeLists.txt` file.
    The first line sets the minimum CMake version required to build a `kaa-application` project.
    The second line sets project name and language.

   ```bash
   cmake_minimum_required(VERSION 2.8.12)
   project(kaa-application C)
   ```

    Some compilers flags are set, and C SDK project is included as a subproject to the kaa-application project.
    That is a clue.
    The parameter contains a path to the C SDK's `CMakeLists.txt`.

   ```bash
   set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c99 -g -Wall -Wextra")
   add_subdirectory(kaa)
   ```
    Compile the executable file `kaa-app` using `src/kaa-application.c`, and link it with `kaac`.

   ```bash
   add_executable(kaa-app src/kaa-application.c)
   target_link_libraries(kaa-app kaac)
   ```

    See the full `CMakeLists.txt` code below.

   ```bash
   cmake_minimum_required(VERSION 2.8.12)
   project(kaa-application C)
   
   set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c99 -g -Wall -Wextra")
   
   add_subdirectory(kaa)
   
   add_executable(kaa-app src/kaa-application.c)
   target_link_libraries(kaa-app kaac)
   ```

5. Now your directory structure should look like this.

   ```
   - my-kaa-application
     - CMakeLists.txt
     - kaa
       - Unpacked C SDK
     - src
       - kaa-application.c
   ```

6. Finally, you can build the application.
Create a build directory.

   ```bash
   mkdir build
   cd build
   ```

7. Configure the build via CMake and run the `make` command.

   ```bash
   cmake -DKAA_MAX_LOG_LEVEL=3 -DBUILD_TESTING=OFF ..
   make
   ```
    `KAA_MAX_LOG_LEVEL` [parameter]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C) is used here to decrease log level which is set by default to eliminate output pollution.

    `BUILD_TESTING` is **ON** by default.
    Switch it to **OFF** to prevent running tests.

8. Run your application.

   ```bash
   ./kaa-app
   ```

    You should observe the following output.

   ```
   Hello, I am a Kaa Application!
   ```

See full [source code on GitHub]({{github_url}}client/client-multi/client-c/examples/my-kaa-application).

For more information about writing application code, see [Programming guide]({{root_url}}Programming-guide).