package org.acme.marketing;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

import javax.enterprise.context.ApplicationScoped;
import java.util.Map;

import static org.acme.marketing.Helper.toJson;

@ApplicationScoped
public class ComplicatedRoute2 extends RouteBuilder {

    @Override
    public void configure() {
        from("kafka:in?brokers=localhost:29092")
                .routeId("complicated-route-2")
                .errorHandler(deadLetterChannel("kafka:errors?brokers=localhost:29092"))
                .log("########### 1")
                .filter(jsonpath("$.[?(@.bringFriends == true)]"))
                .log("########### 2")
                .pollEnrich()
                .simple("file:classes?noop=true&idempotent=false&fileName=snacks.txt")
//                .aggregationStrategy(this::contentAsHeader)
                .log("########### 3")
                .split(header("snacks").tokenize(","))
                .process(this::messageToFriends)
                .to("kafka:out?brokers=localhost:29092");
    }

    private Exchange contentAsHeader(Exchange originalExchange, Exchange enrichmentExchange) {
        String snacks = enrichmentExchange.getMessage().getBody(String.class);
        originalExchange.getMessage().setHeader("snacks", snacks);
        return originalExchange;
    }

    private void messageToFriends(Exchange exchange) {
        final var body = exchange.getMessage().getBody(Map.class);
        final var inputMessage = body.get("message");
        final var outputMessage = toJson(Map.of("message", inputMessage));
        exchange.getMessage().setBody(outputMessage);
    }
}
