---
layout: page
title: Tenants and applications management
permalink: /:path/
sort_idx: 40
---

{% include variables.md %}

* TOC
{:toc}


This guide assumes that Kaa has already been installed and Admin UI is available from the web. If it's not the case, look at the [Installation guide]({{root_url}}Administration-guide/System-installation/) for more info.

# First registration

The first time you log in to Kaa Admin UI, it is required that you register as a [Kaa admin](#kaa-admin) user.

<img src="attach/image2015-5-28%2017-7-0.png" width="400" height="270">

# Kaa user types

The following three user types are available in Kaa :

- Kaa admin
- Tenant admin
- Tenant developer

**NOTE**
In Kaa Sandbox, [default credentials]({{root_url}}Getting-started/#administration-ui) are provided for all three types of Kaa users.

# Kaa admin

The _Kaa admin_ is the highest level administrator of Kaa. He is able to create, edit, and delete tenant admins.
To log into the Kaa UI as a Kaa admin, use the previously created username/password for the Kaa admin.

![](attach/image2014-12-22%2016-22-58.png)

To customize the account, click **Settings => Profile** in the upper right corner of the window and change the first/last name and e-mail to the private ones. Click **Save** to apply the changes.
Note that **Email** is a mandatory field.

<img src="attach/image2014-11-12%2013-3-23.png" width="600" height="400">

To set a private password, click **Settings => Change password** and fill in the fields as required.

![](attach/image2014-11-10%2017-31-31.png)

## Managing Tenants

The Kaa IoT platform supports Multi-tenant architecture. It allows _Kaa admin_ to create separate scope for each instance of Tenant.
Unlike _Kaa admin_, entities _Tenant Admin_ and _Tenant Developer_ can be visible only in the scope of single Tenant.
For more information about Multitenancy, check related description on [wiki]( https://en.wikipedia.org/wiki/Multitenancy ).

# Tenant admin

The _tenant admin_ is a Kaa user who is responsible for managing applications, users and event class families.
To log into the Admin UI as a tenant admin, use the previously generated username/password for the tenant admin.
To customize the account, click **Settings => Profile** and change the first/last name and e-mail to private ones.
To set a private password, click **Settings => Change password** and fill in the fields as required.

## Managing applications

As a tenant admin, you can add and edit applications.

To create a new application, do the following:

1. Open the **Applications** window by clicking the corresponding link on the navigation panel.

    <img src="attach/image2015-3-4%2016-47-22.png" width="400" height="250">

2. Click **Add application** at the top of the window.
3. Enter the title of your application and then click **Add**.

    <img src="attach/image2015-3-4%2016-48-49.png" width="500" height="250">

**NOTE:** If you open the **Application details** window of the newly created application (by clicking this application on either the **Applications** menu on the navigation panel or the **Applications** window), you will notice that the **Application Token** field has been filled in automatically. This is a unique auto-generated application ID.

To edit the application, open the **Application details** window by clicking the application name either on the navigation panel or in the list in the **Applications** window.

# Tenant developer

The tenant developer is a user that creates SDKs based on customer requirements. Tenant developers set the Kaa schemas, group endpoints, and control notification processes.

To log into the Kaa UI as a tenant developer, use the previously generated credentials for the tenant developer.

To customize this account, click **Settings => Profile** and change the first/last name and e-mail to private ones.

To set a private password, click **Settings => Change password**.

**NOTE:** A tenant developer is able to work only with those applications which have been created by his tenant admin. The list of available applications is displayed in the **Applications** window, as well as on the navigation panel under the **Applications** menu.

