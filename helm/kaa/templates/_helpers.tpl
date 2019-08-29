{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "kaa.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "kaa.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "kaa.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/* Image pull secrets */}}
{{- define "kaa.imagePullSecrets" -}}
{{- $pullSecrets := .Values.image.pullSecrets | default .Values.global.image.pullSecrets -}}
{{- if ne (len $pullSecrets) 0 -}}
      imagePullSecrets:
{{- toYaml $pullSecrets | nindent 8 -}}
{{- end -}}
{{- end -}}

{{- define "kaa.kaaSecretName" -}}
{{- $name := .Values.kaa.secretName | default .Values.global.kaa.secretName -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.kaaUsername" -}}
{{- $name := .Values.kaa.username | default .Values.global.kaa.username -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.kaaPassword" -}}
{{- $name := .Values.kaa.password | default .Values.global.kaa.password -}}
{{- tpl $name . -}}
{{- end -}}

{{/* Env values */}}
{{- define "kaa.envVariables" -}}
{{- with .Values.env -}}
{{- range $key, $val := . }}
- name: {{ $key | upper }}
  value: {{ $val | quote }}
{{- end -}}
{{- end -}}
{{- end -}}

{{- define "kaa.kafkaHost" -}}
{{- $name := .Values.kaa.kafkaHost | default .Values.global.kafka.host -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.kafkaPort" -}}
{{- $name := .Values.kaa.kafkaPort | default .Values.global.kafka.port -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.postgresqlUrl" -}}
{{- $name := .Values.postgresqllocal.urlOverride | default .Values.global.postgresql.url | default .Values.postgresqllocal.url -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.postgresqlUrlDatasource" -}}
{{- $name := .Values.postgresqllocal.urlDatasource | default .Values.global.postgresql.kaa.urlDatasource | default .Values.kaa.config.adminDao.jdbcUrl -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.postgresqlSecretName" -}}
{{- $name := .Values.global.postgresql.secretName | default .Values.postgresqllocal.secretName -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.postgresqlUsername" -}}
{{- $name := .Values.global.postgresql.postgresqlUsername | default .Values.postgresqllocal.postgresqlUsername | default .Values.kaa.config.adminDao.jdbcUsername -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.postgresqlPassword" -}}
{{- $name := .Values.global.postgresql.postgresqlPassword | default .Values.postgresqllocal.postgresqlPassword | default .Values.kaa.config.adminDao.jdbcPassword -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.cassandraUrl" -}}
{{- $name := .Values.cassandralocal.urlOverride | default .Values.global.cassandra.url | default .Values.cassandralocal.url -}}
{{- tpl $name . | quote -}}
{{- end -}}

{{/*
Create a default fully qualified app name for the postgres requirement.
*/}}
{{- define "kaa.zookeeper.fullname" -}}
{{- $zookeeperContext := dict "Values" .Values.zookeeper "Release" .Release "Chart" (dict "Name" "zookeeper") -}}
{{ template "zookeeper.fullname" $zookeeperContext }}
{{- end -}}