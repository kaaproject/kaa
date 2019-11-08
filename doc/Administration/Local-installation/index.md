---
layout: page
title: Local installation
permalink: /:path/
sort_idx: 0
---

* TOC
{:toc}


This page provides instructions on installing a Kaa cluster on your Linux or MacOS local machine using [minikube](https://github.com/kubernetes/minikube).


## Docker and minikube

1. [Install Docker](https://docs.docker.com/v17.09/engine/installation/)
2. [Install minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/)

After the installation you should have:
- minikube
- kubectl
- vm driver (kvm in case of Linux, hyperkit in case of MacOS)


### MacOS dependencies

Install dependencies for minikube:

```sh
printf "\n"|/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
brew cask install minikube
brew install kubernetes-cli
brew cask install docker-edge
```


### Linux dependencies

Install dependencies for minikube and kvm-driver:
```sh
sudo apt-get install qemu-kvm libvirt-clients libvirt-daemon-system
sudo adduser $(whoami) libvirt
sudo adduser $(whoami) kvm
sudo adduser $(whoami) libvirt-qemu
sudo adduser $(whoami) libvirt-dnsmasq
sudo chown $(whoami) /dev/kvm
sudo chmod 777 /dev/kvm
```

Once the `kvm2` is configured, validate that `libvirt` reports no errors:
```sh
virt-host-validate
```

Output example:
```console
QEMU: Checking for hardware virtualization                                 : PASS
QEMU: Checking if device /dev/kvm exists                                   : PASS
QEMU: Checking if device /dev/kvm is accessible                            : PASS
QEMU: Checking if device /dev/vhost-net exists                             : PASS
QEMU: Checking if device /dev/net/tun exists                               : PASS
QEMU: Checking for cgroup 'memory' controller support                      : PASS
QEMU: Checking for cgroup 'memory' controller mount-point                  : PASS
QEMU: Checking for cgroup 'cpu' controller support                         : PASS
QEMU: Checking for cgroup 'cpu' controller mount-point                     : PASS
QEMU: Checking for cgroup 'cpuacct' controller support                     : PASS
QEMU: Checking for cgroup 'cpuacct' controller mount-point                 : PASS
QEMU: Checking for cgroup 'cpuset' controller support                      : PASS
QEMU: Checking for cgroup 'cpuset' controller mount-point                  : PASS
QEMU: Checking for cgroup 'devices' controller support                     : PASS
QEMU: Checking for cgroup 'devices' controller mount-point                 : PASS
QEMU: Checking for cgroup 'blkio' controller support                       : PASS
QEMU: Checking for cgroup 'blkio' controller mount-point                   : PASS
QEMU: Checking for device assignment IOMMU support                         : PASS
QEMU: Checking if IOMMU is enabled by kernel                               : WARN (IOMMU appears to be disabled in kernel. Add intel_iommu=on to kernel cmdline arguments)
LXC: Checking for Linux >= 2.6.26                                         : PASS
LXC: Checking for namespace ipc                                           : PASS
LXC: Checking for namespace mnt                                           : PASS
LXC: Checking for namespace pid                                           : PASS
LXC: Checking for namespace uts                                           : PASS
LXC: Checking for namespace net                                           : PASS
LXC: Checking for namespace user                                          : PASS
LXC: Checking for cgroup 'memory' controller support                      : PASS
LXC: Checking for cgroup 'memory' controller mount-point                  : PASS
LXC: Checking for cgroup 'cpu' controller support                         : PASS
LXC: Checking for cgroup 'cpu' controller mount-point                     : PASS
LXC: Checking for cgroup 'cpuacct' controller support                     : PASS
LXC: Checking for cgroup 'cpuacct' controller mount-point                 : PASS
LXC: Checking for cgroup 'cpuset' controller support                      : PASS
LXC: Checking for cgroup 'cpuset' controller mount-point                  : PASS
LXC: Checking for cgroup 'devices' controller support                     : PASS
LXC: Checking for cgroup 'devices' controller mount-point                 : PASS
LXC: Checking for cgroup 'blkio' controller support                       : PASS
LXC: Checking for cgroup 'blkio' controller mount-point                   : PASS
LXC: Checking if device /sys/fs/fuse/connections exists                   : PASS
```


## Minikube machine

Create a minikube machine using the kaa profile.

For MacOS:
```sh
minikube start --cpus 4 --memory 16384 --disk-size 40G --profile kaa --vm-driver=hyperkit --kubernetes-version='v1.15.0'
```

For Linux:
```sh
minikube start --cpus 4 --memory 16384 --disk-size 40G --profile kaa --vm-driver=kvm2 --kubernetes-version='v1.15.0'
```

To verify, run:

```sh
kubectl get pods --all-namespaces
```

Output example:

```console
NAMESPACE     NAME                               READY   STATUS    RESTARTS   AGE
kube-system   coredns-fb8b8dccf-66gkk            1/1     Running   0          106s
kube-system   coredns-fb8b8dccf-drf66            1/1     Running   0          106s
kube-system   etcd-minikube                      1/1     Running   0          29s
kube-system   kube-addon-manager-minikube        1/1     Running   0          39s
kube-system   kube-apiserver-minikube            1/1     Running   0          52s
kube-system   kube-controller-manager-minikube   1/1     Running   0          46s
kube-system   kube-proxy-88nrx                   1/1     Running   0          105s
kube-system   kube-scheduler-minikube            1/1     Running   0          41s
kube-system   storage-provisioner                1/1     Running   0          104s
```

Restart docker service after the minikube starts (**Linux only**):
```sh
sudo systemctl restart docker
```


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
```console
(venv) [OS:none][AWS:default]:/usr/src/kaa/installer
```

Mounted volumes description:
- `${PWD}/kaa_installer/profile_overrides:/usr/src/kaa/installer/profile_overrides` is used for saving profile overrides in the local filesystem.
- `${PWD}/kaa_installer/output:/usr/src/kaa/installer/output` is used for saving terraform state in the local filesystem (installation state, terraform state, terraform vars).

Following steps will be done inside the docker container console.

Set kube-context to minikube.
```sh
kubectl config use-context kaa
```

Verify that the installer container has access to minikube:
```sh
kubectl cluster-info
```

Output example:
```console
Kubernetes master is running at <https://192.168.64.4:8443>
KubeDNS is running at <https://192.168.64.4:8443/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy>

To further debug and diagnose cluster problems, use 'kubectl cluster-info dump'
```

Note the Kubernetes master URL and use it in place of the `<kubernetes master URL>` in the JSON below.

Fill in the below JSON template and create a profile override file in `/usr/src/kaa/installer/profile_overrides` for the Kaa installer (you can use any file name):
```sh
cat <<EOF > my_profile.json
{
  "kube_info": {
    "kube_api_url": "",
    "master_vm_ips": [],
    "worker_vm_ips": [],
    "kube_vm_ips": []
  },
  "kaa": {
    "release_set": {
      "global.license.createSecret.fileBase64": "",
      "global.license.createSecret.password": "",
      "global.image.pullSecretsCreate.registryUsername": "",
      "global.image.pullSecretsCreate.registryPassword": ""
    }
  }
}
EOF
```

Description of the template values:
- `kube_api_url` - use the Kubernetes master URL from the previous step
- `master_vm_ips` - in case of a single node minikube use the Kubernetes master IP address
- `worker_vm_ips` - same as `master_vm_ips`
- `kube_vm_ips` - same as `master_vm_ips`
- `global.license.createSecret.fileBase64` - your Kaa license file content, base64 encoded
- `global.license.createSecret.password` - your Kaa license file password
- `global.image.pullSecretsCreate.registryUsername` - your KaaID login
- `global.image.pullSecretsCreate.registryPassword` - your KaaID password

For example:

```sh
cat <<EOF > my_profile.json
{
  "kube_info": {
    "kube_api_url": "https://192.168.64.4:8443",
    "master_vm_ips": ["192.168.64.4"],
    "worker_vm_ips": ["192.168.64.4"],
    "kube_vm_ips": ["192.168.64.4"]
  },
  "kaa": {
    "release_set": {
        "global.license.createSecret.fileBase64": "<your-licence-file-content-base64-encoded>",
        "global.license.createSecret.password": "<license-file-password>",
        "global.image.pullSecretsCreate.registryUsername": "john@example.com",
        "global.image.pullSecretsCreate.registryPassword": "SeCuReP@ssw0rd"
    }
  }
}
EOF
```


## Kaa thirdparties

Install kaa thirdparties (this step not required if you install minikube ingress addon).
```sh
envmanager manager apply --env kaa-local --profile minikube --script kaa-thirdparty --state local --cloud kubernetes --profile-override /usr/src/kaa/installer/profile_overrides/my_profile.json
```

State file of the terraform installation will be saved to `output/kaa-local/kaa-thirdparty`

Output example:

```console
Apply complete! Resources: 8 added, 0 changed, 0 destroyed.

The state of your infrastructure has been saved to the path
below. This state is required to modify and destroy your
infrastructure, so keep it safe. To inspect the complete state
use the `terraform show` command.

State path: /usr/src/kaa/installer/output/kaa-local/kaa-thirdparty/terraform.tfstate

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
  "kube_api_url" = "https://192.168.64.4:8443"
  "kube_ingress_domain" = "local.kaatech.com"
  "kube_lb_supported" = "false"
  "kube_persistence_supported" = "false"
  "kube_version" = ""
  "kube_vm_ips" = [
    "192.168.64.4",
  ]
  "master_vm_ips" = [
    "192.168.64.4",
  ]
  "worker_vm_ips" = [
    "192.168.64.4",
  ]
}
openebs_repository = [
  "https://kubernetes-charts.storage.googleapis.com",
  "stable",
]
```


## Kaa installation

Now everything is ready to install the Kaa platform.

```sh
envmanager manager apply --env kaa-local --profile minikube --script kaa-apps --state local --cloud kubernetes --profile-override /usr/src/kaa/installer/profile_overrides/my_profile.json
```

By default, deployment timeout is 1200 seconds, but you can change to an appropriate value according to your hardware settings in the `kaa-installer/profiles/kubernetes/kaa-apps/minikube.json` profile.
For example:  `"release_timeout": "700"`.

Terraform installation state will be saved to `output/kaa-local/kaa-apps/`.

Output example:

```console
Apply complete! Resources: 11 added, 0 changed, 0 destroyed.

The state of your infrastructure has been saved to the path
below. This state is required to modify and destroy your
infrastructure, so keep it safe. To inspect the complete state
use the `terraform show` command.

State path: /usr/src/kaa/installer/output/kaa-local/kaa-apps/terraform.tfstate

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
  "kube_api_url" = "https://192.168.64.4:8443"
  "kube_ingress_domain" = "local.kaatech.com"
  "kube_lb_supported" = "false"
  "kube_persistence_supported" = "false"
  "kube_version" = ""
  "kube_vm_ips" = [
    "192.168.64.4",
  ]
  "master_vm_ips" = [
    "192.168.64.4",
  ]
  "worker_vm_ips" = [
    "192.168.64.4",
  ]
}
```


## Verification

Exit the `kube-installer` docker container and append the lines below to `/etc/hosts` file on you host system:

```console
<kubernetes IP> auth.local.kaatech.com
<kubernetes IP> env.local.kaatech.com
```
where `<kubernetes IP>` is the Kubernetes IP address from your [installation profile](#kaa-installation-profile).

Open the [Kaa Web Dashboard interface](https://env.local.kaatech.com) in you browser.
The default user and password are `admin/admin`.

If the web page loads, you have successfuly completed a local installation of the Kaa platform.

The KeyCloak web interface will be available at [https://auth.local.kaatech.com](https://auth.local.kaatech.com).
The default user and password are `admin/admin`.

Platform components' REST API will be served under https://env.local.kaatech.com.
For example: `https://env.local.kaatech.com/epr/api/v1/endpoints`.


## Next steps

- [Connect a device to your local Kaa cluster][how to connect device].
