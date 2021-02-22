---
layout: page
title: Platform backup
permalink: /:path/
sort_idx: 3
---

{% include variables.md %}
{% include_relative links.md %}

* TOC
{:toc}


This page provides instructions on how to back up and restore a deployed Kaa instance.

By default, the Kaa platform is deployed with the enabled self-backup feature.
It automatically backs up every storage on cron and uploads snapshots to `storage-backup--{K8s-namespace}` AWS S3 bucket, which is created automatically in case it doesn't exist.
Here `{K8s-namespace}` is a Kubernetes namespace where Kaa is deployed.

Now let's check out how to back up and restore the platform using the CLI.


## Prerequisites

1. You have an up-and-running Kubernetes cluster with the deployed Kaa instance.


## Backup

Let's consider a general and an Open Distro backup separately due to some specifics of the Open Distro backup.


### General backup

The general backup type includes backups of the following storage types:

* InfluxDB - used by [Endpoint Time Series service][EPTS]
* MariaDB - used by [Credentials Management service][CM], [Over-tha-air Orchestrator service][OTAO]
* MongoDB - used by [Endpoint Register service][EPR], [Endpoint Configuration Registry service][ECR], [Tekton service][TEKTON]
* PostgreSQL - used by [Binary Data Collection service][BCX], [Client Credentials Management service][CCM], [Command Execution service][CEX], [Tenant Manager service][TM], [Web Dashboard][WD], [Keycloak][keycloak]
* Vault - used by [Client Credentials Management service][CCM]
* Minio - used by [Web Dashboard][WD]

As mentioned above, the backup procedure runs on a periodic basis, and its frequency can be specified in the Kaa Installer as a cron via the `pbm.config.backup.schedule` Helm property.
Its default value is `0 0 * * *` meaning that Kaa starts the backup process every day at 00:00 in UTC. 
Each backup snapshot is archived and compressed into a single file with the `.tar.gz` extension. 
The file is named according to the date and time when the backup started and has the following pattern: `DD-MM-YYYY-hh-mm-ss`, for example, `12-02-2021-15-28-37`.
In that case the backup was made on 12th of February 2021 at 15:28:37 in UTC timezone.
The example of a full backup file name is: `12-02-2021-15-28-37.tar.gz`. 

This file is automatically uploaded to a `storage-backup--{K8s-namespace}` AWS S3 bucket.


### Open Distro backup

An Open Distro backup differs from a general backup to the extent that the former is stored in a separate folder on the S3 bucket named `opendistro`.
This folder is created during the first backup, and all the consequent Open Distro backups are stored there.
Also, Open Distro cannot be backed up locally.


## Restore

To restore the Kaa platform state to a specific point in time, find the relevant snapshot file in the `storage-backup--{K8s-namespace}` AWS S3 bucket with the appropriate date and time in the file name, and apply the following Helm values:

* `pbm.config.restore.backupDate` - the backup date that should be used for the restore procedure, e.g., `12-02-2021-15-28-37`
* `pbm.config.restore.sourceNamespace` - Kubernetes namespace where the snapshot was originally taken.
* `pbm.config.restore.run` - specifies whether the restore procedure must be fired, e.g., `true` or `false`

If all the above properties are set and `pbm.config.restore.run` is `true`, {{service_name}} will launch the restore procedure immediately after applying the changes.
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

Notice that we have mounted the `${PWD}/backup` directory on the local machine.
This folder will be used for storing backup snapshots.

Now that you are inside the Docker container, check whether the Kubernetes cluster where Kaa is deployed is reachable.  

```bash
kubectl get pods -n {k8s-namespace}
```

You should see a list of Kaa pods.

Next, export the below environment variables with relevant values:

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

Now everything is ready to back up and/or restore the Kaa platform.


#### Backup

We assume that you are still in the Docker container with the Kaa backup tool.

Use the below command to back up the platform and store a snapshot to a `storage-backup--{K8s-namespace}` AWS S3 bucket.
Also, it will store a snapshot in the `backup` directory of the folder on your host machine where you run the Docker container.

```bash
backup backup --release-name-prefix {release-name-prefix} --namespace {k8s-namespace} create_s3_all_backups
```

where:

* `{k8s-namespace}` - Kubernetes namespace where Kaa is deployed
* `{release-name-prefix}` - prefix of the Helm release name used to deploy the platform

Use the below commands instead of the `create_s3_all_backups` to back up and upload snapshots to S3 only specific types of storage:

* `create_s3_mongodb_backups` - backs up all MongoDB databases
* `create_s3_mariadb_backups` - backs up all MariaDB databases
* `create_s3_postgresdb_backups` - backs up all PostgreSQL databases
* `create_s3_influxdb_backups` - backs up all InfluxDB databases
* `create_s3_minio_backups` - backs up all Minio storages
* `create_s3_vault_backups` - backs up all Vault storages
* `create_s3_opendistro_backups` - backs up Open Distro's master and data nodes

Use the below command to back up the platform and place a snapshot in the `backup` directory on the file system of your host machine where you run the Docker container.

```bash
backup backup --release-name-prefix {release-name-prefix} --namespace {k8s-namespace} create_local_all_backups
```

> Note that the `create_local_all_backups` command backs up all storages except Open Distro.
{:.note}

Use the below commands instead of `create_local_all_backups` to locally back up only specific types of storage:

* `create_local_mongodb_backups` - backs up all MongoDB databases
* `create_local_mariadb_backups` - backs up all MariaDB databases
* `create_local_postgresdb_backups` - backs up all PostgreSQL databases
* `create_local_influxdb_backups` - backs up all InfluxDB databases
* `create_local_minio_backups` - backs up all Minio storages
* `create_local_vault_backups` - backs up all Vault storages

As you've already noticed, it is not possible to back up Open Distro to the local file system.


#### Restore

We assume that you are still in the backup tool Docker container.

Use the below command to restore the platform from the snapshot in the `storage-backup--{K8s-namespace}` AWS S3 bucket.

```bash
backup backup --release-name-prefix {release-name-prefix} --namespace {k8s-namespace} --backup-date {backup-date} --source-namespace {source-k8s-namespace} restore_s3_all_backups
```

where:

* `{k8s-namespace}` - Kubernetes namespace where Kaa is deployed
* `{release-name-prefix}` - prefix of the Helm release name used to deploy the platform
* `{backup-date}` - backup date that you want to restore. It should match the date and time in the snapshot filename on S3
* `{source-K8s-namespace}` - source Kubernetes namespace from where the backup was made.
It is convenient when the backup and restore are made in different Kubernetes namespaces and/or clusters - e.g., back up a production env in the `production-kaa` namespace and restore it to the `stage-kaa` namespace.

Use the below commands instead of `restore_s3_all_backups` to restore only specific types of storage from AWS S3:

* `restore_s3_mongodb_backups` - restore all MongoDB databases
* `restore_s3_mariadb_backups` - restore all MariaDB databases
* `restore_s3_postgresdb_backups` - restore all PostgreSQL databases
* `restore_s3_influxdb_backups` - restore all InfluxDB databases
* `restore_s3_minio_backups` - restore all Minio storages
* `restore_s3_vault_backups` - restore all Vault storages
* `restore_s3_opendistro_backup` - restore Open Distro's master and data nodes

Use the below command to restore the platform from the snapshot from a `backup` folder on a local machine

```bash
backup backup --release-name-prefix {release-name-prefix} --namespace {k8s-namespace} --backup-date {backup-date} restore_local_all_backups
```

> Note that the `restore_local_all_backups` command restores all storages except Open Distro.
{:.note}

Use the below commands instead of `restore_local_all_backups` to restore only specific types of storage.

* `restore_local_mongodb_backups`
* `restore_local_mariadb_backups`
* `restore_local_postgresdb_backups`
* `restore_local_influxdb_backups`
* `restore_local_minio_backups`
* `restore_local_vault_backups`

As you've already noticed, it is not possible to restore Open Distro from a snapshot on a local file system.
