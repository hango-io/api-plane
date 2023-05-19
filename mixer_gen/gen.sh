#!/bin/bash

BASE_PATH=$(dirname "$0")
ISTIO=$GOPATH/src/istio.io/istio

rm -rf $ISTIO/mixer/adapter/nsfmeta
cp -r $BASE_PATH/nsfmeta $ISTIO/mixer/adapter

cd $ISTIO

bin/mixer_codegen.sh -t mixer/adapter/nsfmeta/template.proto
bin/mixer_codegen.sh -a mixer/adapter/nsfmeta/config/config.proto -x "-s=false -n nsfapa -t nsfmeta"

cd -

ADAPTER=$ISTIO/mixer/adapter/nsfmeta

cat $ADAPTER/config/nsfapa.yaml $ADAPTER/template.yaml > $BASE_PATH/nsfmeta.yaml

cp -f $ADAPTER/template_handler_service.proto $BASE_PATH/../nsf-api-plane-protocol/src/main/proto/mixer/adapter/nsfmeta/template_handler_service.proto


