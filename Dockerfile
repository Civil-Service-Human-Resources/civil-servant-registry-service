FROM amazoncorretto:8-alpine

ENV SPRING_PROFILES_ACTIVE production

EXPOSE 9002

ADD lib/AI-Agent.xml /opt/appinsights/AI-Agent.xml
ADD https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.0.3/applicationinsights-agent-3.0.3.jar /opt/appinsights/applicationinsights-agent-3.0.3.jar

ADD build/libs/civil-servant-registry-service.jar /data/app.jar

CMD java -javaagent:/opt/appinsights/applicationinsights-agent-3.0.3.jar -jar /data/app.jar
