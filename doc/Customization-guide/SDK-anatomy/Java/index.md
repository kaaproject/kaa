---
layout: page
title: Java
permalink: /:path/
sort_idx: 30
---
## Architecture overview 

### Lifecycle of client
Diagram below describe lifecycle of Kaa client &ndash; allowed transitions between states.

<center>
<img src="img/lifecycle.png"/>
</center>

### High level overview of Kaa client structure
All calls of Kaa client APIs go through several layers those can be presented by the next scheme:

<br>
 <center>
 <img src="img/layers.png"/>
 </center>
<br>

When we call some method on client for example <code>start()</code> Kaa client delegate responsibility for processing call to appropriate _manager_,
in our case &ndash; <code>BootstrapManager</code>. The manager calls corresponding method of _BootstrapTransport_ in turn. Next, the transport address to 
<code>KaaChannelManager</code> that creates <code>SyncTask</code> and put it in queue from which <code>SyncWorker</code> will take **task** and call <code>sync()</code>
on <code>KaaDataChannel</code> (<code>DefaultBootstrapChannell</code>). And finally, channel makes request to server.

<br>
 <center>
 <img src="img/sequence.png"/>
 </center>
<br>

>**NOTE:** <code>SyncWorker</code> &ndash; class that extends <code>Thread</code> and responsible for serving ongoing tasks from client to channels. 
ChannelManager creates for each channel new instance of this class.