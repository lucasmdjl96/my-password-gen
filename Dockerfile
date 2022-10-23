FROM azul/zulu-openjdk:11-latest AS build
COPY . .
RUN ./gradlew buildFatJar --stacktrace -x jvmTest

FROM azul/zulu-openjdk-alpine:11-latest
RUN mkdir -p app/keystore
COPY /keystore/localhost.jks /app/keystore/
COPY --from=build /build/libs/password-manager-all.jar /app/
WORKDIR /app
EXPOSE 8443
ENTRYPOINT ["java", "-jar", "password-manager-all.jar"]
