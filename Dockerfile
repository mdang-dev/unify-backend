FROM eclipse-temurin:21-jdk as builder

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src/ ./src/
COPY sonar-project.properties ./
RUN ./mvnw -ntp clean install -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

# ✅ OPTIMIZED: JVM memory settings for production stability
ENV JAVA_OPTS="-Xms256m -Xmx1g -XX:MaxMetaspaceSize=256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+OptimizeStringConcat -XX:+UseCompressedOops -XX:+UseCompressedClassPointers -XX:+UnlockExperimentalVMOptions -XX:+UseZGC -XX:+UnlockDiagnosticVMOptions -XX:+LogVMOutput -XX:LogFile=/tmp/jvm.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/ -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
