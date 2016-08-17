---
layout: page
title: Users management
permalink: /:path/
sort_idx: 50
---

{% include variables.md %}

**Table of Contents**

- [Managing tenants](#managing-tenants)
- [Managing tenant admins](#managing-tenant-admins)
- [Managing users](#managing-users)

This guide explains how to manage users in Kaa Admin UI.

This guide assumes that Kaa has already been installed and Admin UI is available from the web. If it's not the case, look at the
[Installation guide]({{root_url}}Programming-guide/Getting-started/#installation) for more info.

## Managing tenants

The **Tenants** window, which is the starting window for a Kaa admin, displays a list of tenants. The Kaa admin can add/edit/delete a tenant and add a tenant admin to it.

<img src="attach/tenants.png" width="600" height="250">

## Managing tenant admins

To view a tenant details, select the tenant either from the list or from the navigation panel on the left side.

<img src="attach/tenant.png" width="600" height="250">


To create a new tenant admin, click **Add user** and then fill in all the required fields. Click **Add** to apply the changes.

<img src="attach/tenant_admin.png" width="600" height="350">

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
