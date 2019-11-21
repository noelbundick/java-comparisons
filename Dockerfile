ARG BASE_TAG=11u5-zulu-alpine

# There's not a Java 11 on https://hub.docker.com/_/microsoft-java-maven, so we'll make our own stripped-down version
FROM mcr.microsoft.com/java/jdk:${BASE_TAG} AS maven

RUN apk add --no-cache curl tar bash procps

ARG MAVEN_VERSION=3.6.2
ARG SHA=d941423d115cd021514bfd06c453658b1b3e39e6240969caf4315ab7119a77299713f14b620fb2571a264f8dff2473d8af3cb47b05acf0036fc2553199a5c1ee
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
  && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
  && echo "${SHA}  /tmp/apache-maven.tar.gz" | sha512sum -c - \
  && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
  && rm -f /tmp/apache-maven.tar.gz \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "/root/.m2"

# Build the application
FROM maven AS build
WORKDIR /build

# Only pull dependencies when pom.xml has changed
COPY ./pom.xml ./pom.xml
RUN mvn dependency:go-offline -B

# Provide some bogus environment variables - required for application startup
ENV AZURE_STORAGE_CONN_STRING="DefaultEndpointsProtocol=https;AccountName=azurestorage;AccountKey=invalid_key;EndpointSuffix=core.windows.net"

# Build the jar
COPY ./src ./src
RUN mvn package

# Run the app in a JRE alpine image
FROM mcr.microsoft.com/java/jre:${BASE_TAG} AS app
WORKDIR /app

EXPOSE 8080

# Run app.jar - defined in pom.xml via <finalName>
CMD ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]
COPY --from=build /build/target/app.jar /app
