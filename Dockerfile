FROM eclipse-temurin:24 AS builder
WORKDIR application
COPY pom.xml .
COPY checkstyle.xml .
COPY src ./src
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:24
WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ENTRYPOINT [ "java", "org.springframework.boot.loader.launch.JarLauncher" ]
EXPOSE 8080
EXPOSE 5005
