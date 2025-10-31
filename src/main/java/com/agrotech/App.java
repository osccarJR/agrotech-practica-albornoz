package com.agrotech;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.SimpleRegistry;
import org.apache.camel.component.sql.SqlComponent;
import org.sqlite.SQLiteDataSource;

import java.nio.file.Files;
import java.nio.file.Path;

import com.agrotech.routes.FileTransferRoute;
import com.agrotech.routes.SharedDbRoute;
import com.agrotech.routes.RpcRoutes;
import com.agrotech.service.ServicioAnalitica;
import com.agrotech.routes.GlobalErrorRoute;

public class App {
    public static void main(String[] args) throws Exception {
        // Crear directorios si no existen
        Files.createDirectories(Path.of("./inbox"));
        Files.createDirectories(Path.of("./processed"));
        Files.createDirectories(Path.of("./database"));
        Files.createDirectories(Path.of("./logs"));

        // Configuración de la base de datos SQLite
        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl("jdbc:sqlite:./database/lecturas.db");

        // Crear la tabla si no existe
        try (var conn = ds.getConnection(); var st = conn.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS lecturas (
                  id_sensor    VARCHAR(10),
                  fecha        TEXT,
                  humedad      DOUBLE,
                  temperatura  DOUBLE
                )
            """);
            // 💡 Evita duplicados del mismo sensor en la misma fecha
            st.executeUpdate("""
                CREATE UNIQUE INDEX IF NOT EXISTS ux_lecturas_sensor_fecha
                ON lecturas (id_sensor, fecha)
            """);
        }

        // Registro de componentes
        SimpleRegistry reg = new SimpleRegistry();
        reg.bind("sqliteDs", ds);
        reg.bind("servicioAnalitica", new ServicioAnalitica());

        // Configuración del CamelContext
        try (CamelContext camel = new DefaultCamelContext(reg)) {
            // Configuración del componente SQL
            SqlComponent sql = new SqlComponent();
            sql.setDataSource(ds);
            camel.addComponent("sql", sql);

            // Configurar el PropertiesComponent incorporado de Camel 4
            var pc = camel.getPropertiesComponent();
            pc.setLocation("classpath:application.properties");   // ✅ Solo classpath

            // Agregar rutas
            camel.addRoutes(new FileTransferRoute());
            camel.addRoutes(new SharedDbRoute());
            camel.addRoutes(new RpcRoutes());
            camel.addRoutes(new GlobalErrorRoute());

            // Iniciar el CamelContext
            camel.start();
            System.out.println("\n=== AgroTechIntegration ejecutándose. Presiona Ctrl+C para salir ===\n");

            // Pruebas rápidas
            var producer = camel.createProducerTemplate();
            var out = producer.requestBodyAndHeader("direct:db.ultimo", null, "id", "S001");
            System.out.println("[TEST] direct:db.ultimo S001 -> " + out);

            var rpc = producer.requestBody("direct:solicitarLectura", "S002", String.class);
            System.out.println("[TEST] RPC respuesta -> " + rpc);

            // Mantener el hilo principal activo
            Thread.currentThread().join();
        }
    }
}
