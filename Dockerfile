FROM eclipse-temurin:21-jdk-alpine

# Instalar netcat (nc) para que wait-for-it.sh funcione
RUN apk add --no-cache netcat-openbsd

# Crear directorio app y copiar el JAR
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app/app.jar

# Copiar y dar permisos al script
COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

# Exponer el puerto de la app
EXPOSE 8080

# Esperar a MySQL y luego ejecutar la app
ENTRYPOINT ["/wait-for-it.sh", "mysql-db", "3306", "--", "java", "-jar", "/app/app.jar"]
