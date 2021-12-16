package org.cargobicycle.platform;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jsonvalidator.JsonValidationException;

public class SimpleRoute extends RouteBuilder {
    @Override
    public void configure() {
        onException(JsonValidationException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .to("kafka:error-topic?brokers=localhost:29092");

        from("rest:post:booking")
                .id("simple-route")
                .to("json-validator:ui-schema.json")
                .to("kafka:bookings?brokers=localhost:29092");
    }
}
