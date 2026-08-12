FROM amazoncorretto:8-alpine

ENV SPRING_PROFILES_ACTIVE production

EXPOSE 9002


ADD build/libs/civil-servant-registry-service.jar /data/app.jar

ADD lib/AI-Agent.xml /opt/appinsights/AI-Agent.xml
ADD https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.4.4/applicationinsights-agent-3.4.4.jar /opt/appinsights/applicationinsights-agent-3.4.4.jar

CMD java -javaagent:/opt/appinsights/applicationinsights-agent-3.4.4.jar -jar /data/app.jar
