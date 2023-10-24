FROM maven:3.8-eclipse-temurin-8-alpine

# Copy local code to the container image.
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build a release artifact.
# RUN mvn package -DskipTests

# 这里不使用mvn打包会报错 把本地打包好的jar包传到Dockerfile的同级目录下然后执行命令
COPY api-service-0.0.1-SNAPSHOT.jar ./target/api-service-0.0.1-SNAPSHOT.jar

# Run the web service on container startup.
CMD ["java","-jar","/app/target/api-service-0.0.1-SNAPSHOT.jar","--spring.profiles.active=dev"]