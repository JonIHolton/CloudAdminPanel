# Build stage
FROM maven:3.9.6-amazoncorretto-17-al2023 AS build

WORKDIR /app

# Copy pom.xml first to leverage Docker cache
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code last since it changes more frequently
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM amazoncorretto:17.0.10-al2023-headless

WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Install Ruby, Fluentd, Supervisor, and clean up in a single RUN to reduce layers and image size
RUN yum update -y && \
    yum install -y ruby ruby-devel gcc make python3 python3-pip && \
    gem install fluentd --no-doc && \
    fluentd --setup ./fluentd && \
    pip3 install supervisor && \
    fluent-gem install fluent-plugin-s3 && \
    fluent-gem install fluent-plugin-opensearch && \
    gem install nokogiri && \
    yum clean all && \
    rm -rf /var/cache/yum 

# Create the log file for fluentd to trace immediately
RUN mkdir /app/logs && touch /app/logs/logfile.log

# Copy configuration files
COPY ./fluentd/fluentd-node.conf /etc/fluentd/
COPY ./supervisord.conf /etc/

# Copy and set permissions for the entrypoint script in a single step
COPY ./entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

EXPOSE 8080

# Use the entrypoint script to start services
ENTRYPOINT ["/app/entrypoint.sh"]