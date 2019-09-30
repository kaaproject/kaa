const baseKaaPlatformUrl = '';  // Specify the Kaa platform base URL

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
          document.getElementById('save-button').innerHTML =
              '<button onclick="saveEndpointLocation(keycloak)">Save changes</button>';
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
}

var saveEndpointLocation = (keycloak) => {
  const endpointsInfo = document.getElementsByClassName('endpoint');
  for (const endpointInfo of endpointsInfo) {
    const endpointId = endpointInfo.getElementsByClassName('endpointId')[0].innerText;
    const updatedLocation = endpointInfo.getElementsByClassName('location')[0].value;
    const url = `${baseKaaPlatformUrl}/epr/api/v1/endpoints/${endpointId}/metadata/location`;
    const req = new XMLHttpRequest();
    req.open('PUT', url, true);
    req.setRequestHeader('Content-Type', 'application/json');
    req.setRequestHeader('Authorization', `bearer ${keycloak.token}`);
    req.send(JSON.stringify(updatedLocation));
  }
};

var renderError = (errorMessage) => {
  document.getElementById('container').innerHTML = `<p style="color: red">${errorMessage}</p>`;
};
