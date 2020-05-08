---
layout: page
title: Local installation
permalink: /:path/
sort_idx: 0
---

{% include variables.md %}
{% include_relative links.md %}

* TOC
{:toc}


This page provides instructions on installing a Kaa cluster on your Linux or macOS local machine using [minikube](https://github.com/kubernetes/minikube).


## Docker and minikube

1. [Install Docker](https://docs.docker.com/install/) (please use version 17.09)
2. [Install minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/)

After the installation you should have:
- minikube
- kubectl
- vm driver (kvm in case of Linux, hyperkit in case of macOS)


### macOS dependencies

#### For Catalina +

Install Docker Desktop for Mac 
https://docs.docker.com/docker-for-mac/install/

Go to `Preference` -> `Kubernetes` and check `Enable Kubernetes` checkbox.
#### For previous macOS versions

Install minikube and hyperkit:

```sh
printf "\n"|/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)" 
brew install hyperkit
brew install minikube 
brew install kubernetes-cli 
brew install docker
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
```
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

For macOS:
```sh
minikube start --cpus=4 --memory=16384 --disk-size=40G --profile=kaa --vm-driver=hyperkit --kubernetes-version='v1.15.0'
```

For Linux:
```sh
minikube start --cpus=4 --memory=16384 --disk-size=40G --profile=kaa --vm-driver=kvm2 --kubernetes-version='v1.15.0'
```
Restart docker service after the minikube started (**Linux only**):
```sh
sudo systemctl restart docker
```

Run command to check that all steps were done correctly:

```sh
kubectl get pods --all-namespaces
```

Output example:

```
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

## Kaa installation profile

Run Kaa installer docker image:
```sh
./dev-cli.sh rebuild
```
Output example:
```
(venv) (k8s: kaa)[OS:none][AWS:default]:/usr/src/kaa/installer
```

Mounted volume description:
- `${PWD}/kaa_installer/output:/usr/src/kaa/installer/output` is used for saving terraform state in the local filesystem (installation state, terraform state, terraform vars).

The following steps will be done inside the docker container console.

Set kube-context for connection to the local Kubernetes cluster. 
```sh
kubectl config use-context kaa
```
or if you use Docker for Mac
```sh
kubectl config use-context docker-desktop
```

Validate that the installer container has access to the local Kubernetes cluster:
```sh
kubectl cluster-info
```

Output example:
```
Kubernetes master is running at https://192.168.39.81:8443
KubeDNS is running at https://192.168.39.81:8443/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy

To further debug and diagnose cluster problems, use 'kubectl cluster-info dump'.
```
Create profile values YAML file for the Kaa installer with any name and replace values with your ones:

Values:
```
local_installation: true
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
envmanager manager --env local --profile kubernetes.yml --vars-file values.yaml apply
```

Terraform installation state will be saved to `output/local/`.

## Verification

Exit the `kaa-installer` docker container and append the lines below to the `/etc/hosts` file on your host system:

```
<kubernetes IP> auth.local.kaatech.com
<kubernetes IP> env.local.kaatech.com
<kubernetes IP> kibana.local.kaatech.com
<kubernetes IP> grafana.local.kaatech.com
```
where `<kubernetes IP>` is the Kubernetes IP address ( from the `kubectl cluster-info`).

Open the [Kaa Web Dashboard interface](https://env.local.kaatech.com) in your browser.

The default credentials:
```
tenant_id: 'kaa'  
login: 'admin@example.com'  
password: 'admin'
```

If the web page loads, you have completed a local installation of the Kaa platform.

The KeyCloak web interface will be available at [https://auth.local.kaatech.com](https://auth.local.kaatech.com).
The default user and password are `admin/admin`.

Platform components' REST API will be served under https://env.local.kaatech.com.
For example: `https://env.local.kaatech.com/epr/api/v1/endpoints`.

Logging and monitoring will be available at [https://kibana.local.kaatech.com](https://kibana.local.kaatech.com) and [https://grafana.local.kaatech.com](https://grafana.local.kaatech.com).
## Next steps

- [Connect a device to your local Kaa cluster][how to connect device].
