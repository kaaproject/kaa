---
layout: page
title: Nix package manager
permalink: /:path/
sort_idx: 70
---

* TOC
{:toc}

{% include variables.md %}

[Nix](http://nixos.org/nix/) is a powerful Linux package manager.
Kaa team uses it to improve development environment for Kaa [C]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/)/[C++]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C++/) SDKs, and to manage all third-party dependencies including the ones required for Kaa documentation generator. <!--TODO: link to Jekver repository when ready-->

## Set up Nix

To quickly set up Nix:

1. Install Nix.

   ```sh
   curl https://nixos.org/nix/install | sh
   source $HOME/.nix-profile/etc/profile.d/nix.sh
   ```

2. Add `source $HOME/.nix-profile/etc/profile.d/nix.sh` to your `.bashrc`.

   ```sh
   echo 'source $HOME/.nix-profile/etc/profile.d/nix.sh' >> ~/.bashrc
   ```

If you don't trust piping shell scripts from the Internet (and you shouldn't), feel free to examine the script or use some alternative setup.

## Install dependencies, enter shell

The first time you enter the shell environment, Nix will install all the dependencies required for development.

Each time you enter shell you should execute the following command.

```sh
nix-shell
```

Since CC3200 SDK is not freely available, `nix-shell` will stop the first time you enter and ask you to download the SDK file manually and add it to `nix-store`.
Follow the on-screen instructions, then run the above command again.

## Use shell
After you installed the dependencies, you will be switched to the custom bash shell.
You can enter it any time using `nix-shell`.

From there, you can use all your development tools, such as `./build.sh` and `cmake`.
A custom top-level Makefile, provided by default, propagates your commands to all targets.
It also configures all targets appropriately.
To build the Kaa C SDK for any supported platform, run `make`.

If you want to run a single command within a shell, use the `--run` option.

```sh
nix-shell --run make
```

### C SDK options

| Option             | Default value | Description                                       |
|--------------------|---------------|-----------------------------------------------|
| `posixSupport`       | true          | Host build with gcc. Goes to `build-posix`.   |
| `clangSupport`       | true          | Host build with clang. Goes to `build-clang`. |
| `cc3200Support`      | true          | CC3200. Goes to `build-cc3200`.               |
| `esp8266Support`     | true          | ESP8266. Goes to `build-esp8266`.             |
| `raspberrypiSupport` | true          | Raspberry Pi. Goes to `build-rpi`.            |
| `testSupport`        | true          | Add all tools for build verification.         |
| `withTooling`        | true          | Add tools for building docs.                  |
| `withValgrind`       | true          | Add Valgrind memory analyzer.                 |
| `withWerror`         | false         | Enable `-Werror` for all builds.              |

You can override any option with the following command.

```sh
nix-shell --arg optionName value
```

For example:

```sh
nix-shell --arg withWerror true
```

## Further reading
- [Nix pills series](http://lethalman.blogspot.com/2014/07/nix-pill-1-why-you-should-give-it-try.html)
- [Nixpkgs Contributors Guide](https://nixos.org/nixpkgs/manual/)