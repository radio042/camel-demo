package org.acme.marketing;

import org.apache.camel.builder.RouteBuilder;

public class SimpleRoute3 extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        errorHandler(deadLetterChannel("kafka:error-topic?brokers=localhost:29092"));
        from("rest:post:booking/{id}")
                .to("json-validator:ui-schema.json")
                .to("kafka:bookings?brokers=localhost:29092");
    }
}
