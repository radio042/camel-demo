package org.acme.marketing;

import org.apache.camel.builder.RouteBuilder;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SimpleRoute extends RouteBuilder {

    @Override
    public void configure() {
//        from("kafka:marketing-topic?brokers=localhost:29092")
//                .routeId("simple-route")
//                .errorHandler(deadLetterChannel("kafka:error-topic?brokers=localhost:29092"))
//                .filter(jsonpath("$.[?(@.audience == 'marketing channels')]"))
//                .to("kafka:twitter-topic?brokers=localhost:29092")
//                .to("kafka:facebook-topic?brokers=localhost:29092");
    }
}
