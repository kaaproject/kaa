#!/usr/bin/env bash

set -x
source .helpers/.env

HELM_REPOSITORY=$1
HELM_REPOSITORY_USERNAME=$2
HELM_REPOSITORY_PASSWORD=$3
VERSION=$4
APP_VERSION=$5

test -n "$HELM_REPOSITORY" || { echo "HELM_REPOSITORY (first argument) is empty"; exit 1; }
test -n "$HELM_REPOSITORY_USERNAME" || { echo "HELM_REPOSITORY_USERNAME (second argument) is empty"; exit 1; }
test -n "$HELM_REPOSITORY_PASSWORD" || { echo "HELM_REPOSITORY_PASSWORD (third argument) is empty"; exit 1; }
test -n "$VERSION" || { echo "VERSION (fourth argument) is empty"; exit 1; }
test -n "$APP_VERSION" || { echo "APP_VERSION (fifth argument) is empty"; exit 1; }

RANDOM_STR=$(LC_CTYPE=C tr -dc A-Za-z0-9 < /dev/urandom | head -c 10 | xargs)
TEMP_DIR=/tmp/$RANDOM_STR
MOUNT_DIR=/app

CHART_PATH="helm/$CHART_NAME"

mkdir $TEMP_DIR

HELM_CMD="
    helm init --client-only && \
    helm repo add incubator http://storage.googleapis.com/kubernetes-charts-incubator && \
    helm repo update && \
    helm dep update --skip-refresh $CHART_PATH && \
    helm package --save=false $CHART_PATH --version $VERSION --app-version $APP_VERSION
"

docker run --rm \
    -e HELM_HOME=/helm_home/.helm \
    -e HELM_REPOSITORY=$HELM_REPOSITORY \
    -e HELM_REPOSITORY_USERNAME=$HELM_REPOSITORY_USERNAME \
    -e HELM_REPOSITORY_PASSWORD=$HELM_REPOSITORY_PASSWORD \
    -v $TEMP_DIR:/helm_home \
    -v $PWD:$MOUNT_DIR \
    -u $(id -u):$(id -g) \
    -w $MOUNT_DIR \
    --entrypoint /bin/sh \
    alpine/helm:$HELM_VERSION \
    -c "$HELM_CMD"

export HELM_TGZ_ARCHIVE_PATH=$(ls *.tgz)
curl -u $HELM_REPOSITORY_USERNAME:$HELM_REPOSITORY_PASSWORD -T $HELM_TGZ_ARCHIVE_PATH $HELM_REPOSITORY/$HELM_TGZ_ARCHIVE_PATH
curl -u $HELM_REPOSITORY_USERNAME:$HELM_REPOSITORY_PASSWORD $HELM_REPOSITORY/index.yaml -o ./index.yaml
rm -rf HELM_TGZ_ARCHIVE_PATH

docker run --rm \
    -e HELM_HOME=/helm_home/.helm \
    -e HELM_REPOSITORY=$HELM_REPOSITORY \
    -e HELM_REPOSITORY_USERNAME=$HELM_REPOSITORY_USERNAME \
    -e HELM_REPOSITORY_PASSWORD=$HELM_REPOSITORY_PASSWORD \
    -v $TEMP_DIR:/helm_home \
    -v $PWD:$MOUNT_DIR \
    -u $(id -u):$(id -g) \
    -w $MOUNT_DIR \
    --entrypoint /bin/sh \
    alpine/helm:$HELM_VERSION \
    -c "helm repo index . --url $HELM_REPOSITORY --merge ./index.yaml"

curl -u $HELM_REPOSITORY_USERNAME:$HELM_REPOSITORY_PASSWORD -T ./index.yaml $HELM_REPOSITORY/index.yaml