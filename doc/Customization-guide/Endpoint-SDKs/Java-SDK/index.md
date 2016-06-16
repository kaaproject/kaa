---
layout: page
title: Java
permalink: /:path/
sort_idx: 30
---
## Architecture overview 

### Lifecycle of client
Diagram below describe lifecycle of Kaa client &ndash; allowed transitions between states.

![lifecycle](img/lifecycle.png)

### High level overview of Kaa client structure
All calls of Kaa client APIs go through several layers those can be presented by the next scheme:

![layers](img/layers.png)

When we call some method on client for example `start()` Kaa client delegate responsibility for processing call to appropriate _manager_,
in our case &ndash; `BootstrapManager`. The manager calls corresponding method of _BootstrapTransport_ in turn. Next, the transport address to 
`KaaChannelManager` that creates `SyncTask` and put it in queue from which `SyncWorker` will take **task** and call `sync()`
on `KaaDataChannel` (`DefaultBootstrapChannell`). And finally, channel makes request to server.


![sequence](img/sequence.png)

>**NOTE:** `SyncWorker` &ndash; class that extends `Thread` and responsible for serving ongoing tasks from client to channels. 
ChannelManager creates for each channel new instance of this class.