package com.agrotech.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.csv.CsvDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;

public class FileTransferRoute extends RouteBuilder {
    @Override
    public void configure() {
        // Configuración del CsvDataFormat para mapear correctamente los nombres de las columnas
        var csv = new CsvDataFormat();
        csv.setUseMaps(true);
        csv.setSkipHeaderRecord(true);
        csv.setHeader(new String[] { "id_sensor", "fecha", "humedad", "temperatura" }); // Nombres de columnas

        // Ruta para procesar archivos CSV en el directorio inbox
        from("file:{{agrotech.in.inbox}}?initialDelay=0&delay=1000&include=.*\\.csv&move={{agrotech.in.processed}}&autoCreate=true")
                .routeId("file-transfer-csv-json")
                .log("[FILE] Archivo recibido: ${header.CamelFileName}")
                .unmarshal(csv)  // Deserializar CSV a un mapa de objetos
                .split(body()).streaming()  // Procesar cada fila
                .marshal().json(JsonLibrary.Jackson)  // Convertir a JSON
                .log("[FILE] JSON: ${body}")  // Log del JSON generado
                .to("direct:agroAnalyzer.ingresar")  // Enviar el JSON al siguiente componente
                .end()
                .log("[FILE] Transferencia completa (CSV -> JSON -> DB)");  // Log final
    }
}
