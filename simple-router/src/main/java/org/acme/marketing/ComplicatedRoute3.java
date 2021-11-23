package org.acme.marketing;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

public class ComplicatedRoute3 extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        errorHandler(deadLetterChannel("kafka:error-topic?brokers=localhost:29092"));
        from("rest:post:booking/{id}")
                .to("json-validator:ui-schema.json")
                .multicast().to("direct:customer-services", "direct:provider-services", "direct:analytics-services");

        from("direct:customer-services")
                .pollEnrich()
                .simple("rest:get:name/{id}?host=localhost:8080/providers")
                .aggregationStrategy(this::appendAsHeader)
                .process(this::messageToCustomerServices)
                .to("kafka:customer-events?brokers=localhost:29092");

        from("direct:provider-services")
                .pollEnrich()
                .simple("rest:get:name/{id}?host=localhost:8080/customers")
                .aggregationStrategy(this::appendAsHeader)
                .process(this::messageToProviderServices)
                .to("kafka:provider-events?brokers=localhost:29092");

        from("direct:customer-services")
                .process(this::messageToAnalyticsServices)
                .to("kafka:customer-events?brokers=localhost:29092");
    }

    private void messageToAnalyticsServices(Exchange exchange) {
        // todo
    }

    private void messageToProviderServices(Exchange exchange) {
        // todo
    }

    private void messageToCustomerServices(Exchange exchange) {
        // todo
    }

    private Exchange appendAsHeader(Exchange originalExchange, Exchange enrichmentExchange) {
        originalExchange.getMessage().setHeader("enrichment", enrichmentExchange.getMessage().getBody(String.class));
        return originalExchange;
    }

}
