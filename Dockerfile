FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiamos el JAR generado por IntelliJ desde la carpeta target
COPY target/*.jar app.jar

# Creamos la carpeta para las subidas de archivos (file.upload-dir)
RUN mkdir uploads

EXPOSE 8092
ENTRYPOINT ["java", "-jar", "app.jar"]