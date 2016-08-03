---
layout: page
title: Shell
permalink: /:path/
sort_idx: 50
---

- Prefer plain Bourne shell scripts when possible. Avoid using GNU Bash, zsh or fish since they are possibly will be missing in a particular environment.
- Use [sha-bang](https://en.wikipedia.org/wiki/Shebang_(Unix)) at the beginning of each shell script.

  ```sh
  #!/bin/sh
  ```
- Use [the shellcheck utility](https://www.shellcheck.net) to validate correctness of shell scripts.
- Use `set -e` to stop script execution when any command fails.
- Use new line after conditional and loop statemets for readability purposes.

  ```sh
  # Good
  if [ 1 -le 0 ]; then
    echo YES
  else
    echo NO
  fi

  # Bad
  if [ 1 -le 0 ]; then echo YES; else echo NO; fi
  ```
