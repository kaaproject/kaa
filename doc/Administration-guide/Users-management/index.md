---
layout: page
title: Users management
permalink: /:path/
sort_idx: 50
---

{% include variables.md %}

**Table of Contents**

- [Managing tenant admins](#managing-tenant-admins)
- [Managing users](#managing-users)

This guide explains how to manage users in Kaa Admin UI.

This guide assumes that Kaa has already been installed and Admin UI is available from the web. If it's not the case, look at the [Installation guide]({{root_url}}Programming-guide/Getting-started/#installation) for more info.

## Managing tenant admins

The **Tenants** window, which is the starting window for a Kaa admin, displays a list of tenant admins. A Kaa admin can edit a tenant admin's name/e-mail and delete tenant admins from the system.

<img src="attach/image2015-3-5%2014-4-25.png" width="600" height="250">

To view a tenant admin's details, select the tenant admin either from the list or from the navigation panel on the left side.

To create a new tenant admin, click **Add tenant** and then fill in all the required fields. Click **Add** to apply the changes.

<img src="attach/image2015-3-5%2014-6-33.png" width="600" height="350">

The newly created tenant admin will receive an e-mail with his or her login to Kaa web UI credentials window.

## Managing users

The tenant admin can add, edit and delete users.

To add a user, do the following:

1. Open to the **Users** window and click **Add user**.

    <img src="attach/image2015-3-4%2016-54-48.png" width="850" height="200">

2. In the **Add user** window, enter the username and email.
3. In the **Account role** field, select _Tenant developer_.

   <img src="attach/image2015-3-4%2016-54-1.png" width="500" height="300">

To edit a user's profile, open the **User details** window by clicking the user's name either on the navigation panel or in the list in the **Users** window.

To delete a user, open the **Users** window and click **Delete** next to the user's name.

<img src="attach/image2015-3-4%2016-54-48.png" width="850" height="200">
