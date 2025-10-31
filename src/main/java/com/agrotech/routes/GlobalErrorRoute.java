// src/main/java/com/agrotech/routes/GlobalErrorRoute.java
package com.agrotech.routes;

import org.apache.camel.builder.RouteBuilder;

public class GlobalErrorRoute extends RouteBuilder {
    @Override
    public void configure() {
        onException(Exception.class)
                .handled(true)
                .log("ERROR procesando ${header.CamelFileNameOnly} : ${exception.message}")
                .setHeader("errorMessage").simple("${exception.message}")
                .setHeader("originalFile").simple("${header.CamelFileNameOnly}")
                .to("file:./logs?fileName=${date:now:yyyyMMdd-HHmmss}-${header.CamelFileNameOnly}.err");
    }
}
