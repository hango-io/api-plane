FROM hub.c.163.com/qingzhou/arm64-tomcat:7-jdk8-openjdk

ENV TZ=Asia/Shanghai LANG=C.UTF-8 LANGUAGE=C.UTF-8 LC_ALL=C.UTF-8
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
WORKDIR /usr/local/tomcat
COPY setenv.sh $CATALINA_HOME/bin/
RUN rm -rf webapps/*
COPY ./target/*.war webapps/
RUN cd /usr/local/tomcat/webapps && \
    mv nsf*.war ROOT.war && \
    unzip ROOT.war -d ./ROOT && \
    rm -f ROOT.war

ENTRYPOINT ["catalina.sh", "run"]