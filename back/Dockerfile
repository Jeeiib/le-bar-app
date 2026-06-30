# Etape 1 : build du jar avec le wrapper Maven (JDK 25)
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw -B -DskipTests clean package

# Etape 2 : image d'execution legere (JRE 25)
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
