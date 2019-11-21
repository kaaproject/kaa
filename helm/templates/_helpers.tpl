{{/* --------------- Common templates start --------------- */}}
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

{{/* Env values */}}
{{- define "kaa.envVariables" -}}
{{- with .Values.env -}}
{{- range $key, $val := . }}
- name: {{ $key | upper }}
  value: {{ $val | quote }}
{{- end -}}
{{- end -}}
{{- end -}}
{{/* --------------- Common templates end --------------- */}}

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

{{- define "kaa.kaaPort" -}}
{{- $name := .Values.service.port | default .Values.global.kaa.port -}}
{{- $name -}}
{{- end -}}

{{/* --------------- Dependencies templates start --------------- */}}
{{/* -- cassandra -- */}}
{{- define "kaa.cassandraUrls" -}}
{{- $name := .Values.global.cassandra.urls -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.cassandraHost" -}}
{{- $name := .Values.global.cassandra.host -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.cassandraPort" -}}
{{- $name := .Values.global.cassandra.port -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.cassandraSeedCompleteTable" -}}
{{- $name := .Values.global.cassandra.seedJob.seedCompleteTable -}}
{{- tpl $name . -}}
{{- end -}}

{{/* -- kafka -- */}}
{{- define "kaa.kafkaUrls" -}}
{{- $name := .Values.global.kafka.urls -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.kafkaHost" -}}
{{- $name := .Values.kaa.kafkaHost | default .Values.global.kafka.host -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.kafkaPort" -}}
{{- $name := .Values.kaa.kafkaPort | default .Values.global.kafka.port -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.kafkaZookeeperHost" -}}
{{- $name := .Values.global.zookeeper.host -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.kafkaSeedCompleteTopic" -}}
{{- $name := .Values.global.kafka.topicsJob.seedCompleteTopic -}}
{{- tpl $name . -}}
{{- end -}}

{{/* -- postgresql -- */}}
{{- define "kaa.postgresqlUrls" -}}
{{- $name := .Values.global.postgresql.urls -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.postgresqlHost" -}}
{{- $name := .Values.global.postgresql.host -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.postgresqlDatabase" -}}
{{- $name := .Values.global.postgresql.kaa.postgresqlDatabase -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.postgresqlPort" -}}
{{- $name := .Values.global.postgresql.port -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.postgresqlSecretName" -}}
{{- $name := .Values.global.postgresql.secretName -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.postgresqlUsername" -}}
{{- $name := .Values.global.postgresql.postgresqlUsername -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.postgresqlPassword" -}}
{{- $name := .Values.global.postgresql.postgresqlPassword -}}
{{- tpl $name . -}}
{{- end -}}

{{/* -- flume -- */}}
{{- define "kaa.flumeHost" -}}
{{- $name := .Values.kaa.flumeHost | default .Values.global.flume.host -}}
{{- tpl $name . -}}
{{- end -}}

{{- define "kaa.flumePort" -}}
{{- $name := .Values.kaa.flumePort | default .Values.global.flume.port -}}
{{- tpl $name . -}}
{{- end -}}

{{/* -- zookeeper -- */}}
{{/*
Create a default fully qualified app name for the postgres requirement.
*/}}
{{- define "kaa.zookeeper.fullname" -}}
{{- $zookeeperContext := dict "Values" .Values.zookeeper "Release" .Release "Chart" (dict "Name" "zookeeper") -}}
{{ template "zookeeper.fullname" $zookeeperContext }}
{{- end -}}
{{/* --------------- Dependencies templates end --------------- */}}