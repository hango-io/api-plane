FROM tomcat:8.5-jdk8

ENV TZ=Asia/Shanghai LANG=C.UTF-8 LANGUAGE=C.UTF-8 LC_ALL=C.UTF-8
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
WORKDIR /usr/local/tomcat
COPY hango-api-plane-server/setenv.sh $CATALINA_HOME/bin/
RUN rm -rf webapps/*
COPY hango-api-plane-server/target/*.war webapps/
RUN cd /usr/local/tomcat/webapps && \
    mkdir ROOT && \
    mv *.war ROOT/ROOT.war && \
    cd ROOT && \
    jar -xvf ROOT.war && \
    rm -rf *.war

ENTRYPOINT ["catalina.sh", "run"]