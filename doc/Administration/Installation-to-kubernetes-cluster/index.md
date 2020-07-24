---
layout: page
title: Installation to Kubernetes cluster
permalink: /:path/
sort_idx: 1
---

{% include variables.md %}
{% include_relative links.md %}

* TOC
{:toc}


This page provides instructions on installing the Kaa platform to an existing Kubernetes cluster.


## Docker

[Install Docker](https://docs.docker.com/install/) to your local machine (please, use 17.09 version).
This is required to be able to run the Kaa installer locally.


## Kaa installation profile

Run `kaa-installer` docker image:

```sh
docker run --rm \
-it \
--entrypoint bash \
-u app \
--name kaa-installer \
-v ${HOME}/.ssh:/home/app/.ssh:ro \
-v ${PWD}:/usr/src/kaa/installer \
-v ${HOME}/.aws:/home/app/.aws:ro \
-v ${HOME}/.kube:/home/app/.kube \
-v ${HOME}/.minikube:${HOME}/.minikube \
-v ${HOME}/.azure:/home/app/.azure \
dev-hub.kaaiot.net/devops/kaa-installer/kaa-installer:latest
```

Output example:
```
(venv) (k8s: kaa)[OS:none][AWS:default]:/usr/src/kaa/installer
```
Mounted volumes description:
- `${PWD}/kaa_installer/output:/usr/src/kaa/installer/output` is used for saving terraform state in the local filesystem (installation state, terraform state, terraform vars).
- `${HOME}/.kube:/home/app/.kube` is used for getting the kubeconfig file.

The following steps will be done inside the docker container console.

Verify that the installer container has access to Kubernetes cluster:
```sh
kubectl cluster-info
```

Output example:
```
Kubernetes master is running at https://example.kaaiot.net:6443
CoreDNS is running at https://example.kaaiot.net:6443/api/v1/namespaces/kube-system/services/coredns:dns/proxy
kubernetes-dashboard is running at https://example.kaaiot.net:6443/api/v1/namespaces/kube-system/services/https:kubernetes-dashboard:/proxy
```

Create profile values YAML file for the Kaa installer with any name and replace values with your ones:

Values:
```
kaa_license: "${KAAIOT_LICENSE}"
kaa_license_password: "${KAAIOT_LICENSE_PASSWORD}"
monitoring_enabled: true
opendistro_enabled: true
logstash_enabled: true
filebeat_enabled: true
```
Where:

- `KAAIOT_LICENSE` - KaaIoT license file content (base64 encoded)
- `KAAIOT_LICENSE_PASSWORD` - KaaIoT license file password
- if you want to install Prometheus and Grafana:
  `monitoring_enabled: true`
- if you want to install ELK stack:
  `opendistro_enabled: true`,
  `logstash_enabled: true`,
  `filebeat_enabled: true`

## Kaa installation

Now everything is ready to install the Kaa platform.

```sh
envmanager manager --env aws --profile aws.yml --vars-file values.yaml apply
```

Terraform installation state will be saved to `output/aws/`.

## Verification

Open the `https://env.<kube_ingress_domain>` in your browser.

The default credentials:
```
tenant_id: 'kaa'  
login: 'admin@example.com'  
password: 'admin'
```

If the web page loads, you have completed the installation of the Kaa platform on an existing Kubernetes cluster.

The KeyCloak web interface will be available at `https://auth.<kube_ingress_domain>`.
The default user and password are `admin/admin`.

Platform components' REST API will be served under `https://env.<kube_ingress_domain>`.
For example: `https://env.<kube_ingress_domain>/epr/api/v1/endpoints`.

Logging and monitoring will be available at `https://kibana.<kube_ingress_domain>` and `https://grafana.<kube_ingress_domain>`.

## Next steps

- [Connect a device to your Kaa cluster][identity management tutorial].
