---
layout: page
title: Intel Edison
permalink: /:path/
sort_idx: 60
---

{% include variables.md %}

* TOC
{:toc}

The guide provides information on how to **cross-compile** C SDK for Intel Edison.
Alternatively, you can build the Kaa C endpoint SDK right on the Edison board.
Refer to [the Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux/) for further details.

# Install dependencies

**The further instructions must be executed on the host machine.**

**Verified against:**

 - **Host OS:** **Ubuntu 16.04 64-bit LTS**
 - **Target OS:** Poky (Yocto Project Reference Distro) 1.7.3.

1. Download the [cross compile tools](https://downloadcenter.intel.com/download/24472/Cross-Compiler-Toolchain-for-Intel-Edison-Maker-Board) for your platform, 32 or 64 bit. Untar the downloded file (don't forget to change the file name to proper one).

        tar -xvf edison-toolchain-20150120-linux64.tar.bz2

1. Install toolchain.

        cd i686
        ./install_script.sh

You may experience `find: invalid mode '+111'` error message while running the installation script. In this case you should replace `+111` entries of `install_script.sh` with `/111`: 

        executable_files=$($SUDO_EXEC find "$native_sysroot" -type f -perm /111 -exec printf "\"%s\" " {} \; )

The cross compilation toolchain is installed to `/opt/poky-edison/1.6.1/` directory by default. On some configurations the script installs the toolchain only to its working directory.

# Create application

Now, dependencies are installed and it is time to create Kaa application.
Since Edison is running Linux, you can refer to [the Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux/#c-sdk-build) for detailed process of application creation.
But remember, you must specify correct compiler when compiling your Kaa application for Intel Edison:

        source /opt/poky-edison/1.6.1/environment-setup-core2-32-poky-linux

This replaces standard `$CC` and some other environment variables used during compilation. 

For more details on how to build, upload and run your application on Edision board, you may refer to official [user guide](https://software.intel.com/en-us/intel-edison-board-user-guide).

