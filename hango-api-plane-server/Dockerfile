FROM hangoio/tomcat-8.5:v1.1

ENV TZ=Asia/Shanghai LANG=C.UTF-8 LANGUAGE=C.UTF-8 LC_ALL=C.UTF-8
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
WORKDIR /usr/local/tomcat
COPY ./*-api-plane-server/setenv.sh $CATALINA_HOME/bin/
RUN rm -rf webapps/*
COPY ./*-api-plane-server/target/*.war webapps/
RUN cd /usr/local/tomcat/webapps && \
    mv *.war ROOT.war && \
     unzip ROOT.war -d ./ROOT && \
     rm -f ROOT.war
ENTRYPOINT ["catalina.sh", "run"]