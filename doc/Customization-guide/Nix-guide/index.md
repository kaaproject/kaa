---
layout: page
title: Nix guide
permalink: /:path/
sort_idx: 70
---

* TOC
{:toc}

{% include variables.md %}

[Nix](http://nixos.org/nix/) is a powerful Linux package manager.
We use it to create a better development environment for the Kaa C SDK and manage all third-party dependencies.

# Setting up Nix

To quickly set up Nix, proceed as follows:

1. Install Nix.

   ```sh
   curl https://nixos.org/nix/install | sh
   source $HOME/.nix-profile/etc/profile.d/nix.sh
   ```
2. Then, add `source $HOME/.nix-profile/etc/profile.d/nix.sh` to your `.bashrc`.

   ```sh
   echo 'source $HOME/.nix-profile/etc/profile.d/nix.sh' >> ~/.bashrc
   ```

If you don't trust piping shell scripts from the Internet (and you shouldn't), feel free to examine the script or use some alternative setup.

# Installing dependencies and entering the shell
The first time you enter the shell environment, Nix installs all dependencies needed for development.

Note that each time you enter shell you should execute the following command.

```sh
nix-shell
```

Also, as CC3200 SDK is not freely available, nix-shell will stop the first time you enter and ask you to download the SDK file manually and add it to nix-store.
Follow the instructions provided on the screen.
Then re-run the command above.

# Using shell
After dependencies are installed, you'll find yourself in a custom bash shell (you can enter it with `nix-shell` whenever you want).

You can use all your development tools, such as `./build.sh` and `cmake`, from there.
Furthermore, there is a custom top-level Makefile, provided by default, that propagates your commands to all targets (it also configures all targets appropriately).
So just run `make` to build the Kaa C SDK for all platforms.

If you want to run a single command within a shell, use `--run` option. For example:

```sh
nix-shell --run make
```

## C SDK options

| Option             | Default value | Meaning                                       |
|--------------------|---------------|-----------------------------------------------|
| posixSupport       | true          | Host build with gcc. Goes to `build-posix`.   |
| clangSupport       | true          | Host build with clang. Goes to `build-clang`. |
| cc3200Support      | true          | CC3200. Goes to `build-cc3200`.               |
| esp8266Support     | true          | ESP8266. Goes to `build-esp8266`.             |
| raspberrypiSupport | true          | Raspberry Pi. Goes to `build-rpi`.            |
| testSupport        | true          | Add all tools for build verification.         |
| withTooling        | true          | Add tools for building docs.                  |
| withValgrind       | true          | Add Valgrind memory analyzer.                 |
| withWerror         | false         | Enable `-Werror` for all builds.              |

You can override any option with the following command.

```sh
nix-shell --arg optionName value
```

For example:

```sh
nix-shell --arg withWerror true
```

# Further reading
- [Nix pills series](http://lethalman.blogspot.com/2014/07/nix-pill-1-why-you-should-give-it-try.html)
- [Nixpkgs Contributors Guide](https://nixos.org/nixpkgs/manual/)
