---
layout: page
title: Platform backup
permalink: /:path/
sort_idx: 0
---

{% include variables.md %}
{% include_relative links.md %}

* TOC
{:toc}


This page provides instructions on how to backup and restore deployed Kaa instance.

By default, the Kaa platform is deployed with the enabled self-backup feature.
It automatically backs up all storages on cron and uploads snapshots to `storage-backup--{K8s-namespace}` AWS S3 bucket creating it if it doesn't exist.
Here `{K8s-namespace}` is a Kubernetes namespace where Kaa is deployed.

Now let's check how to back up and restore the platform using the CLI.


## Prerequisites

1. You have up-and-running Kubernetes cluster with deployed Kaa instance.


## Backup

Let's separate general and Open Distro backup because of some differences in the Open Distro backup.


### General backup

General backup type includes backup of the next storages:

* InfluxDB - used by [Endpoint Time Series service][EPTS]
* MariaDB - used by [Credentials Management service][CM], [Over-tha-air Orchestrator service][OTAO]
* MongoDB - used by [Endpoint Register service][EPR], [Endpoint Configuration Registry service][ECR], [Tekton service][TEKTON]
* PostgreSQL - used by [Binary Data Collection service][BCX], [Client Credentials Management service][CCM], [Command Execution service][CEX], [Tenant Manager service][TM], [Web Dashboard][WD], [Keycloak][keycloak]
* Vault - used by [Client Credentials Management service][CCM]
* Minio - used by [Web Dashboard][WD]

As it was said, the backup procedure runs on a periodic basis and its frequency can be specified in the Kaa Installer as a cron via the `pbm.config.backup.schedule` Helm property.
Its default value is `0 0 * * *` meaning that Kaa starts the backup process every day at 00:00 in UTC. 
Each backup snapshot is archived and compressed into a single file with the `.tar.gz` extension. 
The file is named after the date and time when the backup run started and has the next pattern: `DD-MM-YYYY-hh-mm-ss`, for example, `12-02-2021-15-28-37`.
In that case the backup was taken on 30th of November 2020 at 15:28:37 in UTC timezone.
The example of a full backup file name is: `12-02-2021-15-28-37.tar.gz`. 

This file is automatically uploaded to a `storage-backup--{K8s-namespace}` AWS S3 bucket.


### Open Distro backup

Open Distro backup differs from the general backup in a way that it is stored in a separate folder on the S3 bucket named `opendistro`.
This folder is created during the first backup and all consequent Open Distro backups are stored there.
Also, Open Distro can't be backed up locally.


## Restore

To restore the Kaa platform state to a specific point in time, find the relative snapshot file in the `storage-backup--{K8s-namespace}` AWS S3 bucket with an appropriate date and time in file name and apply the next Helm values:

* `pbm.config.restore.backupDate` - the backup date that should be used for restore procedure, e.g., `12-02-2021-15-28-37`
* `pbm.config.restore.sourceNamespace` - Kubernetes namespace where the snapshot was originally taken.
* `pbm.config.restore.run` - specifies whether the restore procedure must be fired, e.g., `true` or `false`

If all the above properties are set and `pbm.config.restore.run` is `true` {{service_name}} will immediately launch the restore procedure after applying changes.
The procedure may take a while depending on the snapshot size.


## Local usage

Swap the `{image-tag}` with the appropriate Docker image tag of the backup tool and run it.

```bash
VERSION={image-tag}
docker run --rm \
  -it \
  --entrypoint bash \
  -u app \
  --name pbm \
  -v ${PWD}/backup:/tmp/backup \
  -v ${HOME}/.kube:/home/app/.kube:ro \
  dev-hub.kaaiot.net/devops/pbm/pbm:${VERSION}
```

Notice that we have mounted `${PWD}/backup` directory on the local machine.
This folder will be used for storing backup snapshots.

Now when you are inside the Docker container, check that the Kubernetes cluster where Kaa is deployed is reachable. 

```bash
kubectl get pods -n {k8s-namespace}
```

You should see a list of Kaa pods.

Next, export the bellow environment variables with relevant values:

```bash
export AWS_DEFAULT_REGION=
export AWS_ACCESS_KEY_ID=
export AWS_SECRET_ACCESS_KEY=
export MARIADB_ROOT_PWD=
export POSTGRES_ROOT_PWD=
export OPENDISTRO_USERNAME=
export OPENDISTRO_PASSWORD=
```

AWS environment variables are used for uploading backup snapshots.

Now everything is ready to backup and/or restore the Kaa platform.


#### Backup

We assume that you are still in the Docker container with the Kaa backup tool.

Use the bellow command to backup the platform and store a snapshot to a `storage-backup--{K8s-namespace}` AWS S3 bucket.
Also, it will store a snapshot in the `backup` directory of the folder on your host machine where you run the Docker container.

```bash
backup backup --release-name-prefix {release-name-prefix} --namespace {k8s-namespace} create_s3_all_backups
```

where:

* `{k8s-namespace}` - Kubernetes namespace where Kaa is deployed
* `{release-name-prefix}` - prefix of the Helm release name used to deploy the platform

Use the bellow commands instead of the `create_s3_all_backups` to backup and upload snapshots to S3 only specific type of storage:

* `create_s3_mongodb_backups` - backups all MongoDB databases
* `create_s3_mariadb_backups` - backups all MariaDB databases
* `create_s3_postgresdb_backups` - backups all PostgreSQL databases
* `create_s3_influxdb_backups` - backups all InfluxDB databases
* `create_s3_minio_backups` - backups all Minio storages
* `create_s3_vault_backups` - backups all Vault storages
* `create_s3_opendistro_backups` - backups Open Distro's master and data nodes

Use the bellow command to backup the platform and place a snapshot in the `backup` directory on a filesystem of your host machine where you run the Docker container.

```bash
backup backup --release-name-prefix {release-name-prefix} --namespace {k8s-namespace} create_local_all_backups
```

> Note that the `create_local_all_backups` command backups all storages except Open Distro.
{:.note}

Use the bellow commands instead of `create_local_all_backups` to locally backup only specific type of storage:

* `create_local_mongodb_backups` - backups all MongoDB databases
* `create_local_mariadb_backups` - backups all MariaDB databases
* `create_local_postgresdb_backups` - backups all PostgreSQL databases
* `create_local_influxdb_backups` - backups all InfluxDB databases
* `create_local_minio_backups` - backups all Minio storages
* `create_local_vault_backups` - backups all Vault storages

As you already noticed, it is not possible to backup Open Distro to the local filesystem.


#### Restore

We assume that you are still in the backup tool Docker container.

Use the bellow command to restore the platform from the snapshot in the `storage-backup--{K8s-namespace}` AWS S3 bucket.

```bash
backup backup --release-name-prefix {release-name-prefix} --namespace {k8s-namespace} --backup-date {backup-date} --source-namespace {source-k8s-namespace} restore_s3_all_backups
```

where:

* `{k8s-namespace}` - Kubernetes namespace where Kaa is deployed
* `{release-name-prefix}` - prefix of the Helm release name used to deploy the platform
* `{backup-date}` - backup date that you want to restore. Must match the date and time in the snapshot filename on S3
* `{source-K8s-namespace}` - source Kubernetes namespace from where the backup was taken.
It is useful when backup and restore are taken between different Kubernetes namespaces and/or clusters - e.g., backup production env in the `production-kaa` namespace and restore it to the `stage-kaa` namespace.

Use the bellow commands instead of `restore_s3_all_backups` to restore only specific type of storage from AWS S3:

* `restore_s3_mongodb_backups` - restore all MongoDB databases
* `restore_s3_mariadb_backups` - restore all MariaDB databases
* `restore_s3_postgresdb_backups` - restore all PostgreSQL databases
* `restore_s3_influxdb_backups` - restore all InfluxDB databases
* `restore_s3_minio_backups` - restore all Minio storages
* `restore_s3_vault_backups` - restore all Vault storages
* `restore_s3_opendistro_backup` - restore Open Distro's master and data nodes

Use the bellow command to restore the platform from the snapshot from a `backup` folder on a local machine

```bash
backup backup --release-name-prefix {release-name-prefix} --namespace {k8s-namespace} --backup-date {backup-date} restore_local_all_backups
```

> Note that the `restore_local_all_backups` command restores all storages except Open Distro.
{:.note}

Use the bellow commands instead of `restore_local_all_backups` to restore only specific type of storage.

* `restore_local_mongodb_backups`
* `restore_local_mariadb_backups`
* `restore_local_postgresdb_backups`
* `restore_local_influxdb_backups`
* `restore_local_minio_backups`
* `restore_local_vault_backups`

As you already noticed, it is not possible to restore Open Distro from a snapshot on a local filesystem.

