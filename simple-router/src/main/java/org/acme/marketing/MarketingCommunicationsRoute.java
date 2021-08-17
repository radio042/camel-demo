package org.acme.marketing;

import org.apache.camel.builder.RouteBuilder;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MarketingCommunicationsRoute extends RouteBuilder {

    @Override
    public void configure() {
        from("kafka:marketing-topic?brokers=localhost:29092")
                .routeId("marketing-route")
                .errorHandler(deadLetterChannel("kafka:error-topic?brokers=localhost:29092"))
                .filter(jsonpath("$.[?(@.audience == 'marketing channels')]"))
                .to("kafka:social-media-topic?brokers=localhost:29092")
                .to("kafka:local-papers-topic?brokers=localhost:29092");
    }
}
