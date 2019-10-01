---
layout: page
title: Custom web dashboard
permalink: /:path/
sort_idx: 3
---

{% include variables.md %}
{% include_relative links.md %}
{% assign code_url = "https://github.com/kaaproject/kaa/tree/master/doc/Tutorials/build-iot-dashboard/attach/code" %}

* TOC
{:toc}


# Overview

In this tutorial, we will learn how to implement simple **web dashboard (interface)** in vanilla JavaScript that integrates with the Kaa IoT platform and provides simple **device management** functionality. 
We will teach you how to:
- **authenticate user with a username/password** in [Keycloak][keycloak] and get his **[access token][access token]** for usage in **communication with the Kaa microservices' REST API**;
- retrieve all [endpoints][endpoint] using [EPR REST API]({{docs_url}}EPR/docs/current/REST-API/#endpoints_get) by user access token;
- display **endpoint metadata**;
- provide a user with the ability to **manage his endpoints** by updating their metadata.

![Architecture overview](attach/img/architecture-overview.svg)


# Prerequisites

1. You have **web server for serving static HTML**. In this tutorial Intellij Idea built-in web server is used. [Nginx][nginx] is another option.
2. Keycloak has a configured client with the ["openid-connect"][openid] in *"Client Protocol"* and ["public"](https://www.keycloak.org/docs/4.8/server_admin/#_access-type) in *"Access Type"* on its administration page (Keycloak administration dashboard -> "Clients" in left sidebar -> "kaa-frontend" or other client -> "Settings" tab). 
In our case we will work with the client that has the `kaa-frontend` client ID.
![Client configuration for the Client Protocol and Access Type](attach/img/client-configuration-for-the-client-protocol-and-access-type.png)
3. Keycloak has the `*` value the in `Valid Redirect URIs` for the development purpose. **Remove `*` in production.**
![Client configuration for the Valid Redirect URIs](attach/img/client-configuration-for-the-valid-redirect-uris.png)
4. Keycloak has the `*` value in the `Web Origins` for the development purpose. **Remove `*` in production.**
![Client configuration for the Web Origins](attach/img/client-configuration-for-the-web-origins.png)
5. There is a registered user in Keycloak whose credentials we will use.


# Playbook

Keycloak comes with a **[client-side JavaScript library](https://www.keycloak.org/docs/4.8/securing_apps/index.html#_javascript_adapter)** that can be used to secure HTML5/JavaScript applications. 
The library can be retrieved directly from the Keycloak server at `https://{keycloak-host}/auth/js/keycloak.js` REST API endpoint.

<br/>

> A best practice is to load the JavaScript adapter programmatically directly from Keycloak Server as it will automatically be updated when you upgrade the server. 
Make sure you upgrade the adapter only after you have upgraded the server.
{:.note}

<br/>

Let's create the project folder and download the library into the file named `keycloak.js`:

```bash
$ mkdir custom-dashboard-tutorial/
$ cd custom-dashboard-tutorial/
$ curl https://{keycloak-host}/auth/js/keycloak.js >> keycloak.js
```

Once the library is downloaded go to Keycloak administration page of the client (*Clients*-> *kaa-frontend* -> *Settings* tab) and click on the `Installation` tab, select `Keycloak OIDC JSON` for `Format Option` then click `Download` button. 
The downloaded `keycloak.json` file must be placed in the root project directory along the `keycloak.js`.
The `keycloak.json` file contains Keycloak auth URL, information about realm, client ID, etc. Check it. 

Your current project directory must look next:

```
custom-dashboard-tutorial/
  |--- keycloak.js
  |--- keycloak.json
```

<br/>

Now let's get our hands dirty in the development :)

<br/>

Create the `index.html` HTML file in the project root and paste the below code in it:

```html
<html lang="en">
  <head>
    <script src="keycloak.js"></script>
  </head>
  
  Hello, user! You are logged-in!
  
  <script>
      var keycloak = Keycloak();
      keycloak
      .init({onLoad: 'login-required'})
      .success(function() {
        // render page visible for a logged in user
        // renderApp(keycloak)
      }).error(function () {
        // log error
        // renderError('Failed to initialize Keycloak client');
      });
  </script>
</html>
```

Here we **initialize the JavaScript adapter**. Pay attention to the passed `{onLoad: 'login-required'}` option to the `init` method.
`login-required` will **automatically authenticate the client** if the user is logged-in to the Keycloak or **display the Keycloak login page** if not.

<br/>

Load to your web server `index.html`, open it in a browser and verify that you were immediately redirected to the Keycloak login page:

![Keycloak login page](attach/img/keycloak-login-page.png)

Enter username/password and verify that you was redirected back to our web dashboard.

![Logged in user view](attach/img/logged-in-user.png)

If the login process **succeeds** the callback passed to the `keycloak`'s `success` method is called. In the callback we will **render application for the logged in user**.
At the same time, if the login process **fails** the callback passed to the `keycloak`'s `error` method is called. In the callback we will **notify a user about the error**.

After successful login `keycloak` variable has authenticated user's access token that can be obtained next: `keycloak.token`. 
Now we can use the access token in subsequent requests to **any Kaa platform microservice REST API** specifying it in the `Authorization` header: `Authorization: bearer eyJhbGciOiJSUzI1NiIsInR5cCIg...`

<br/>

**Check [tips bellow](#tips)** to find out **how to manually obtain and use user access token in requests to the Kaa microservices**.

<br/>

Next, create the `index.js` file in the project root. This file will contain **application rendering logic**.

Firstly, we will implement the `renderApp` method. 
Here we call [Endpoint Register service][EPR] [`GET /endpoints` REST API]({{docs_url}}EPR/docs/current/REST-API/#endpoints_get) with the access token obtained by Keycloak adapter.
This REST API endpoint returns all available endpoints for a requesting user.

```js
const baseKaaPlatformUrl = ''; // Specify the Kaa platform base URL

var renderApp = (keycloak) => {
  if (keycloak.token) {
    const url = `${baseKaaPlatformUrl}/epr/api/v1/endpoints?include=metadata`;
    const req = new XMLHttpRequest();
    req.open('GET', url, true);
    req.setRequestHeader('Accept', 'application/json');
    req.setRequestHeader('Authorization', `bearer ${keycloak.token}`);

    req.onreadystatechange = function () {
      if (req.readyState === 4) {
        if (req.status === 200) {
          renderEndpointTable(req.responseText);
        } else {
          renderError(`Failed to retrieve endpoints' metadata`);
        }
      }
    };

    req.send();
  } else {
    document.getElementById('container').innerHTML = `<button onclick="keycloak.login()">Login</button>`;
  }
};

var renderError = (errorMessage) => {
  document.getElementById('container').innerHTML = `<p style="color: red">${errorMessage}</p>`;
};
```

In the code above if the access token is presented (`keycloak.token` is not `undefined`) it is used to retrieve endpoints, otherwise 'Login' button is rendered.

<br/>

Now, let's implement the `renderEndpointTable` method, that will render retrieved from the previous step endpoints into HTML table.

```js
function renderEndpointTable(responseText) {
  const response = JSON.parse(responseText);
  let endpointRows = ``;
  for (const endpointInfo of response.content) {
    endpointRows = endpointRows + `
              <tr class="endpoint">
                <td class="endpointId">${endpointInfo.endpointId}</td>
                <td>${endpointInfo.appVersion.name}</td>
                <td><input class="location" type="text" value="${endpointInfo.metadata.location
                                                                 || ''}"></td>
              </tr>`
  }
  document.getElementById('table').innerHTML = endpointRows;
  document.getElementById('save-button').innerHTML =
      '<button onclick="saveEndpointLocation(keycloak)">Save location</button>';
}
```

The table displays endpoint ID, its [application version][application] and endpoint `location` metadata field (**you may want to introduce your own metadata field since the Kaa platform is agnostic to the endpoint metadata structure**).

Let's update `index.html` with the HTML of the table and add some [styles]({{code_url}}/style.css) to prettify it.

```html
<html lang="en">
  <head>
    <link rel="stylesheet" type="text/css" href="style.css">
    <script src="keycloak.js"></script>
    <script src="index.js"></script>
  </head>

  <body>
    <div id="container">
      <table>
        <caption id="table-caption">My endpoints</caption>
        <thead>
          <tr>
            <th>Endpoint ID</th>
            <th>Application version</th>
            <th>Location</th>
          </tr>
        </thead>
        <tbody id="table">
        </tbody>
      </table>
    </div>
  </body>

  <script>
    var keycloak = Keycloak();
    keycloak.init({onLoad: 'login-required'}).success(function() {
      renderApp(keycloak);
    }).error(function () {
      renderError('Failed to initialize Keycloak client');
    });
  </script>
</html>
```

<br/>

Revisit the `index.html` in the browser (log in if needed) and verify that endpoint data is successfully retrieved.

![Endpoint table](attach/img/endpoint-table.png)

As you can see there is no data in the 'Location' column since none of the endpoints has the corresponding metadata field (in your case it may be filled if endpoints do have such metadata attribute). 
Jump bellow to change it.

<br/>

**Let's expand our functionality to not only retrieving and displaying endpoint information but also to updating it.**
For that, go to the `index.html` and add the `<p>` tag above the closing `</body>` tag:

```html
  ...
    <p id="save-button">
    </p>
  </body>
  ...
```

Add the `saveEndpointLocation` method to the `index.js`. It sends endpoint location entered by the user to the server using the [EPR REST API]({{docs_url}}EPR/docs/current/REST-API/#endpoints__endpointid__metadata__metadatakey__put).

```js
var saveEndpointLocation = (accessToken) => {
  const endpointsInfo = document.getElementsByClassName('endpoint');
  for (const endpointInfo of endpointsInfo) {
    const endpointId = endpointInfo.getElementsByClassName('endpointId')[0].innerText;
    const updatedLocation = endpointInfo.getElementsByClassName('location')[0].value;
    // request execution
    const url = `${baseKaaPlatformUrl}/epr/api/v1/endpoints/${endpointId}/metadata/location`;
    const req = new XMLHttpRequest();
    req.open('PUT', url, true);
    req.setRequestHeader('Content-Type', 'application/json');
    req.setRequestHeader('Authorization', `bearer ${accessToken}`);
    req.send(JSON.stringify(updatedLocation));
  }
};
```

Pay attention to the `req.setRequestHeader('Authorization', 'bearer ${accessToken}');` line. 
Here we initialize `Authorization` header with the user access token prefixed with the `bearer `.
 
Add rendering of the 'Save changes' button under the `renderEndpointTable` method invocation in the `renderApp` method.

```js
...
if (req.status === 200) {
  renderEndpointTable(req.responseText);
  // 'Save changes' button rendering 
  document.getElementById('save-button').innerHTML =
    '<button onclick="saveEndpointLocation(keycloak.token)">Save changes</button>';
} else {
  renderError(`Failed to retrieve endpoints' metadata`);
}
...
```  

Revisit the `index.html` file in the browser, enter any endpoint location you want and push the 'Save changes' button. It should save entered location on the [EPR][EPR] service.

![Endpoint table with location](attach/img/endpoint-table-with-location.png)

Refresh the page and verify that all changes were saved.

<br/>

Now you can use the same approach with the `Authorization` header to communicate with the [Endpoint Time Series service][EPTS], [Endpoint Configuration Repository service][ECR], [Credential Management service][CM] and other microservices with the REST API to operate with time series, configuration, credentials, etc.


# Resources

All tutorial resources are located on [GitHub]({{code_url}}).


# Tips

You can **obtain user access token manually**. For that execute the next CUrl command:

```bash
$ curl -X POST \
  https://{keycloak-host}/auth/realms/kaa/protocol/openid-connect/token \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password&client_id=kaa-frontend&username={username}&password={password}'
  
{"access_token":"eyJhbGciOiJSUzI1NiIsInR5cCIg..."
```

<br/>

Use the value of the `access_token` field as access token by prefixing it with the `bearer` in requests to the Kaa platform microservices REST API.

[EPR `GET /endpoints`]({{docs_url}}EPR/docs/current/REST-API/#endpoints_get) example:

```bash
$ curl https://{kaa-platform-host}/epr/api/v1/endpoints \
  -H 'Authorization: bearer eyJhbGciOiJSUzI1NiIsInR5cCIg...'
  
{"totalElements":1,"content":[{"endpointId":"0de07db5-5d40-4108-8cf3-1aee344b4aef"...
```

[EPTS `GET /time-series/config`]({{docs_url}}EPTS/docs/current/REST-API/#time_series_config_get) example:

```bash
$ curl https://{kaa-platform-host}/epts/api/v1/time-series/config \
  -H 'Authorization: bearer eyJhbGciOiJSUzI1NiIsInR5cCIg...'
  
{"demo_application":[{"name":"temperature","values":["temp"]}...
```
