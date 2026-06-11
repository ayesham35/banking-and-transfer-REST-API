# Stage 1: Build the jar
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

# Copy Maven wrapper and pom first - this layer caches as long as pom.xml doesnt change
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

# Now copy source and build
COPY src ./src
RUN ./mvnw package -DskipTests -B

# Stage 2: Extract layered jar
FROM eclipse-temurin:25-jdk AS extractor
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Stage 3: Runtime image
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy layers in increasing order of change frequency
COPY --from=extractor /app/dependencies/ ./
COPY --from=extractor /app/spring-boot-loader/ ./
COPY --from=extractor /app/snapshot-dependencies/ ./
COPY --from=extractor /app/application/ ./

EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]