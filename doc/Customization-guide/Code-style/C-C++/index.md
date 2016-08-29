---
layout: page
title: C/C++
permalink: /:path/
sort_idx: 30
---

* TOC
{:toc}

The guide describes code formatting and coding rules that must be followed in Kaa C and C++ SDKs.

There are few common and most important guidelines for both C and C++ SDK.

- Use C99 for C code and C++11 for C++ code
- Anything that causes the undefined behavior (according to C/C++ standarts) either in runtime or in compile-time is prohibited, even if at this point of time it is predictable.
E.g. signed integer overflow.
- Anything that relies on assumptions about unspecified behavior (according to C/C++ standarts) either in runtime or in compile-time is prohibited as well.
E.g. assuming that function arguments evaluate in particular order.
- Anything that relies on particular implementation-defined values, or semantically behaves differentely in different implementations should be marked and properly documented and allowed only in platform-dependent code of the Kaa SDK.
E.g. assuming that `int` will occupy 4 bytes on every platform.

  ```c
  int *x = malloc(4); // Wrong! Size of integer is implementation-defined.
  ```

# Code appearance in C

- It is recommended to use a source formatting script against modified sources.
The script is placed in `client/client-multi/client-c/scripts/srcformat.sh`.
- Use 4 spaces for indentation. Don't use tabs.
- Use 100-character column width.
- Use `snake_case` for the names (not `camelCase`) in C code.
- All macro names must be in UPPERCASE.
- Use [1TBS](https://en.wikipedia.org/wiki/Indent_style#Variant:_1TBS) (the one true brace style).

  ```cpp
  int main(int argc, char *argv[])
  {
     if (argc > 2) {
         printf("too many args\n");
     } else {
         printf("too little args\n");
     }

     return 0;
  }
  ```

  Note brace position.

- The braces in control structures are required even for a single statement.
- The pointer star must be aligned to the variable name.

  ```cpp
  int *x;
  ```

- Don't add space after a cast. (Because cast binds tighter than binary operators, it's less ambiguous.)

  ```cpp
  // Good
  int x = (int)y + z;

  // Bad
  int x = (int) y + z;
  ```

- Use void cast to indicate variable is not used.

  ```cpp
  // Good
  void f(void *x)
  {
      (void)x;
  }

  // Bad: __attribute__ is not standard.
  void f(void *x __attribute__((unused)))
  {
  }
  ```

- Labels must be indented one level less than the normal indentation (except for case labels).

  ```cpp
  int main(int argc, char *argv[])
  {
      switch (argc) {
          case 3:
              printf("hi\n");
              break;

          default:
              goto fail;
      }

      return 0;

  fail:
      return 1;
  }
  ```

- Prefer double-indent instead of alignment in conditions and argument lists.

  ```cpp
  int my_function(int param1, int param2,
         int param3, int param4)
  {
      if (param1 > 3 && param2 > 3 &&
              param3 > 3 && param4 > 3) {
          return param1 + param2 + param3 +
                  param4;
      } else {
          return 0;
      }
  }
  ```

- Don't allow trailing spaces or lines.
- Wrap complex macro in a `do { } while (0)` statement.

  ```c
  #define COMPLEX_MACRO(a, b, c) \
      do { foo(a); bar(b); baz(c); } while (0)
  ```

- Prefer trailing commas over leading, when breaking multi-line function declaration.

  ```c
  // Good
  void function_with_many_params_and_long_name(long_name_param a,
          long_name_param b, long_name_param c, long_name_param d,
          long_name_param e, long_name_param f);


  // Bad
  void function_with_many_params_and_long_name(param a
                                             , param b
                                             , param c);
  ```

- Commented out code is not allowed without great reason and notes.

- Do not use `_t` suffix for type names. It is reserved by a compiler.


- Use TODO wisely. Valid cases include:

  - You don't know how to solve the issue yet.
  - Support for a feature is needed.
  - The changes can't be done now.

  Place TODO comment in the code along with issue number and description as shown below.

  ```c
  // Good:
  // TODO(KAA-xxx): short description

  // Bad:
  // TODO: short description

  // Will be banned during code review:
  // TODO: add error handling
  ```

- Documentation should have non-zero entropy. (i.e., it should carry at least some additional information.)

  ```c
  // Bad (all information can be easily deduced from the function
  // signature, 0 bytes of usefulness):
  /**
   * Set connection listener.
   *
   * @param[in] listener the listener
   */
  void set_connection_listener(const connection_listener listener);

  // Better (no documentation):
  void set_connection_listener(const connection_listener listener);

  // Good:
  /**
   * Sets a connection listener that will be called in case connection
   * status changes.
   *
   * This includes next events:
   * - client connects to the server
   * - connection is broken
   */
  void set_connection_listener(const connection_listener listener);
  ```

# Semantics

- Initialize all variables.
- Prefer declaration with initialization.

  ```c
  int a = 10; // Good
  int b; // Bad
  ```

- Initialize structures with designated initializers.

  ```c
  struct st {
      int a;
      int b;
      int c;
  };

  void foo(void)
  {
      struct st obj = {
          .a = 1,
          .b = 2,
          .c = 3,
      };
  }

  ```

- Typedefs that indicate size and signedness should be used in place of the basic numerical types.
E.g. `uint32_t` instead of `unsigned int` and so on.
See Dir 4.6 from MISRA C guidelines for details.

- `#pragma` is forbidden.

- Size of the variable-length array (those that allocated on stack with size known at runtime) must be explicitely checked within the same frame to prevent stack overflow.

  ```c
  void foo(size_t size)
  {
      assert(size < SOME_PREDEFINED_SIZE); // Prevent stack from overflowing
      assert(size); // Zero-length arrays are explicitly prohibited by C standart

      int arr[size];

      // Do something with arr ...
  }
  ```

- Functions that takes no parameters must have `void` in the place of parameter lists.

  ```c
  void foo(void); // Good
  void bar(); // Bad, according to the C standart this declares function that takes variable argument list
  ```

# Style and semantics in C++

C++ SDK conforms [the Google C++ Style Guide](https://google.github.io/styleguide/cppguide.html).
