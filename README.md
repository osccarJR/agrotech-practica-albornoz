# AgroTech Integration – Apache Camel 4 + SQLite

Integración que:
- Lee CSV desde `./inbox`, mueve OK a `./processed` y errores a `./logs` (con `.err`).
- Inserta/consulta lecturas en SQLite (`./database/lecturas.db`).
- Expone rutas RPC con `direct:` para prueba rápida.
- Maneja errores globales con `GlobalErrorRoute`.

## Requisitos
- JDK 21 (Temurin)
- Maven 3.9+
- (Opcional) DBeaver para ver `lecturas.db`

## Ejecutar
```bash
mvn clean package
# Desde IntelliJ: Run App (com.agrotech.App)
# o
mvn exec:java -Dexec.mainClass="com.agrotech.App"
