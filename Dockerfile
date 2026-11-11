FROM amazoncorretto:25-alpine-jdk

ENV SPRING_PROFILES_ACTIVE production

EXPOSE 9002

ADD target/civil-servant-registry-service.jar /data/app.jar

ADD https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.7.9/applicationinsights-agent-3.7.9.jar /opt/appinsights/applicationinsights-agent.jar

CMD java -javaagent:/opt/appinsights/applicationinsights-agent.jar -jar /data/app.jar
