FROM gradle:jdk19-jammy AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN ./gradlew build --no-daemon

FROM eclipse-temurin:19

EXPOSE 9000

RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/app.jar

ENTRYPOINT ["java", "-XX:+CrashOnOutOfMemoryError", "-Xms256m", "-Xmx3072m", "-jar", "/app/app.jar"]
