FROM eclipse-temurin:21-jdk

# Install freetype and fontconfig for AWT
RUN apt-get update && apt-get install -y \
    libfreetype6 \
    fontconfig \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .

RUN ./mvnw clean package -DskipTests

CMD ["java", "-jar", "target/countryapi-0.0.1-SNAPSHOT.jar"]
