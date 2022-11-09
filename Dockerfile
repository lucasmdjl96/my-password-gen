FROM azul/zulu-openjdk:11-latest AS build
COPY . .
RUN ./gradlew buildFatJar --stacktrace

FROM azul/zulu-openjdk-alpine:11-latest
RUN mkdir -p app/keystore
COPY /keystore/localhost.jks /app/keystore/
COPY --from=build /build/libs/my-password-gen-all.jar /app/
WORKDIR /app
ARG SSL_PORT
EXPOSE $SSL_PORT
ENTRYPOINT ["java", "-jar", "my-password-gen-all.jar"]
