FROM openjdk:16.0.1-slim
ADD target/reactive-r2dbc-0.0.1-SNAPSHOT.jar app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=staging","-jar","/app.jar"]