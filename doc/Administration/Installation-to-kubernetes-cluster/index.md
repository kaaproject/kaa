---
layout: page
title: Installation to Kubenetes cluster
permalink: /:path/
sort_idx: 1
---

{% include variables.md %}
{% include_relative links.md %}

* TOC
{:toc}


This page provides instructions on installing the Kaa platform to an existing Kubernetes cluster.


## Docker

[Install Docker](https://docs.docker.com/v17.09/engine/installation/) to your local machine.
This is required to be able to run the Kaa installer locally.


## Kaa installation profile

Run `kaa-installer` docker image:

```sh
docker run --rm -it --entrypoint bash \
  --name kaa-installer  \
  -v ${HOME}/.kube:/home/app/.kube \
  -v ${HOME}/.minikube:${HOME}/.minikube \
  -v ${PWD}/kaa_installer/output:/usr/src/kaa/installer/output \
  -v ${PWD}/kaa_installer/profile_overrides:/usr/src/kaa/installer/profile_overrides \
  hub.kaaiot.net/devops/kaa-installer:rel_{{version}}
```

Output example:
```
(venv) [OS:none][AWS:default]:/usr/src/kaa/installer
```

Mounted volumes description:
- `${PWD}/kaa_installer/profile_overrides:/usr/src/kaa/installer/profile_overrides` is used for saving profile overrides in the local filesystem.
- `${PWD}/kaa_installer/output:/usr/src/kaa/installer/output` is used for saving terraform state in the local filesystem (installation state, terraform state, terraform vars).
- `${HOME}/.kube:/home/app/.kube` is used for getting the kubeconfig file.

Following steps will be done inside the docker container console.

Verify that the installer container has access to kubernetes cluster:
```sh
kubectl cluster-info
```

Output example:
```
Kubernetes master is running at https://example.kaaiot.net:6443
CoreDNS is running at https://example.kaaiot.net:6443/api/v1/namespaces/kube-system/services/coredns:dns/proxy
kubernetes-dashboard is running at https://example.kaaiot.net:6443/api/v1/namespaces/kube-system/services/https:kubernetes-dashboard:/proxy
```

Fill in the below JSON template and create a profile override file in `/usr/src/kaa/installer/profile_overrides` for the Kaa installer (you can use any file name):
```sh
cat <<EOF > my_profile.json
{
  "use_kubeconfig": "true",
  "kaa": {
    "release_set": {
      "global.license.createSecret.fileBase64": "",
      "global.license.createSecret.password": "",
      "global.image.pullSecretsCreate.registryUsername": "",
      "global.image.pullSecretsCreate.registryPassword": ""
    }
  },
  "kube_info": {
      "kube_ingress_domain": ""
  }
}
EOF
```

Description of the template values:
- `global.license.createSecret.fileBase64` - your Kaa license file content, base64 encoded
- `global.license.createSecret.password` - your Kaa license file password
- `global.image.pullSecretsCreate.registryUsername` - your KaaID login
- `global.image.pullSecretsCreate.registryPassword` - your KaaID password
- `kube_ingress_domain` - external domain for your Kaa installation

For example:

```sh
cat <<EOF > my_profile.json
{
  "use_kubeconfig": true,
  "kaa": {
    "release_set": {
        "global.license.createSecret.fileBase64": "<your-licence-file-content-base64-encoded>",
        "global.license.createSecret.password": "<license-file-password>",
        "global.image.pullSecretsCreate.registryUsername": "john@example.com",
        "global.image.pullSecretsCreate.registryPassword": "SeCuReP@ssw0rd"
    }
  },
  "kube_info": {
      "kube_ingress_domain": "example.com"
  }
}
EOF
```


## Kaa thirdparties

Install kaa thirdparties (this step not required if you install minikube ingress addon).
```sh
envmanager manager apply --env <environment-name> --profile <profile> --script kaa-thirdparty --state local --cloud kubernetes --profile-override /usr/src/kaa/installer/profile_overrides/my_profile.json
```

Description of the template values:
- `environment-name` - name of your installation
- `profile` - your cloud provider name (`azure`, `aws`, `openstack`), for bare-metal installation use `non-cloud`

State file of the terraform installation will be saved to `output/<environment-name>/kaa-thirdparty`

Output example:

```
Apply complete! Resources: 8 added, 0 changed, 0 destroyed.

The state of your infrastructure has been saved to the path
below. This state is required to modify and destroy your
infrastructure, so keep it safe. To inspect the complete state
use the `terraform show` command.

State path: /usr/src/kaa/installer/output/example-env/kaa-thirdparty/terraform.tfstate

Outputs:

certmanager_issuer_repository = [
  "https://kubernetes-charts.storage.googleapis.com",
  "stable",
]
certmanager_repository = [
  "https://kubernetes-charts.storage.googleapis.com",
  "stable",
]
dns_note = Don't forget setup dns records for created nginx ingress service
required 2 records (kaa domain, keycloak, domain)

ingress_name = ingress
ingress_namespace = ingress
ingress_repository = [
  "https://kubernetes-charts.storage.googleapis.com",
  "stable",
]
ingress_revision = 1
kaaid_roles_repository = [
  "https://kubernetes-charts.storage.googleapis.com",
  "stable",
]
kube_info = {
  "kube_api_ca" = ""
  "kube_api_token" = ""
  "kube_api_url" = "https://example.kaaiot.net:6443"
  "kube_ingress_domain" = "local.kaatech.com"
  "kube_lb_supported" = "false"
  "kube_persistence_supported" = "false"
  "kube_version" = ""
  "kube_vm_ips" = [""]
  "master_vm_ips" = [""]
  "worker_vm_ips" = [""]
}
openebs_repository = [
  "https://kubernetes-charts.storage.googleapis.com",
  "stable",
]
```


## DNS records

By default the Kaa installation requires 2 DNS A or CNAME records (based on your cloud) for which ingress objects are created:
 - `env.<kube_ingress_domain>` - used for Kaa UI and REST API
 - `auth.<kube_ingress_domain>` - used for KeyCloak auth server

For example, for **bare metal installation**:
```sh
kubectl get svc -n ingress ingress-nginx-ingress-controller
NAME                               TYPE        CLUSTER-IP      EXTERNAL-IP    PORT(S)          AGE
ingress-nginx-ingress-controller   ClusterIP   10.233.33.122   54.37.76.254   80/TCP,443/TCP   58d
```

add records:
  - `env.<kube_ingress_domain> A EXTERNAL-IP`
  - `auth.<kube_ingress_domain> A EXTERNAL-IP`

For **cloud installation**:
```sh
kubectl get svc -n ingress ingress-nginx-ingress-controller
NAME                               TYPE           CLUSTER-IP      EXTERNAL-IP                                                               PORT(S)                      AGE
ingress-nginx-ingress-controller   LoadBalancer   10.0.0.122      se720v6ec018911ea9b7802c02081dbe-xxxxxxx.us-west-2.elb.amazonaws.com      80:32426/TCP,443:30766/TCP   1d

```

add records:
  - `env.<kube_ingress_domain> CNAME EXTERNAL-IP`
  - `auth.<kube_ingress_domain> CNAME EXTERNAL-IP`


## Kaa installation

Now everything is ready to install the Kaa platform.

```sh
envmanager manager apply --env <environment-name> --profile <profile> --script kaa-apps --state local --cloud kubernetes --profile-override /usr/src/kaa/installer/profile_overrides/my_profile.json
```
where:
- `environment-name` - name of your installation
- `profile` - your profile name (`dev`- single-node, non-replicated profile for development purpose, `prod` - three-node, replicated profile for production use),

Terraform installation state will be saved to `output/example-env/kaa-apps/`.

Output example:

```
Apply complete! Resources: 11 added, 0 changed, 0 destroyed.

The state of your infrastructure has been saved to the path
below. This state is required to modify and destroy your
infrastructure, so keep it safe. To inspect the complete state
use the `terraform show` command.

State path: /usr/src/kaa/installer/output/example-env/kaa-apps/terraform.tfstate

Outputs:

kaa_name = kaa
kaa_namespace = kaa
kaa_repository = [
  "https://kubernetes-charts.storage.googleapis.com",
  "stable",
]
kaa_revision = 1
kaa_version = {
  "componets" = {
    "core" = {
      "blueprint" = {
        "building" = "0.0.9"
      }
      "client" = {
        "python-simulator" = "0.1.4"
      }
      "dev-tools" = {
        "keycloak-configurator" = "0.1.16"
      }
      "service" = {
        "cex" = "1.0.13"
        "cm" = "1.0.13"
        "cmx" = "1.0.5"
        "dcx" = "1.0.11"
        "ecr" = "1.0.11"
        "epl" = "1.0.22"
        "epmx" = "1.0.13"
        "epr" = "1.0.15"
        "epts" = "1.0.25"
        "kdca" = "0.0.21"
        "kpc" = "1.0.17"
        "otao" = "1.0.13"
        "rci" = "1.0.7"
        "tekton" = "0.0.37"
        "tsx" = "1.0.4"
        "wd" = "0.0.233"
      }
    }
  }
  "version_repo" = "1.0.390"
}
kube_info = {
  "kube_api_ca" = ""
  "kube_api_token" = ""
  "kube_api_url" = "https://example.kaaiot.net:6443"
  "kube_ingress_domain" = "example.com"
  "kube_lb_supported" = "false"
  "kube_persistence_supported" = "false"
  "kube_version" = ""
  "kube_vm_ips" = [""]
  "master_vm_ips" = [""]
  "worker_vm_ips" = [""]
}
```


## Verification

Open the `https://env.<kube_ingress_domain>` in you browser.
The default user and password are `admin/admin`.

If the web page loads, you have successfuly completed an installation of the Kaa platform on an existing Kubernetes cluster.

The KeyCloak web interface will be available at `https://auth.<kube_ingress_domain>`.
The default user and password are `admin/admin`.

Platform components' REST API will be served under `https://env.<kube_ingress_domain>`.
For example: `https://env.<kube_ingress_domain>/epr/api/v1/endpoints`.


## Next steps

- [Connect a device to your Kaa cluster][how to connect device].
