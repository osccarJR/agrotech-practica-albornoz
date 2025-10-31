package com.agrotech.routes;

import org.apache.camel.builder.RouteBuilder;

public class SharedDbRoute extends RouteBuilder {
    @Override
    public void configure() {
        from("direct:agroAnalyzer.ingresar")
                .routeId("shared-db-insert")
                .log("[DB] Insertando lectura en DB desde JSON: ${body}")
                .setHeader("id_sensor").jsonpath("$.id_sensor")
                .setHeader("fecha").jsonpath("$.fecha")
                .setHeader("humedad").jsonpath("$.humedad")
                .setHeader("temperatura").jsonpath("$.temperatura")
                // UPSERT: Si ya existe, actualiza los valores
                .toD("sql:INSERT INTO lecturas (id_sensor, fecha, humedad, temperatura) " +
                        "VALUES (:#id_sensor, :#fecha, :#humedad, :#temperatura) " +
                        "ON CONFLICT(id_sensor, fecha) DO UPDATE SET " +
                        "humedad = excluded.humedad, " +
                        "temperatura = excluded.temperatura")
                .log("[DB] Lectura insertada o actualizada OK");

        from("direct:db.ultimo")
                .routeId("shared-db-select-ultimo")
                .setHeader("id").simple("${header.id}")
                .to("sql:SELECT id_sensor, fecha, humedad, temperatura FROM lecturas WHERE id_sensor = :#id ORDER BY fecha DESC LIMIT 1?outputType=SelectOne")
                .log("[DB] Último valor DB: ${body}");
    }
}
