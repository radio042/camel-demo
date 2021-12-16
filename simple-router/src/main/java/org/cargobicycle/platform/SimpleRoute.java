package org.cargobicycle.platform;

import org.apache.camel.builder.RouteBuilder;

public class SimpleRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        errorHandler(deadLetterChannel("kafka:error-topic?brokers=localhost:29092"));

        from("rest:post:booking")
                .id("simple-route")
                .to("json-validator:ui-schema.json")
                .to("kafka:bookings?brokers=localhost:29092");
    }
}
