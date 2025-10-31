package com.agrotech.routes;

import org.apache.camel.builder.RouteBuilder;

public class RpcRoutes extends RouteBuilder {
    @Override
    public void configure() {
        from("direct:solicitarLectura")
                .routeId("rpc-cliente")
                .setHeader("id_sensor", simple("${body}"))
                .log("[CLIENTE] Solicitando lectura del sensor ${header.id_sensor}")
                .toD("direct:rpc.obtenerUltimo?timeout=2000")
                .log("[CLIENTE] Respuesta recibida: ${body}");

        from("direct:rpc.obtenerUltimo")
                .routeId("rpc-servidor")
                .log("[SERVIDOR] Solicitud recibida para sensor ${header.id_sensor}")
                .bean("servicioAnalitica", "getUltimoValor");
    }
}
