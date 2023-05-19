#!/bin/bash
mvn clean package -Dmaven.test.skip=true
dates=`date +%s`
echo $dates

MODULE=api-plane
branch=$(git symbolic-ref --short -q HEAD)
commit=$(git rev-parse --short HEAD)
dates=`date +%s`
tag=$(git show-ref --tags| grep $commit | awk -F"[/]" '{print $3}')
if [ -z $tag ]
then
   export TAG=$branch-$commit
else
   export TAG=$tag
fi
if ! git diff-index --quiet HEAD --; then
  TAG=$TAG-$dates
fi

docker login -u staff.qingzhou@service.netease.com -p Qingzhou4321 hub.c.163.com

docker build -f hango-api-plane-server/Dockerfile -t hub.c.163.com/qingzhou/$MODULE:$TAG .

docker  push  hub.c.163.com/qingzhou/$MODULE:$TAG