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

ENV JAVA_OPTS="-Xms1g -Xmx2g -XX:MaxMetaspaceSize=512m -XX:+UseG1GC -XX:MaxGCPauseMillis=300 -XX:+UseStringDeduplication -XX:+OptimizeStringConcat -XX:+UseCompressedOops -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/ -XX:+ExitOnOutOfMemoryError -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:/tmp/gc.log"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
