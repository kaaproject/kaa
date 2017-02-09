---
layout: page
title: Code style
permalink: /:path/
sort_idx: 40
---

{% include variables.md %}

* TOC
{:toc}

This guide provides some important conventions on writing clear, readable code and API documentation. It is highly recommended that you refer to the guides mentioned at the bottom of the page to learn more about code style conventions used in Kaa.

## General

Never spell "Kaa" as "KAA" (except for [Jira](http://jira.kaaproject.org/browse/KAA/) tickets, e.g.: [KAA-1]).

## Code formatting and naming

- For indentation, use four spaces per indent level for most programming languages, and two spaces for markup languages (HTML, XML, etc.).
- Set maximum line length in your IDE to 160 characters (in Eclipse, go to Java -> Code Style -> Formatter).
- Enable removal of the trailing spaces on file saving in your IDE.
- Always use LOG as a logger name in Java, for example:

```Java
    private static final Logger LOG = LoggerFactory.getLogger(DefaultBootstrapInitializationService.class);
```

- Log exceptions as shown in the following code example (do not use .toString()).

```Java
    LOG.error("Exception caught!", e);
```

- Log parameters using {}, for example:

```Java
    LOG.trace("Bootstrap server register in ZK: thriftHost {}; thriftPort {}; nettyHost {}; nettyPort {}" , thriftHost, thriftPort, nettyHost, nettyPort);
```

## Code commenting

- Place each comment on its own line and before the code that it's describing.
- Add a meaningful description before each class declaration. You may add author(s) full name(s) as in the following example.

```Java
    /**
      * DefaultBootstrapInitializationService Class.
      * Starts and stops all services in the Bootstrap service:
      * 1. CLI Thrift service
      * 2. ZooKeeper service
      * 3. Netty HTTP service
      *
      * @author Andrey Panasenko
      */
```

- Observe standard rules of punctuation and grammar (use articles!).  
- Do not use the first person, that is, _I_ or _We_. For example, do not write “Here we display a list of servers.” Use present tense and omit the subject in comments that describe a code block, and use imperative mood to describe the action within that code block or between code blocks.

```Java
    // Displays a list of servers with their addresses in the console.
    void printServers(List<Server> serverList) {
        // Display the name and address for each server.
        for (Server server : serverList) {
            System.out.printf("Server: %s, %s", server.getName(), server.getAddress());
        }
    }
    // Insert code for get and set accessors.
```

- For more information on writing doc comments for the Javadoc tool, see [http://www.oracle.com/technetwork/articles/java/index-137868.html](http://www.oracle.com/technetwork/articles/java/index-137868.html).

## Log levels

Use the following log levels according to the log purpose.

| Log Level | Description                                                                                                   |
|-----------|---------------------------------------------------------------------------------------------------------------|
| Info      | Use for important or interesting run time events that help understand what the program is doing at the moment |
| Error     | Use for run time errors or unexpected conditions that the program can gracefully recover from                 |
| Fatal     |  Use for severe errors that cause premature program termination                                               |
| Warning   |  Use for unexpected or undesirable run time conditions that are not necessarily affecting the program         |
| Debug     | Use to log detailed information according to the logical work flow of the system                              |
| Trace     | Use to log the most detailed information intended for development and debugging purposes only                 |
