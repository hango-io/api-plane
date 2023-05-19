#!/bin/bash

if [ "$NCE_PORT" ]; then
sed -i 's/\(<Connector port="\)[0-9]\{1,5\}\(" protocol="HTTP\/1.1"\)/\1'"$NCE_PORT"'\2/g' $CATALINA_HOME/conf/server.xml
fi

if [ -z "$NCE_XMS" ]; then
NCE_XMS=1024m
fi

if [ -z "$NCE_XMX" ]; then
NCE_XMX=2048m
fi

if [ -z "$NCE_PERM" ]; then
NCE_PERM=256m
fi

NCE_APPNAME=
export NCE_JAVA_OPTS="$NCE_JAVA_OPTS -Xms${NCE_XMS} -Xmx${NCE_XMX} -XX:MaxPermSize=${NCE_PERM} -Dlog.dir=${CATALINA_HOME}/logs"

export JAVA_OPTS="-Djava.awt.headless=true -server -verbose:gc -Djava.security.egd=file:/dev/./urandom -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDetails -Xloggc:$CATALINA_HOME/logs/gc.log -XX:+PrintGCTimeStamps -Dforeign.domain=true -Dsun.rmi.transport.tcp.responseTimeout=20000 -Dsun.rmi.dgc.client.gcInterval=7200000 -Dsun.rmi.dgc.server.gcInterval=7200000 -Duser.timezone=Asia/Shanghai -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 $JAVA_OPTS $NCE_JAVA_OPTS"